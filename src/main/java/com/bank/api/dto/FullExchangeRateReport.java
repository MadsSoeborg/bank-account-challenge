package com.bank.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FullExchangeRateReport(
        @JsonProperty("today")
        ExchangeRateResponse today,

        @JsonProperty("historical")
        List<HistoricalRateEntry> historical
) {}