-- Clean up
DELETE FROM ledger_entries;
DELETE FROM accounts;

-- Reset Sequences
ALTER SEQUENCE accounts_SEQ RESTART WITH 1;
ALTER SEQUENCE ledger_entries_SEQ RESTART WITH 1;

-- Insert Accounts (Note: version=0, status=0/ACTIVE)
-- Assuming ID 1 and 2 are generated.
INSERT INTO accounts(id, accountNumber, balance, version, firstName, lastName, status, createdAt) 
VALUES (1, 'account-1', 100.00, 0, 'Alice', 'Sender', 'ACTIVE', CURRENT_TIMESTAMP());

INSERT INTO accounts(id, accountNumber, balance, version, firstName, lastName, status, createdAt) 
VALUES (2, 'account-2', 50.00, 0, 'Bob', 'Receiver', 'ACTIVE', CURRENT_TIMESTAMP());

-- Update Sequence for next inserts
ALTER SEQUENCE accounts_SEQ RESTART WITH 3;