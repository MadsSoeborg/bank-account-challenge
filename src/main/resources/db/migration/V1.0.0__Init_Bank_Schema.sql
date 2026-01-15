-- Create Accounts Table
CREATE TABLE accounts (
    id BIGINT NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);

-- Unique Constraint on Account Number
ALTER TABLE accounts ADD CONSTRAINT uc_accounts_accountnumber UNIQUE (account_number);

-- Sequence for Account IDs
CREATE SEQUENCE accounts_SEQ START WITH 1 INCREMENT BY 50;


-- Create Ledger Entries Table
CREATE TABLE ledger_entries (
    id BIGINT NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    balance_snapshot NUMERIC(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    reference_info VARCHAR(255),
    account_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- Foreign Key: Ledger -> Account
ALTER TABLE ledger_entries 
    ADD CONSTRAINT fk_ledger_account 
    FOREIGN KEY (account_id) 
    REFERENCES accounts (id);

-- Index for searching history efficiently
CREATE INDEX idx_ledger_acc_date ON ledger_entries(account_id, timestamp DESC);

-- Sequence for Ledger IDs
CREATE SEQUENCE ledger_entries_SEQ START WITH 1 INCREMENT BY 50;