package com.investrocket.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdminMarketDataStatusResponse(
        String provider,
        String status,
        String testSymbol,
        BigDecimal currentPrice,
        Instant checkedAt,
        String message) {
}
