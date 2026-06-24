package com.investrocket.analytics.dto;

import java.math.BigDecimal;

public record DashboardAnalyticsResponse(
        BigDecimal currentPortfolioValue,
        BigDecimal totalProfitLoss,
        BigDecimal totalReturnPercent,
        BigDecimal cashBalance,
        BigDecimal holdingsValue) {
}
