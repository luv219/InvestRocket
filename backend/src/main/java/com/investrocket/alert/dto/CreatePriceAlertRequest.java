package com.investrocket.alert.dto;

import java.math.BigDecimal;

import com.investrocket.alert.PriceAlertCondition;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePriceAlertRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,
        @NotNull(message = "Target price is required")
        @DecimalMin(value = "0.0001", message = "Target price must be positive")
        BigDecimal targetPrice,
        @NotNull(message = "Condition is required")
        PriceAlertCondition condition) {
}
