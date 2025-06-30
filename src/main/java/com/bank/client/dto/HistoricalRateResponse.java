package com.bank.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public record HistoricalRateResponse(
        String result,

        @JsonProperty("conversion_rates")
        Map<String, BigDecimal> conversionRates
) {}