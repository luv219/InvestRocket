package com.investrocket.exception;

public class PriceAlertNotFoundException extends RuntimeException {
    public PriceAlertNotFoundException() {
        super("Price alert not found");
    }
}
