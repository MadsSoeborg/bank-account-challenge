CREATE TABLE idempotency_records (
    idempotency_key VARCHAR(255) NOT NULL,
    response_status INTEGER NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    response_message VARCHAR(255),
    PRIMARY KEY (idempotency_key)
);