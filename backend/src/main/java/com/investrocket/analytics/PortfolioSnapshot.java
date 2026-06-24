package com.investrocket.analytics;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.investrocket.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshot {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedCash;

    @Column(name = "holdings_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal holdingsValue;

    @Column(name = "total_portfolio_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPortfolioValue;

    @Column(name = "total_invested", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalInvested;

    @Column(name = "unrealized_profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal unrealizedProfitLoss;

    @Column(name = "realized_profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedProfitLoss;

    @Column(name = "daily_profit_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyProfitLoss;

    @Column(name = "daily_profit_loss_percent", nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyProfitLossPercent;

    @Column(name = "overall_return_percent", nullable = false, precision = 19, scale = 4)
    private BigDecimal overallReturnPercent;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "snapshot_time", nullable = false)
    private Instant snapshotTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PortfolioSnapshot() {
    }

    public PortfolioSnapshot(
            User user,
            BigDecimal cashBalance,
            BigDecimal reservedCash,
            BigDecimal holdingsValue,
            BigDecimal totalPortfolioValue,
            BigDecimal totalInvested,
            BigDecimal unrealizedProfitLoss,
            BigDecimal realizedProfitLoss,
            BigDecimal dailyProfitLoss,
            BigDecimal dailyProfitLossPercent,
            BigDecimal overallReturnPercent,
            LocalDate snapshotDate,
            Instant snapshotTime) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.cashBalance = cashBalance;
        this.reservedCash = reservedCash;
        this.holdingsValue = holdingsValue;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalInvested = totalInvested;
        this.unrealizedProfitLoss = unrealizedProfitLoss;
        this.realizedProfitLoss = realizedProfitLoss;
        this.dailyProfitLoss = dailyProfitLoss;
        this.dailyProfitLossPercent = dailyProfitLossPercent;
        this.overallReturnPercent = overallReturnPercent;
        this.snapshotDate = snapshotDate;
        this.snapshotTime = snapshotTime;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getReservedCash() {
        return reservedCash;
    }

    public BigDecimal getHoldingsValue() {
        return holdingsValue;
    }

    public BigDecimal getTotalPortfolioValue() {
        return totalPortfolioValue;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public BigDecimal getUnrealizedProfitLoss() {
        return unrealizedProfitLoss;
    }

    public BigDecimal getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public BigDecimal getDailyProfitLoss() {
        return dailyProfitLoss;
    }

    public BigDecimal getDailyProfitLossPercent() {
        return dailyProfitLossPercent;
    }

    public BigDecimal getOverallReturnPercent() {
        return overallReturnPercent;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public Instant getSnapshotTime() {
        return snapshotTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
