package com.investrocket.exception;

public class UnsupportedOrderTypeException extends RuntimeException {

    public UnsupportedOrderTypeException() {
        super("Only MARKET orders are supported in Phase 3.");
    }
}
