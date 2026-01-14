package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.Account;
import com.bank.model.TransactionEntry;
import com.bank.model.TransactionType;

import io.quarkus.panache.common.Sort;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AccountService {

    @Transactional
    public Account createAccount(String firstName, String lastName) {
        Account newAccount = new Account(firstName, lastName);
        newAccount.persist();
        return newAccount;
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return Account.find("from Account where accountNumber = ?1", accountNumber).firstResultOptional();
    }

    public BigDecimal getBalance(String accountNumber) {
        return findAccountByNumberOrThrow(accountNumber).balance;
    }

    public List<TransactionEntry> getTransactionHistory(String accountNumber, int pageIndex, int pageSize) {
        findAccountByNumberOrThrow(accountNumber);

        return TransactionEntry.find("accountNumber", Sort.descending("timestamp"), accountNumber)
                .page(Page.of(pageIndex, pageSize)).list();
    }

    @Transactional
    @Counted(value = "bank.deposits.count", description = "Number of deposits")
    @Timed(value = "bank.deposits.timer", description = "Time taken to deposit")
    public Account deposit(String accountNumber, BigDecimal amount) {
        int rowsUpdated = Account.update("balance = balance + ?1 WHERE accountNumber = ?2", amount, accountNumber);

        if (rowsUpdated == 0) {
            throw new AccountNotFoundException(accountNumber);
        }

        TransactionEntry.create(accountNumber, amount, TransactionType.DEPOSIT, null).persist();

        return findAccountByNumberOrThrow(accountNumber);
    }

    @Transactional
    @Counted(value = "bank.withdrawals.count", description = "Number of withdrawals")
    @Timed(value = "bank.withdrawals.timer", description = "Time taken to withdraw")
    public Account withdraw(String accountNumber, BigDecimal amount) {
        long rowsUpdated = Account.update(
                "balance = balance - ?1 WHERE accountNumber = ?2 AND balance >= ?1", amount, accountNumber);

        if (rowsUpdated == 0) {
            getAccountByNumber(accountNumber).orElseThrow(() -> new AccountNotFoundException(accountNumber));

            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        TransactionEntry.create(accountNumber, amount.negate(), TransactionType.WITHDRAWAL, null).persist();

        return findAccountByNumberOrThrow(accountNumber);
    }

    @Transactional
    @Counted(value = "bank.transfers.count", description = "Number of transfers")
    @Timed(value = "bank.transfers.timer", description = "Time taken to transfer")
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

        if (fromAccountNumber.compareTo(toAccountNumber) < 0) {
            executeDebit(fromAccountNumber, toAccountNumber, amount);
            executeCredit(fromAccountNumber, toAccountNumber, amount);
        } else {
            executeCredit(fromAccountNumber, toAccountNumber, amount);
            executeDebit(fromAccountNumber, toAccountNumber, amount);
        }
    }

    private void executeDebit(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        long debitRows = Account.update(
                "balance = balance - ?1 WHERE accountNumber = ?2 AND balance >= ?1",
                amount, fromAccountNumber);

        if (debitRows == 0) {
            getAccountByNumber(fromAccountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(fromAccountNumber));
            throw new InsufficientFundsException("Insufficient funds in account " + fromAccountNumber);
        }

        TransactionEntry.create(fromAccountNumber, amount.negate(), TransactionType.TRANSFER_OUT, toAccountNumber)
                .persist();
    }

    private void executeCredit(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        long creditRows = Account.update(
                "balance = balance + ?1 WHERE accountNumber = ?2",
                amount, toAccountNumber);

        if (creditRows == 0) {
            throw new AccountNotFoundException(toAccountNumber);
        }

        TransactionEntry.create(toAccountNumber, amount, TransactionType.TRANSFER_IN, fromAccountNumber).persist();
    }

    private Account findAccountByNumberOrThrow(String accountNumber) {
        return Account.<Account>find("from Account where accountNumber = ?1", accountNumber).firstResultOptional()
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

}