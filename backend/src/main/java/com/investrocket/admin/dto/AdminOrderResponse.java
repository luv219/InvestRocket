package com.investrocket.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.order.Order;

public record AdminOrderResponse(
        UUID id,
        String userEmail,
        String symbol,
        String side,
        String orderType,
        Integer quantity,
        String status,
        BigDecimal totalAmount,
        Instant createdAt) {

    public static AdminOrderResponse from(Order order) {
        return new AdminOrderResponse(
                order.getId(),
                order.getUser().getEmail(),
                order.getSymbol(),
                order.getSide().name(),
                order.getOrderType().name(),
                order.getQuantity(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt());
    }
}
