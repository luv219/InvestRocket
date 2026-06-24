package com.investrocket.portfolio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_holdings_user_symbol",
                columnNames = {"user_id", "symbol"}))
public class Holding {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 15)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "locked_quantity", nullable = false)
    private Integer lockedQuantity;

    @Column(name = "average_buy_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageBuyPrice;

    @Column(name = "total_invested", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalInvested;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Holding() {
    }

    public Holding(
            User user,
            String symbol,
            String companyName,
            Integer quantity,
            BigDecimal buyPrice) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.lockedQuantity = 0;
        this.averageBuyPrice = buyPrice;
        this.totalInvested = buyPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
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

    public void add(Integer additionalQuantity, BigDecimal buyPrice) {
        BigDecimal currentCost = averageBuyPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal additionalCost = buyPrice.multiply(BigDecimal.valueOf(additionalQuantity));
        int newQuantity = quantity + additionalQuantity;
        averageBuyPrice = currentCost.add(additionalCost)
                .divide(BigDecimal.valueOf(newQuantity), 4, RoundingMode.HALF_UP);
        quantity = newQuantity;
        totalInvested = averageBuyPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void remove(Integer soldQuantity) {
        quantity -= soldQuantity;
        totalInvested = averageBuyPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void lock(Integer quantityToLock) {
        lockedQuantity += quantityToLock;
    }

    public void unlock(Integer quantityToUnlock) {
        lockedQuantity -= quantityToUnlock;
    }

    public void executeLockedSale(Integer soldQuantity) {
        quantity -= soldQuantity;
        lockedQuantity -= soldQuantity;
        totalInvested = averageBuyPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
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

    public String getCompanyName() {
        return companyName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getLockedQuantity() {
        return lockedQuantity;
    }

    public Integer getAvailableQuantity() {
        return quantity - lockedQuantity;
    }

    public BigDecimal getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
