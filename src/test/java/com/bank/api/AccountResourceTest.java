package com.bank.api;

import com.bank.api.dto.CreateAccountRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.comparesEqualTo;

@QuarkusTest
public class AccountResourceTest {

    @BeforeAll
    public static void setup() {
        JsonConfig jsonConfig = JsonConfig.jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig);
    }

    @Test
    public void testCreateAccountAndGetBalance() {
        BigDecimal expectedBalance = new BigDecimal("0.00");

        String newAccountJson = given()
                .contentType(ContentType.JSON)
                .body(new CreateAccountRequest("John", "Doe"))
                .when().post("/accounts")
                .then()
                .statusCode(201)
                .body("accountNumber", notNullValue())
                .body("firstName", is("John"))
                .body("lastName", is("Doe"))
                .body("balance", comparesEqualTo(expectedBalance))
                .extract().asString();

        String accountNumber = com.jayway.jsonpath.JsonPath.read(newAccountJson, "$.accountNumber");

        given()
                .when().get("/accounts/" + accountNumber + "/balance")
                .then()
                .statusCode(200)
                .body("accountNumber", is(accountNumber))
                .body("balance", comparesEqualTo(expectedBalance));
    }

    @Test
    public void testGetBalanceForNonExistentAccount() {
        given()
                .when().get("/accounts/fake-account-123/balance")
                .then()
                .statusCode(404);
    }
}