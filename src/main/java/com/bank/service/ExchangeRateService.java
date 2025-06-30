package com.bank.service;

import com.bank.api.dto.ExchangeRateResponse;
import com.bank.client.ExchangeRateApiClient;
import com.bank.client.dto.ErrorResponse;
import com.bank.client.dto.PairConversionResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;

@ApplicationScoped
public class ExchangeRateService {

    @Inject
    @RestClient
    ExchangeRateApiClient apiClient;

    @ConfigProperty(name = "exchangerate.api.key")
    String apiKey;

    public ExchangeRateResponse getDkkToUsdConversion(BigDecimal baseAmount) {
        final String baseCode = "DKK";
        final String targetCode = "USD";

        try (Response response = apiClient.getPairConversion(apiKey, baseCode, targetCode, baseAmount.intValue())) {

            if (response.getStatus() != 200) {
                ErrorResponse error = response.readEntity(ErrorResponse.class);
                throw new RuntimeException("Error from exchange rate API: " + error.errorType());
            }

            PairConversionResponse conversionData = response.readEntity(PairConversionResponse.class);
            if (!"success".equals(conversionData.result())) {
                throw new RuntimeException("Exchange rate API call was not successful.");
            }

            return new ExchangeRateResponse(baseAmount, conversionData.conversionResult());
        }
    }
}