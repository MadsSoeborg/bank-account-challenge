package com.bank.api;

import com.bank.api.dto.FullExchangeRateReport;
import com.bank.service.ExchangeRateService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;


@Path("/exchange-rates")
@Produces(MediaType.APPLICATION_JSON)
public class ExchangeRateResource {

    @Inject
    ExchangeRateService exchangeRateService;

    @GET
    @Path("/dkk-usd")
    public Uni<Response> getDkkToUsdRate(@QueryParam("amount") @DefaultValue("100") BigDecimal baseAmount) {
        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Message("Query parameter 'amount' must be positive.")).build());
        }

        return exchangeRateService.fetchDkkToUsdConversion(baseAmount)
                .onItem().transform(rateResponse -> Response.ok(rateResponse).build())
                .onFailure().recoverWithItem(failure -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new Message("Failed to retrieve exchange rate: " + failure.getMessage())).build());
    }

    @GET
    @Path("/dkk-usd/historical-report")
    public Uni<FullExchangeRateReport> getHistoricalReport() {
        return exchangeRateService.getHistoricalReport();
    }

    public record Message(String message) {}
}