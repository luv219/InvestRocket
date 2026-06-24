package com.investrocket.user.dto;

import java.math.BigDecimal;

import com.investrocket.user.UserRiskSettings;

public record RiskSettingsResponse(
        BigDecimal maxOrderValue,
        Integer maxDailyTrades,
        boolean allowStopLossOrders,
        boolean allowLimitOrders) {

    public static RiskSettingsResponse from(UserRiskSettings settings) {
        return new RiskSettingsResponse(
                settings.getMaxOrderValue(),
                settings.getMaxDailyTrades(),
                settings.isAllowStopLossOrders(),
                settings.isAllowLimitOrders());
    }
}
