DELETE FROM Account;

-- Create two accounts with known account numbers and balances.
INSERT INTO Account(id, accountNumber, balance, firstName, lastName) VALUES (1, 'account-1', 100.00, 'Alice', 'Sender');
INSERT INTO Account(id, accountNumber, balance, firstName, lastName) VALUES (2, 'account-2', 50.00, 'Bob', 'Receiver');

ALTER SEQUENCE Account_SEQ RESTART WITH 3;