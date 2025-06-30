package com.bank.client;

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
    Response getPairConversion(
            @PathParam("apiKey") String apiKey,
            @PathParam("baseCode") String baseCode,
            @PathParam("targetCode") String targetCode,
            @PathParam("amount") int amount
    );
}