-- Clean up
DELETE FROM ledger_entries;
DELETE FROM accounts;

-- Reset Sequences
ALTER SEQUENCE accounts_SEQ RESTART WITH 1;
ALTER SEQUENCE ledger_entries_SEQ RESTART WITH 1;

-- Insert Accounts
INSERT INTO accounts(id, account_number, balance, version, first_name, last_name, status, created_at) 
VALUES (1, 'account-1', 100.00, 0, 'Alice', 'Sender', 'ACTIVE', CURRENT_TIMESTAMP);

INSERT INTO accounts(id, account_number, balance, version, first_name, last_name, status, created_at) 
VALUES (2, 'account-2', 50.00, 0, 'Bob', 'Receiver', 'ACTIVE', CURRENT_TIMESTAMP);

-- Update Sequence so the next new account gets ID 3
ALTER SEQUENCE accounts_SEQ RESTART WITH 3;