package com.investrocket.exception;

public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(String symbol) {
        super("No market data found for symbol " + symbol);
    }
}
