package com.investrocket.exception;

public class InsufficientHoldingsException extends RuntimeException {

    public InsufficientHoldingsException() {
        super("Insufficient holdings for this sell order");
    }
}
