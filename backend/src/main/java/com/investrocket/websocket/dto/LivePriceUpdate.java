package com.investrocket.websocket.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LivePriceUpdate(
        String symbol,
        BigDecimal currentPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        Instant latestTradingTime,
        String provider) {
}
