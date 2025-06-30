package com.bank.api.dto;

import com.bank.util.MoneySerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;

public record HistoricalRateEntry(
        int year,

        @JsonSerialize(using = MoneySerializer.class)
        BigDecimal usdValue
) {}