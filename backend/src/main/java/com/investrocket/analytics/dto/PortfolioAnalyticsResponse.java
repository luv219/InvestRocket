package com.investrocket.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioAnalyticsResponse(
        BigDecimal currentPortfolioValue,
        BigDecimal initialBalance,
        BigDecimal cashBalance,
        BigDecimal reservedCash,
        BigDecimal holdingsValue,
        BigDecimal totalInvested,
        BigDecimal realizedProfitLoss,
        BigDecimal unrealizedProfitLoss,
        BigDecimal totalProfitLoss,
        BigDecimal totalReturnPercent,
        HoldingPerformanceResponse bestHolding,
        HoldingPerformanceResponse worstHolding,
        List<AllocationResponse> allocation,
        List<PortfolioPerformancePoint> performanceHistory,
        TradingStatsResponse tradingStats) {
}
