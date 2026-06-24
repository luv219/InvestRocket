package com.investrocket.exception;

public class InvalidAuditCategoryException extends RuntimeException {

    public InvalidAuditCategoryException() {
        super("Activity category is invalid");
    }
}
