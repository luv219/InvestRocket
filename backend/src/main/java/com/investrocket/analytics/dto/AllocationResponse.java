package com.investrocket.analytics.dto;

import java.math.BigDecimal;

public record AllocationResponse(
        String symbol,
        String companyName,
        BigDecimal currentValue,
        BigDecimal allocationPercent) {
}
