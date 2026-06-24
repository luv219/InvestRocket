package com.investrocket.order.dto;

import java.math.BigDecimal;

import com.investrocket.order.OrderSide;
import com.investrocket.order.OrderType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotNull(message = "Order side is required")
        OrderSide side,

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than 0")
        Integer quantity,

        BigDecimal limitPrice,

        BigDecimal stopPrice) {
}
