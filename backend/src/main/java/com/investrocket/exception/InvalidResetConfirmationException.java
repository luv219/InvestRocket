package com.investrocket.exception;

public class InvalidResetConfirmationException extends RuntimeException {

    public InvalidResetConfirmationException() {
        super("Reset confirmation text is invalid");
    }
}
