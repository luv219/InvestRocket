package com.investrocket.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddWatchlistRequest(
        @NotBlank(message = "Stock symbol is required")
        @Pattern(
                regexp = "^[A-Za-z0-9.-]{1,15}$",
                message = "Stock symbol is invalid")
        String symbol) {
}
