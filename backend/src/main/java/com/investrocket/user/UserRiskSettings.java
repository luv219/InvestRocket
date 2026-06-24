package com.investrocket.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_risk_settings")
public class UserRiskSettings {

    public static final BigDecimal DEFAULT_MAX_ORDER_VALUE = new BigDecimal("25000.00");
    public static final int DEFAULT_MAX_DAILY_TRADES = 50;

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "max_order_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxOrderValue;

    @Column(name = "max_daily_trades", nullable = false)
    private Integer maxDailyTrades;

    @Column(name = "allow_stop_loss_orders", nullable = false)
    private boolean allowStopLossOrders;

    @Column(name = "allow_limit_orders", nullable = false)
    private boolean allowLimitOrders;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserRiskSettings() {
    }

    public UserRiskSettings(User user) {
        id = UUID.randomUUID();
        this.user = user;
        maxOrderValue = DEFAULT_MAX_ORDER_VALUE;
        maxDailyTrades = DEFAULT_MAX_DAILY_TRADES;
        allowStopLossOrders = true;
        allowLimitOrders = true;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getMaxOrderValue() {
        return maxOrderValue;
    }

    public Integer getMaxDailyTrades() {
        return maxDailyTrades;
    }

    public boolean isAllowStopLossOrders() {
        return allowStopLossOrders;
    }

    public boolean isAllowLimitOrders() {
        return allowLimitOrders;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            BigDecimal maxOrderValue,
            Integer maxDailyTrades,
            boolean allowStopLossOrders,
            boolean allowLimitOrders) {
        this.maxOrderValue = maxOrderValue;
        this.maxDailyTrades = maxDailyTrades;
        this.allowStopLossOrders = allowStopLossOrders;
        this.allowLimitOrders = allowLimitOrders;
    }
}
