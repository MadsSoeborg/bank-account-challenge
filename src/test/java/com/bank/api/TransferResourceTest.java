package com.bank.api;

import com.bank.api.dto.TransferRequest;
import com.bank.model.Account;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.JsonConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.comparesEqualTo;

@QuarkusTest
public class TransferResourceTest {

    private Account account1;
    private Account account2;

    @BeforeAll
    public static void setup() {
        JsonConfig jsonConfig = JsonConfig.jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL);
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig);
    }

    @BeforeEach
    @Transactional
    public void setupAccounts() {
        Account.deleteAll();

        account1 = new Account("Sender", "One");
        account1.balance = new BigDecimal("100.00");
        account1.persist();

        account2 = new Account("Receiver", "Two");
        account2.balance = new BigDecimal("50.00");
        account2.persist();
    }

    @Test
    public void testSuccessfulTransfer() {
        BigDecimal transferAmount = new BigDecimal("25.50");
        TransferRequest request = new TransferRequest(account1.accountNumber, account2.accountNumber, transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(200)
                .body("message", is("Transfer successful"));

        BigDecimal expectedBalance1 = new BigDecimal("74.50");
        given()
                .when().get("/accounts/" + account1.accountNumber + "/balance")
                .then()
                .statusCode(200)
                .body("balance", comparesEqualTo(expectedBalance1));

        BigDecimal expectedBalance2 = new BigDecimal("75.50");
        given()
                .when().get("/accounts/" + account2.accountNumber + "/balance")
                .then()
                .statusCode(200)
                .body("balance", comparesEqualTo(expectedBalance2));
    }

    @Test
    public void testTransferWithInsufficientFunds() {
        BigDecimal transferAmount = new BigDecimal("100.01");
        TransferRequest request = new TransferRequest(account1.accountNumber, account2.accountNumber, transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(400)
                .body("message", is("Insufficient funds in account " + account1.accountNumber));
    }

    @Test
    public void testTransferToNonExistentAccount() {
        BigDecimal transferAmount = new BigDecimal("10.00");
        TransferRequest request = new TransferRequest(account1.accountNumber, "fake-account-999", transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(404)
                .body("message", is("Account with number 'fake-account-999' not found."));
    }

    @Test
    public void testTransferFromNonExistentAccount() {
        BigDecimal transferAmount = new BigDecimal("10.00");
        TransferRequest request = new TransferRequest("fake-account-888", account2.accountNumber, transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(404)
                .body("message", is("Account with number 'fake-account-888' not found."));
    }

    @Test
    public void testTransferToSameAccount() {
        BigDecimal transferAmount = new BigDecimal("10.00");
        TransferRequest request = new TransferRequest(account1.accountNumber, account1.accountNumber, transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(400)
                .body("message", is("Cannot transfer money to the same account."));
    }

    @Test
    public void testTransferWithNegativeAmount() {
        BigDecimal transferAmount = new BigDecimal("-50.00");
        TransferRequest request = new TransferRequest(account1.accountNumber, account2.accountNumber, transferAmount);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/transfers")
                .then()
                .statusCode(400)
                .body("message", is("Transfer amount must be positive."));
    }
}