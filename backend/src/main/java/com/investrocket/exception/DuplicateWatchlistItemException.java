package com.investrocket.exception;

public class DuplicateWatchlistItemException extends RuntimeException {

    public DuplicateWatchlistItemException(String symbol) {
        super(symbol + " is already in your watchlist");
    }
}
