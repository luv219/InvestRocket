package com.investrocket.trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.order.Order;
import com.investrocket.order.OrderSide;
import com.investrocket.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 15)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrderSide side;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "trade_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal tradeValue;

    @Column(name = "realized_profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedProfitLoss;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected Trade() {
    }

    public Trade(
            Order order,
            User user,
            String symbol,
            OrderSide side,
            Integer quantity,
            BigDecimal price,
            BigDecimal tradeValue,
            BigDecimal realizedProfitLoss) {
        this.id = UUID.randomUUID();
        this.order = order;
        this.user = user;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.tradeValue = tradeValue;
        this.realizedProfitLoss = realizedProfitLoss;
        this.executedAt = order.getExecutedAt();
    }

    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
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

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTradeValue() {
        return tradeValue;
    }

    public BigDecimal getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
