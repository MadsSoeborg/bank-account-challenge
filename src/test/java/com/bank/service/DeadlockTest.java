package com.bank.service;

import com.bank.model.Account;
import com.bank.model.LedgerEntry;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@QuarkusTest
public class DeadlockTest {

    @Inject
    AccountService accountService;

    private String accountA;
    private String accountB;

    @BeforeEach
    @Transactional
    public void setup() {
        LedgerEntry.deleteAll();
        Account.deleteAll();
        accountA = createAccount("Alice", "Deadlock", new BigDecimal("1000.00"));
        accountB = createAccount("Bob", "Deadlock", new BigDecimal("1000.00"));
    }

    @Test
    public void testDeadlockPotential() throws InterruptedException {
        int threadCount = 4; // 2 threads moving A->B, 2 threads moving B->A
        int transfersPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1); // Starting gun
        AtomicInteger failureCount = new AtomicInteger(0);

        Runnable transferAtoB = () -> {
            try {
                latch.await(); // Wait for starting gun
                for (int i = 0; i < transfersPerThread; i++) {
                    accountService.transfer(accountA, accountB, BigDecimal.ONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failureCount.incrementAndGet();
            }
        };

        Runnable transferBtoA = () -> {
            try {
                latch.await();
                for (int i = 0; i < transfersPerThread; i++) {
                    accountService.transfer(accountB, accountA, BigDecimal.ONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failureCount.incrementAndGet();
            }
        };

        executor.submit(transferAtoB);
        executor.submit(transferAtoB);
        executor.submit(transferBtoA);
        executor.submit(transferBtoA);

        // Start all threads simultaneously
        latch.countDown();

        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

        Assertions.assertTrue(finished, "Test timed out - potential deadlock detected!");
        Assertions.assertEquals(0, failureCount.get(),
                "Exceptions occurred during transfer (Deadlocks throw exceptions)");

        // Verify balances are consistent
        BigDecimal balanceA = accountService.getBalance(accountA);
        BigDecimal balanceB = accountService.getBalance(accountB);

        // Since we sent equal amounts back and forth, balances should remain 1000.00
        Assertions.assertEquals(0, new BigDecimal("1000.00").compareTo(balanceA));
        Assertions.assertEquals(0, new BigDecimal("1000.00").compareTo(balanceB));
    }

    @Transactional
    String createAccount(String first, String last, BigDecimal balance) {
        Account acc = new Account(first, last);
        acc.balance = balance;
        acc.persist();
        return acc.accountNumber;
    }
}