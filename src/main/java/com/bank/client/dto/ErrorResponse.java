package com.bank.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        String result,

        @JsonProperty("error-type")
        String errorType
) {}