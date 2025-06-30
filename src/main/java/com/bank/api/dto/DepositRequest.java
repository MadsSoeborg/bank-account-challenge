package com.bank.api.dto;

import java.math.BigDecimal;

public record DepositRequest(BigDecimal amount) {}