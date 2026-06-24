package com.investrocket.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException() {
        super("Virtual wallet not found");
    }
}
