package com.investrocket.admin.dto;

import java.util.List;

public record AdminTradingStatsResponse(
        long totalBuyOrders,
        long totalSellOrders,
        long totalMarketOrders,
        long totalLimitOrders,
        long totalStopLossOrders,
        List<SymbolCountResponse> mostTradedSymbols,
        List<UserMetricResponse> topUsersByTradeCount,
        List<UserMetricResponse> topUsersByPortfolioValue,
        List<AdminTradeResponse> recentTrades,
        List<AdminOrderResponse> recentOrders) {
}
