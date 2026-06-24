package com.investrocket.journal;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.order.Order;
import com.investrocket.trade.Trade;
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
@Table(name = "trading_journal_entries")
public class TradingJournalEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private JournalMood mood;

    @Column(length = 150)
    private String strategy;

    @Column(length = 15)
    private String symbol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @Column(length = 500)
    private String tags;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TradingJournalEntry() {
    }

    public TradingJournalEntry(
            User user,
            String title,
            String content,
            JournalMood mood,
            String strategy,
            String symbol,
            Order order,
            Trade trade,
            String tags) {
        this.id = UUID.randomUUID();
        this.user = user;
        update(title, content, mood, strategy, symbol, tags);
        this.order = order;
        this.trade = trade;
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

    public void update(
            String title,
            String content,
            JournalMood mood,
            String strategy,
            String symbol,
            String tags) {
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.strategy = strategy;
        this.symbol = symbol;
        this.tags = tags;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public JournalMood getMood() { return mood; }
    public String getStrategy() { return strategy; }
    public String getSymbol() { return symbol; }
    public Order getOrder() { return order; }
    public Trade getTrade() { return trade; }
    public String getTags() { return tags; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
