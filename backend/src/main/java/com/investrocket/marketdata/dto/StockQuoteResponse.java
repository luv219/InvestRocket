package com.investrocket.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record StockQuoteResponse(
        String symbol,
        String companyName,
        BigDecimal currentPrice,
        BigDecimal changeAmount,
        BigDecimal changePercent,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal previousClose,
        Long volume,
        Instant latestTradingTime,
        String currency,
        String provider) {
}
