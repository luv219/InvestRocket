package com.investrocket.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt,
        BigDecimal walletCashBalance,
        long totalOrders,
        long totalTrades,
        long totalHoldings) {
}
