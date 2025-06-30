package com.bank.client;

import com.bank.client.dto.HistoricalRateResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = "exchangerate-api")
@Path("/v6/{apiKey}")
public interface ExchangeRateApiClient {


    @GET
    @Path("/pair/{baseCode}/{targetCode}/{amount}")
    Uni<Response> getPairConversion(
            @PathParam("apiKey") String apiKey,
            @PathParam("baseCode") String baseCode,
            @PathParam("targetCode") String targetCode,
            @PathParam("amount") int amount
    );


    @GET
    @Path("/history/{baseCode}/{year}/{month}/{day}")
    Uni<HistoricalRateResponse> getHistoricalRate(
            @PathParam("apiKey") String apiKey,
            @PathParam("baseCode") String baseCode,
            @PathParam("year") int year,
            @PathParam("month") int month,
            @PathParam("day") int day
    );
}