package com.investrocket.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException() {
        super("Insufficient virtual cash balance");
    }
}
