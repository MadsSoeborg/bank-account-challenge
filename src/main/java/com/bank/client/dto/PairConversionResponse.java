package com.bank.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record PairConversionResponse(
        String result,

        @JsonProperty("conversion_result")
        BigDecimal conversionResult
) {}