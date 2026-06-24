package com.investrocket.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
        BigDecimal cashBalance,
        BigDecimal holdingsValue,
        BigDecimal totalPortfolioValue,
        BigDecimal totalInvested,
        BigDecimal unrealizedProfitLoss,
        BigDecimal unrealizedProfitLossPercent,
        Integer numberOfHoldings) {
}
