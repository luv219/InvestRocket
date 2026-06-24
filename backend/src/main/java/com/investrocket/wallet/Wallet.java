package com.investrocket.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.investrocket.user.User;

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
@Table(name = "wallets")
public class Wallet {

    public static final BigDecimal DEFAULT_BALANCE = new BigDecimal("100000.00");
    public static final String DEFAULT_CURRENCY = "USD";

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance;

    @Column(name = "reserved_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedBalance;

    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(User user) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.cashBalance = DEFAULT_BALANCE;
        this.reservedBalance = BigDecimal.ZERO.setScale(2);
        this.initialBalance = DEFAULT_BALANCE;
        this.currency = DEFAULT_CURRENCY;
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

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public BigDecimal getReservedBalance() {
        return reservedBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void debit(BigDecimal amount) {
        cashBalance = cashBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        cashBalance = cashBalance.add(amount);
    }

    public void reserve(BigDecimal amount) {
        cashBalance = cashBalance.subtract(amount);
        reservedBalance = reservedBalance.add(amount);
    }

    public void releaseReserved(BigDecimal amount) {
        reservedBalance = reservedBalance.subtract(amount);
        cashBalance = cashBalance.add(amount);
    }

    public void settleReserved(BigDecimal reservedAmount, BigDecimal executedAmount) {
        reservedBalance = reservedBalance.subtract(reservedAmount);
        cashBalance = cashBalance.add(reservedAmount.subtract(executedAmount));
    }
}
