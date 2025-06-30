package com.bank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountNumber) {
        super("Account with number '" + accountNumber + "' not found.");
    }
}