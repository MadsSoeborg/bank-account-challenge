package com.bank.service;

import com.bank.model.Account;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@QuarkusTest
public class ConcurrencyTest {

    @Inject
    AccountService accountService;

    @Test
    public void testConcurrentDeposits() throws InterruptedException {
        String accountNumber = createAccount("Concurrent", "User");

        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        BigDecimal amount = new BigDecimal("10.00");

        // 2. Fire 50 threads, each depositing 10.00
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    accountService.deposit(accountNumber, amount);
                } catch (Exception e) {
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        BigDecimal finalBalance = accountService.getBalance(accountNumber);
        System.out.println("Final Balance: " + finalBalance);

        Assertions.assertEquals(0, new BigDecimal("500.00").compareTo(finalBalance),
                "Lost updates occurred! Expected 500.00 but got " + finalBalance);
    }

    @Transactional
    String createAccount(String first, String last) {
        Account acc = new Account(first, last);
        acc.persist();
        return acc.accountNumber;
    }
}