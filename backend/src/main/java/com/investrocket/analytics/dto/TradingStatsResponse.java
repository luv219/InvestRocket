package com.investrocket.analytics.dto;

import java.math.BigDecimal;

public record TradingStatsResponse(
        long totalTrades,
        long buyTrades,
        long sellTrades,
        long totalOrders,
        long executedOrders,
        long pendingOrders,
        long cancelledOrders,
        BigDecimal realizedProfitLoss,
        long winningSellTrades,
        long losingSellTrades,
        BigDecimal winRatePercent) {
}
