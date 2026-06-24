package com.investrocket.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.trade.Trade;

public record AdminTradeResponse(
        UUID id,
        String userEmail,
        String symbol,
        String side,
        Integer quantity,
        BigDecimal price,
        BigDecimal tradeValue,
        BigDecimal realizedProfitLoss,
        Instant executedAt) {

    public static AdminTradeResponse from(Trade trade) {
        return new AdminTradeResponse(
                trade.getId(),
                trade.getUser().getEmail(),
                trade.getSymbol(),
                trade.getSide().name(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTradeValue(),
                trade.getRealizedProfitLoss(),
                trade.getExecutedAt());
    }
}
