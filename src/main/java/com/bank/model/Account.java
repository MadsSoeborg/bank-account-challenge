package com.bank.model;

import com.bank.util.MoneySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Account extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String accountNumber;

    @Column(nullable = false)
    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal balance;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    public Account() {}

    public Account(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = BigDecimal.ZERO;
        this.accountNumber = UUID.randomUUID().toString();
    }
}
