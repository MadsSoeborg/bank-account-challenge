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
        return Account.find("from Account where accountNumber = ?1", accountNumber).firstResultOptional();
    }

    public BigDecimal getBalance(String accountNumber) {
        return findAccountByNumberOrThrow(accountNumber).balance;
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = findAccountByNumberOrThrow(accountNumber);
        account.balance = account.balance.add(amount);
        return account;
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
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

    private Account findAccountByNumberOrThrow(String accountNumber) {
        return Account.<Account>find("from Account where accountNumber = ?1", accountNumber).firstResultOptional()
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}