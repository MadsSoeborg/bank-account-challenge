# Bank Account System

This project is a simple REST API for a bank account system, built with Java and the Quarkus framework. It fulfills the core requirements of creating accounts, handling deposits and transfers, and checking balances.

## Core Technologies

- **Framework**: Java 21 with Quarkus
- **Build Tool**: Gradle
- **Database**: H2 (In-Memory)
- **Persistence**: Hibernate ORM with Panache
- **Testing**: JUnit 5, REST Assured, Mockito

## Prerequisites

- JDK 17 or higher

## How to Run the Application

The application can be run locally using Quarkus's development mode, which provides live reloading of code changes.

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/MadsSoeborg/bank-account-challenge.git
    cd bank-account-challenge
    ```

2.  **Run the application:**
    - In the terminal:
      ```bash
      ./gradlew quarkusDev
      ```

The application will start and be available at `http://localhost:8080`.

## How to Run the Tests

To run the full suite of automated tests, use the following command:

- From the terminal:
  ```bash
  ./gradlew test
  ```

---

## API Endpoints

The base URL for all endpoints is `http://localhost:8080`.

**Note on Pre-populated Data:** When the application starts in `dev` or `test` mode, the database is automatically populated with two accounts with the following account numbers:

- `account-1` (Balance: 100.00)
- `account-2` (Balance: 50.00)

**Note on `cURL` for Windows:** The inline `cURL` examples below use syntax for Linux/macOS. For a cross-platform solution, it is recommended to save the JSON body to a file (e.g., `data.json`) and run the command like so:
`curl -X POST ... -d @data.json`

### Core Banking API

#### 1. Create a New Account

- **Endpoint:** `POST /accounts`
- **cURL Example:**
  ```bash
  curl -i -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"firstName": "Alice", "lastName": "Smith"}'
  ```

#### 2. Get Balance for an Account

- **Endpoint:** `GET /accounts/{accountNumber}/balance`
- **cURL Example:**
  ```bash
  curl http://localhost:8080/accounts/account-1/balance
  ```

#### 3. Deposit Money to an Account

- **Endpoint:** `POST /accounts/{accountNumber}/deposit`
- **cURL Example:**
  ```bash
  curl -i -X POST http://localhost:8080/accounts/account-2/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 50.25}'
  ```

#### 4. Transfer Money Between Accounts

- **Endpoint:** `POST /transfers`
- **cURL Example:**
  ```bash
  curl -i -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -d '{"fromAccountNumber": "account-1", "toAccountNumber": "account-2", "amount": 75.00}'
  ```
