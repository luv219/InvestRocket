package com.investrocket.exception;

public class InvalidMarketDataRequestException extends RuntimeException {

    public InvalidMarketDataRequestException(String message) {
        super(message);
    }
}
