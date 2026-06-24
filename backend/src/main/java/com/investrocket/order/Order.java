package com.investrocket.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 15)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "requested_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal requestedPrice;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Column(name = "stop_price", precision = 19, scale = 4)
    private BigDecimal stopPrice;

    @Column(name = "executed_price", precision = 19, scale = 4)
    private BigDecimal executedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "status_reason", length = 255)
    private String statusReason;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    protected Order() {
    }

    private Order(
            User user,
            String symbol,
            OrderSide side,
            OrderType orderType,
            Integer quantity,
            BigDecimal requestedPrice,
            BigDecimal limitPrice,
            BigDecimal stopPrice,
            BigDecimal totalAmount,
            OrderStatus status,
            String statusReason) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
        this.quantity = quantity;
        this.requestedPrice = requestedPrice;
        this.limitPrice = limitPrice;
        this.stopPrice = stopPrice;
        this.totalAmount = totalAmount;
        this.status = status;
        this.statusReason = statusReason;
    }

    public static Order pending(
            User user,
            String symbol,
            OrderSide side,
            OrderType orderType,
            Integer quantity,
            BigDecimal requestedPrice,
            BigDecimal limitPrice,
            BigDecimal stopPrice,
            BigDecimal reservedOrEstimatedAmount) {
        return new Order(
                user,
                symbol,
                side,
                orderType,
                quantity,
                requestedPrice,
                limitPrice,
                stopPrice,
                reservedOrEstimatedAmount,
                OrderStatus.PENDING,
                "Waiting for trigger condition");
    }

    public static Order executed(
            User user,
            String symbol,
            OrderSide side,
            OrderType orderType,
            Integer quantity,
            BigDecimal requestedPrice,
            BigDecimal limitPrice,
            BigDecimal stopPrice,
            BigDecimal executedPrice,
            BigDecimal totalAmount) {
        Order order = new Order(
                user,
                symbol,
                side,
                orderType,
                quantity,
                requestedPrice,
                limitPrice,
                stopPrice,
                totalAmount,
                OrderStatus.EXECUTED,
                "Order executed");
        order.executedPrice = executedPrice;
        order.executedAt = Instant.now();
        return order;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void markExecuted(BigDecimal price, BigDecimal amount) {
        executedPrice = price;
        totalAmount = amount;
        status = OrderStatus.EXECUTED;
        statusReason = "Order executed";
        executedAt = Instant.now();
    }

    public void cancel() {
        status = OrderStatus.CANCELLED;
        statusReason = "Cancelled by user";
        cancelledAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getRequestedPrice() {
        return requestedPrice;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public BigDecimal getStopPrice() {
        return stopPrice;
    }

    public BigDecimal getExecutedPrice() {
        return executedPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
