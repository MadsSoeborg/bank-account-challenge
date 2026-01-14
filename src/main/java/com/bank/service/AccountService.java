package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.Account;
import com.bank.model.TransactionEntry;
import com.bank.model.TransactionType;

import io.quarkus.panache.common.Sort;
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

    public List<TransactionEntry> getTransactionHistory(String accountNumber) {
        findAccountByNumberOrThrow(accountNumber);

        return TransactionEntry.list("accountNumber", Sort.descending("timestamp"), accountNumber);
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = findAccountByNumberOrThrow(accountNumber);
        account.balance = account.balance.add(amount);

        TransactionEntry.create(accountNumber, amount, TransactionType.DEPOSIT, null).persist();

        return account;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

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