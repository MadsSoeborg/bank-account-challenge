package com.bank.api.dto;

import com.bank.util.MoneySerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigDecimal;

public record ExchangeRateResponse(
        @JsonProperty("DKK")
        @JsonSerialize(using = MoneySerializer.class)
        BigDecimal dkk,

        @JsonProperty("USD")
        @JsonSerialize(using = MoneySerializer.class)
        BigDecimal usd
) {}