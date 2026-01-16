package com.bank.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord extends PanacheEntityBase {

    @Id
    @Column(name = "idempotency_key", nullable = false)
    public String idempotencyKey;

    @Column(name = "response_status", nullable = false)
    public int responseStatus;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "response_message")
    public String responseMessage;

    public static IdempotencyRecord of(String key, int status, String message) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.idempotencyKey = key;
        record.responseStatus = status;
        record.responseMessage = message;
        record.createdAt = Instant.now();
        return record;
    }
}