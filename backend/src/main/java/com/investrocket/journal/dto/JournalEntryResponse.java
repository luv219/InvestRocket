package com.investrocket.journal.dto;

import java.time.Instant;
import java.util.UUID;

import com.investrocket.journal.JournalMood;
import com.investrocket.journal.TradingJournalEntry;

public record JournalEntryResponse(
        UUID id,
        String title,
        String content,
        JournalMood mood,
        String strategy,
        String symbol,
        UUID orderId,
        UUID tradeId,
        String tags,
        Instant createdAt,
        Instant updatedAt) {

    public static JournalEntryResponse from(TradingJournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(), entry.getTitle(), entry.getContent(),
                entry.getMood(), entry.getStrategy(), entry.getSymbol(),
                entry.getOrder() == null ? null : entry.getOrder().getId(),
                entry.getTrade() == null ? null : entry.getTrade().getId(),
                entry.getTags(), entry.getCreatedAt(), entry.getUpdatedAt());
    }
}
