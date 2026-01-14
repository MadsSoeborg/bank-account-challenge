package com.bank.api.dto;

import com.bank.model.TransactionType;
import com.bank.util.MoneySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String accountNumber,
        @JsonSerialize(using = MoneySerializer.class) BigDecimal amount,
        TransactionType type,
        LocalDateTime timestamp,
        String relatedAccountNumber) {
}