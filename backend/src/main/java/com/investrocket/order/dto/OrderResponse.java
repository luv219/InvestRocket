package com.investrocket.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.order.Order;
import com.investrocket.order.OrderSide;
import com.investrocket.order.OrderStatus;
import com.investrocket.order.OrderType;

public record OrderResponse(
        UUID id,
        String symbol,
        OrderSide side,
        OrderType orderType,
        Integer quantity,
        BigDecimal limitPrice,
        BigDecimal stopPrice,
        BigDecimal requestedPrice,
        BigDecimal executedPrice,
        OrderStatus status,
        String statusReason,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant executedAt,
        Instant cancelledAt,
        String message) {

    public static OrderResponse from(Order order, String message) {
        return new OrderResponse(
                order.getId(),
                order.getSymbol(),
                order.getSide(),
                order.getOrderType(),
                order.getQuantity(),
                order.getLimitPrice(),
                order.getStopPrice(),
                order.getRequestedPrice(),
                order.getExecutedPrice(),
                order.getStatus(),
                order.getStatusReason(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getExecutedAt(),
                order.getCancelledAt(),
                message);
    }
}
