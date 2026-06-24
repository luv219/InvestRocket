package com.investrocket.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PortfolioPerformancePoint(
        LocalDate date,
        Instant snapshotTime,
        BigDecimal totalPortfolioValue,
        BigDecimal cashBalance,
        BigDecimal holdingsValue,
        BigDecimal dailyProfitLoss,
        BigDecimal dailyProfitLossPercent) {
}
