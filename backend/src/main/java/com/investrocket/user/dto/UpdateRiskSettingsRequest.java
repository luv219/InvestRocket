package com.investrocket.user.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateRiskSettingsRequest(
        @NotNull(message = "Maximum order value is required")
        @DecimalMin(value = "0.01", message = "Maximum order value must be positive")
        @DecimalMax(value = "100000.00", message = "Maximum order value cannot exceed 100000.00")
        BigDecimal maxOrderValue,
        @NotNull(message = "Maximum daily trades is required")
        @Min(value = 1, message = "Maximum daily trades must be positive")
        @Max(value = 200, message = "Maximum daily trades cannot exceed 200")
        Integer maxDailyTrades,
        boolean allowStopLossOrders,
        boolean allowLimitOrders) {
}
