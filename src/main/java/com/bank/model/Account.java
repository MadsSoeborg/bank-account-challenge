package com.bank.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_acc_num", columnList = "accountNumber", unique = true)
})
public class Account extends PanacheEntity {

    @Column(nullable = false, unique = true, updatable = false)
    public String accountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal balance;

    @Version
    public long version;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AccountStatus status = AccountStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }

    public Account() {
    }

    public Account(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = BigDecimal.ZERO;
        this.accountNumber = UUID.randomUUID().toString();
        this.status = AccountStatus.ACTIVE;
    }
}