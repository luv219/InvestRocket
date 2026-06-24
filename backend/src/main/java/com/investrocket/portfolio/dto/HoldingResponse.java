package com.investrocket.portfolio.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        String symbol,
        String companyName,
        Integer quantity,
        Integer lockedQuantity,
        Integer availableQuantity,
        BigDecimal averageBuyPrice,
        BigDecimal currentPrice,
        BigDecimal totalInvested,
        BigDecimal currentValue,
        BigDecimal unrealizedProfitLoss,
        BigDecimal unrealizedProfitLossPercent) {
}
