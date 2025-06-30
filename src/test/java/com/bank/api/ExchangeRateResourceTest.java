package com.bank.api;

import com.bank.client.ExchangeRateApiClient;
import com.bank.client.dto.HistoricalRateResponse;
import com.bank.client.dto.PairConversionResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.path.json.config.JsonPathConfig;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.comparesEqualTo;

@QuarkusTest
public class ExchangeRateResourceTest {

    @InjectMock
    @RestClient
    ExchangeRateApiClient exchangeRateApiClient;

    @BeforeAll
    public static void setup() {
        JsonConfig jsonConfig = JsonConfig.jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig);
    }

    @Test
    public void testGetDkkToUsdRate_Success_WithDefaultAmount() {
        int amount = 100;
        BigDecimal expectedDkk = new BigDecimal("100.00");
        BigDecimal expectedUsd = new BigDecimal("14.61");
        PairConversionResponse mockApiResponse = new PairConversionResponse("success", expectedUsd);

        Mockito.when(exchangeRateApiClient.getPairConversion(Mockito.anyString(), Mockito.eq("DKK"), Mockito.eq("USD"), Mockito.eq(amount)))
                .thenReturn(Uni.createFrom().item(Response.ok(mockApiResponse).build()));

        given()
                .when().get("/exchange-rates/dkk-usd")
                .then()
                .statusCode(200)
                .body("DKK", comparesEqualTo(expectedDkk))
                .body("USD", comparesEqualTo(expectedUsd));
    }

    @Test
    public void testGetDkkToUsdRate_Success_WithCustomAmount() {
        int customAmount = 500;
        BigDecimal exchangeRate = new BigDecimal("0.1461");
        BigDecimal expectedUsdResultFromApi = new BigDecimal(customAmount).multiply(exchangeRate);
        BigDecimal expectedDkkInResponse = new BigDecimal(customAmount);
        PairConversionResponse mockApiResponse = new PairConversionResponse("success", expectedUsdResultFromApi);

        Mockito.when(exchangeRateApiClient.getPairConversion(Mockito.anyString(), Mockito.eq("DKK"), Mockito.eq("USD"), Mockito.eq(customAmount)))
                .thenReturn(Uni.createFrom().item(Response.ok(mockApiResponse).build()));

        given()
                .queryParam("amount", customAmount)
                .when().get("/exchange-rates/dkk-usd")
                .then()
                .statusCode(200)
                .body("DKK", comparesEqualTo(expectedDkkInResponse))
                .body("USD", comparesEqualTo(expectedUsdResultFromApi));
    }

    @Test
    public void testGetDkkToUsdRate_InvalidAmount() {
        given()
                .queryParam("amount", -50)
                .when().get("/exchange-rates/dkk-usd")
                .then()
                .statusCode(400)
                .body("message", is("Query parameter 'amount' must be positive."));
    }

    @Test
    public void testGetDkkToUsdRate_ApiFailure() {
        Mockito.when(exchangeRateApiClient.getPairConversion(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(Uni.createFrom().item(Response.serverError().build()));

        given()
                .when().get("/exchange-rates/dkk-usd")
                .then()
                .statusCode(500);
    }

    @Test
    public void testGetHistoricalReport() {
        BigDecimal todayUsd = new BigDecimal("14.61");
        PairConversionResponse todayResponse = new PairConversionResponse("success", todayUsd);
        Mockito.when(exchangeRateApiClient.getPairConversion(Mockito.anyString(), Mockito.eq("DKK"), Mockito.eq("USD"), Mockito.eq(100)))
                .thenReturn(Uni.createFrom().item(Response.ok(todayResponse).build()));

        BigDecimal historicalRate = new BigDecimal("0.1500");
        Map<String, BigDecimal> ratesMap = Collections.singletonMap("USD", historicalRate);
        HistoricalRateResponse mockHistoricalResponse = new HistoricalRateResponse("success", ratesMap);

        Mockito.when(exchangeRateApiClient.getHistoricalRate(Mockito.anyString(), Mockito.eq("DKK"), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Uni.createFrom().item(mockHistoricalResponse));

        given()
                .when().get("/exchange-rates/dkk-usd/historical-report")
                .then()
                .statusCode(200)
                .body("today.DKK", comparesEqualTo(new BigDecimal("100.00")))
                .body("today.USD", comparesEqualTo(new BigDecimal("14.61")))
                .body("historical.size()", is(10))
                .body("historical[0].year", is(2005))
                .body("historical[0].usdValue", comparesEqualTo(new BigDecimal("15.00")));
    }
}