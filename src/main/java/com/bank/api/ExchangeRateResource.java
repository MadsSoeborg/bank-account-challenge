package com.bank.api;

import com.bank.api.dto.ExchangeRateResponse;
import com.bank.service.ExchangeRateService;
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
    public Response getDkkToUsdRate(
            @QueryParam("amount") @DefaultValue("100") BigDecimal baseAmount) {

        if (baseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new Message("Query parameter 'amount' must be positive."))
                    .build();
        }

        try {
            ExchangeRateResponse rateResponse = exchangeRateService.getDkkToUsdConversion(baseAmount);
            return Response.ok(rateResponse).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new Message("Failed to retrieve exchange rate: " + e.getMessage()))
                    .build();
        }
    }

    public record Message(String message) {}
}