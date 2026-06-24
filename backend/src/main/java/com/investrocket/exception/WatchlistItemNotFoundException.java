package com.investrocket.exception;

public class WatchlistItemNotFoundException extends RuntimeException {

    public WatchlistItemNotFoundException(String symbol) {
        super(symbol + " is not in your watchlist");
    }
}
