package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.model.Account;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
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
        return Account.find("accountNumber", accountNumber).firstResultOptional();
    }

    public BigDecimal getBalance(String accountNumber) {
        Account account = getAccountByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return account.balance;
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        Account account = getAccountByNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.balance = account.balance.add(amount);
        return account;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

        long debitRows = Account.update(
                "balance = balance - ?1 WHERE accountNumber = ?2 AND balance >= ?1",
                amount, fromAccountNumber
        );

        if (debitRows == 0) {
            getAccountByNumber(fromAccountNumber)
                    .orElseThrow(() -> new AccountNotFoundException(fromAccountNumber));

            throw new IllegalStateException("Insufficient funds in account " + fromAccountNumber);
        }

        long creditRows = Account.update(
                "balance = balance + ?1 WHERE accountNumber = ?2",
                amount, toAccountNumber
        );

        if (creditRows == 0) {
            throw new AccountNotFoundException(toAccountNumber);
        }
    }
}
