package com.investrocket.journal.dto;

import java.util.UUID;

import com.investrocket.journal.JournalMood;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJournalEntryRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,
        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content must be at most 5000 characters")
        String content,
        JournalMood mood,
        @Size(max = 150, message = "Strategy must be at most 150 characters")
        String strategy,
        @Size(max = 15, message = "Symbol must be at most 15 characters")
        String symbol,
        UUID orderId,
        UUID tradeId,
        @Size(max = 500, message = "Tags must be at most 500 characters")
        String tags) {
}
