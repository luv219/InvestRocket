package com.investrocket.watchlist.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WatchlistItemResponse(
        UUID id,
        String symbol,
        String companyName,
        String exchange,
        String currency,
        BigDecimal currentPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        Instant latestTradingTime,
        Instant createdAt) {
}
