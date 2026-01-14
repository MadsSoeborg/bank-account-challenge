package com.bank.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class TransactionEntry extends PanacheEntity {
    public String accountNumber;

    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    public TransactionType type;

    public LocalDateTime timestamp;

    public String relatedAccountNumber;

    public static TransactionEntry create(String accountNumber, BigDecimal amount, TransactionType type,
            String relatedAccount) {
        TransactionEntry entry = new TransactionEntry();
        entry.accountNumber = accountNumber;
        entry.amount = amount;
        entry.type = type;
        entry.timestamp = LocalDateTime.now();
        entry.relatedAccountNumber = relatedAccount;
        return entry;
    }
}