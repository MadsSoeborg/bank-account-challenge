package com.bank.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_acc_date", columnList = "account_id, timestamp DESC")
})
public class LedgerEntry extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    public Account account;

    @Column(nullable = false, precision = 19, scale = 4)
    public BigDecimal amount;

    @Column(name = "balance_snapshot", nullable = false, precision = 19, scale = 4)
    public BigDecimal balanceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TransactionType type;

    @Column(nullable = false, updatable = false)
    public Instant timestamp = Instant.now();

    @Column(name = "reference_info")
    public String referenceInfo;

    public static LedgerEntry create(Account account, BigDecimal amount, BigDecimal newBalance, TransactionType type,
            String referenceInfo) {
        LedgerEntry entry = new LedgerEntry();
        entry.account = account;
        entry.amount = amount;
        entry.balanceSnapshot = newBalance;
        entry.type = type;
        entry.referenceInfo = referenceInfo;
        return entry;
    }
}