package com.bank.service;

import com.bank.api.dto.ExchangeRateResponse;
import com.bank.api.dto.FullExchangeRateReport;
import com.bank.api.dto.HistoricalRateEntry;
import com.bank.client.ExchangeRateApiClient;
import com.bank.client.dto.ErrorResponse;
import com.bank.client.dto.PairConversionResponse;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@ApplicationScoped
public class ExchangeRateService {

    private static final Logger LOG = Logger.getLogger(ExchangeRateService.class);

    @Inject
    @RestClient
    ExchangeRateApiClient apiClient;

    @ConfigProperty(name = "exchangerate.api.key")
    String apiKey;


    public Uni<ExchangeRateResponse> fetchDkkToUsdConversion(BigDecimal baseAmount) {
        return apiClient.getPairConversion(apiKey, "DKK", "USD", baseAmount.intValue())
                .onItem().transform(response -> {
                    if (response.getStatus() != 200) {
                        ErrorResponse error = response.readEntity(ErrorResponse.class);
                        throw new WebApplicationException("Error from exchange rate API: " + error.errorType(), 502);
                    }
                    PairConversionResponse data = response.readEntity(PairConversionResponse.class);
                    if (!"success".equals(data.result())) {
                        throw new RuntimeException("Exchange rate API call was not successful.");
                    }
                    return new ExchangeRateResponse(baseAmount, data.conversionResult());
                });
    }


    public Uni<FullExchangeRateReport> getHistoricalReport() {
        final BigDecimal baseAmount = new BigDecimal("100");

        Uni<ExchangeRateResponse> todayRateUni = this.fetchDkkToUsdConversion(baseAmount);

        List<Integer> years = IntStream.rangeClosed(2005, 2015).filter(y -> y != 2012).boxed().toList();
        List<Uni<HistoricalRateEntry>> historicalRateUnis = years.stream()
                .map(year -> getHistoricalEntryUni(year, baseAmount))
                .collect(Collectors.toList());

        Uni<List<HistoricalRateEntry>> allHistoricalRatesUni = Uni.combine().all()
                .unis(historicalRateUnis)
                .with(results -> results.stream()
                        .filter(Objects::nonNull)
                        .map(result -> (HistoricalRateEntry) result)
                        .collect(Collectors.toList()));

        return Uni.combine().all().unis(todayRateUni, allHistoricalRatesUni)
                .asTuple()
                .onItem().transform(this::buildReportFromTuple);
    }


    private Uni<HistoricalRateEntry> getHistoricalEntryUni(int year, BigDecimal baseAmount) {
        return apiClient.getHistoricalRate(apiKey, "DKK", year, 1, 1)
                .onItem().transform(response -> {
                    BigDecimal rate = response.conversionRates().get("USD");
                    if (rate == null) {
                        LOG.warnf("USD rate not found in historical data for year %d", year);
                        return null;
                    }
                    BigDecimal convertedValue = baseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                    return new HistoricalRateEntry(year, convertedValue);
                })
                .onFailure().invoke(failure -> {
                    LOG.errorf(failure, "Failed to retrieve historical rate for year %d. Reason: %s", year, failure.getMessage());
                })
                .onFailure().recoverWithNull();
    }

    private FullExchangeRateReport buildReportFromTuple(Tuple2<ExchangeRateResponse, List<HistoricalRateEntry>> results) {
        return new FullExchangeRateReport(results.getItem1(), results.getItem2());
    }
}