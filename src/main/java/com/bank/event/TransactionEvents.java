package com.bank.event;

import java.math.BigDecimal;

public class TransactionEvents {

    public record DepositEvent(String accountNumber, BigDecimal amount) {
    }

    public record WithdrawEvent(String accountNumber, BigDecimal amount) {
    }
}