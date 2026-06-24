package com.investrocket.admin.dto;

import java.math.BigDecimal;

public record UserMetricResponse(
        String email,
        long tradeCount,
        BigDecimal portfolioValue) {
}
