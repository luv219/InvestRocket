package com.investrocket.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardStatsResponse(
        long totalUsers,
        long enabledUsers,
        long disabledUsers,
        long adminUsers,
        long totalOrders,
        long executedOrders,
        long pendingOrders,
        long cancelledOrders,
        long rejectedOrders,
        long totalTrades,
        long totalWatchlistItems,
        long totalPortfolioSnapshots,
        BigDecimal totalVirtualCash,
        BigDecimal totalHoldingsValue,
        BigDecimal totalPlatformPortfolioValue,
        BigDecimal totalRealizedProfitLoss,
        BigDecimal totalUnrealizedProfitLoss) {
}
