package com.investrocket.marketdata.dto;

public record StockSearchResult(
        String symbol,
        String name,
        String exchange,
        String currency,
        String type) {
}
