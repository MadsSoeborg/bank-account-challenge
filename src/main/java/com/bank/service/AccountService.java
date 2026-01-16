package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientFundsException;
import com.bank.model.Account;
import com.bank.model.IdempotencyRecord;
import com.bank.model.LedgerEntry;
import com.bank.model.TransactionType;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class AccountService {

    @Transactional
    public Account createAccount(String firstName, String lastName) {
        Account newAccount = new Account(firstName, lastName);
        newAccount.persist();
        return newAccount;
    }

    public BigDecimal getBalance(String accountNumber) {
        return findAccountByNumberOrThrow(accountNumber).balance;
    }

    public List<LedgerEntry> getTransactionHistory(String accountNumber, int pageIndex, int pageSize) {
        Account account = findAccountByNumberOrThrow(accountNumber);

        return LedgerEntry.find("account", Sort.descending("timestamp"), account)
                .page(Page.of(pageIndex, pageSize)).list();
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = findAccountAndLock(accountNumber);

        if (account.status != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        account.balance = account.balance.add(amount);

        LedgerEntry.create(account, amount, account.balance, TransactionType.DEPOSIT, null).persist();

        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = findAccountAndLock(accountNumber);

        if (account.status != Account.AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        if (account.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.balance = account.balance.subtract(amount);

        LedgerEntry.create(account, amount.negate(), account.balance, TransactionType.WITHDRAWAL, null).persist();

        return account;
    }

    @Transactional
    public boolean transfer(String idempotencyKey, String fromAccountNumber, String toAccountNumber,
            BigDecimal amount) {

        if (idempotencyKey != null) {
            boolean exists = IdempotencyRecord.count("idempotencyKey", idempotencyKey) > 0;
            if (exists) {
                return false;
            }
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

        Account firstLock = fromAccountNumber.compareTo(toAccountNumber) < 0
                ? findAccountAndLock(fromAccountNumber)
                : findAccountAndLock(toAccountNumber);

        Account secondLock = fromAccountNumber.compareTo(toAccountNumber) < 0
                ? findAccountAndLock(toAccountNumber)
                : findAccountAndLock(fromAccountNumber);

        Account source = firstLock.accountNumber.equals(fromAccountNumber) ? firstLock : secondLock;
        Account target = firstLock.accountNumber.equals(toAccountNumber) ? firstLock : secondLock;

        if (source.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + fromAccountNumber);
        }

        source.balance = source.balance.subtract(amount);
        target.balance = target.balance.add(amount);

        LedgerEntry.create(source, amount.negate(), source.balance, TransactionType.TRANSFER_OUT, toAccountNumber)
                .persist();
        LedgerEntry.create(target, amount, target.balance, TransactionType.TRANSFER_IN, fromAccountNumber).persist();

        if (idempotencyKey != null) {
            IdempotencyRecord.of(idempotencyKey, 200, "Transfer successful").persist();
        }

        return true;
    }

    private Account findAccountAndLock(String accountNumber) {
        return Account.<Account>find("accountNumber", accountNumber)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional()
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private Account findAccountByNumberOrThrow(String accountNumber) {
        return Account.<Account>find("accountNumber", accountNumber)
                .firstResultOptional()
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}