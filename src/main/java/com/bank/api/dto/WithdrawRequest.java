package com.bank.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record WithdrawRequest(
        @NotNull(message = "Amount cannot be null") @Positive(message = "Amount must be positive") BigDecimal amount) {
}