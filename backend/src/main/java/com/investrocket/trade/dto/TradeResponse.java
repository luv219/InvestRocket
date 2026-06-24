package com.investrocket.trade.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.order.OrderSide;
import com.investrocket.trade.Trade;

public record TradeResponse(
        UUID id,
        UUID orderId,
        String symbol,
        OrderSide side,
        Integer quantity,
        BigDecimal price,
        BigDecimal tradeValue,
        BigDecimal realizedProfitLoss,
        Instant executedAt) {

    public static TradeResponse from(Trade trade) {
        return new TradeResponse(
                trade.getId(),
                trade.getOrder().getId(),
                trade.getSymbol(),
                trade.getSide(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTradeValue(),
                trade.getRealizedProfitLoss(),
                trade.getExecutedAt());
    }
}
