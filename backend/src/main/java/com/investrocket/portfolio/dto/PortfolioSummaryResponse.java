package com.investrocket.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        BigDecimal availableCash,
        BigDecimal reservedCash,
        BigDecimal totalCash,
        BigDecimal holdingsValue,
        BigDecimal totalPortfolioValue,
        BigDecimal totalInvested,
        BigDecimal unrealizedProfitLoss,
        BigDecimal unrealizedProfitLossPercent,
        Integer numberOfHoldings) {
}
