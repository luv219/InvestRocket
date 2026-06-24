package com.investrocket.alert;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 15)
    private String symbol;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "target_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PriceAlertCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceAlertStatus status;

    @Column(name = "triggered_price", precision = 19, scale = 4)
    private BigDecimal triggeredPrice;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PriceAlert() {
    }

    public PriceAlert(
            User user,
            String symbol,
            String companyName,
            BigDecimal targetPrice,
            PriceAlertCondition condition) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.symbol = symbol;
        this.companyName = companyName;
        this.targetPrice = targetPrice;
        this.condition = condition;
        this.status = PriceAlertStatus.ACTIVE;
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

    public boolean shouldTrigger(BigDecimal currentPrice) {
        return condition == PriceAlertCondition.ABOVE
                ? currentPrice.compareTo(targetPrice) >= 0
                : currentPrice.compareTo(targetPrice) <= 0;
    }

    public void trigger(BigDecimal currentPrice) {
        status = PriceAlertStatus.TRIGGERED;
        triggeredPrice = currentPrice;
        triggeredAt = Instant.now();
    }

    public void cancel() {
        status = PriceAlertStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getSymbol() { return symbol; }
    public String getCompanyName() { return companyName; }
    public BigDecimal getTargetPrice() { return targetPrice; }
    public PriceAlertCondition getCondition() { return condition; }
    public PriceAlertStatus getStatus() { return status; }
    public BigDecimal getTriggeredPrice() { return triggeredPrice; }
    public Instant getTriggeredAt() { return triggeredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
