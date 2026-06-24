package com.investrocket.journal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investrocket.user.User;

public interface TradingJournalRepository
        extends JpaRepository<TradingJournalEntry, UUID> {
    List<TradingJournalEntry> findByUserOrderByCreatedAtDesc(User user);
    List<TradingJournalEntry> findByUserAndSymbolOrderByCreatedAtDesc(User user, String symbol);
    Optional<TradingJournalEntry> findByIdAndUser(UUID id, User user);
}
