package com.investrocket.analytics.dto;

import java.math.BigDecimal;

public record HoldingPerformanceResponse(
        String symbol,
        String companyName,
        Integer quantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal totalInvested,
        BigDecimal unrealizedProfitLoss,
        BigDecimal unrealizedProfitLossPercent) {
}
