package com.investrocket.exception;

public class JournalEntryNotFoundException extends RuntimeException {
    public JournalEntryNotFoundException() {
        super("Journal entry not found");
    }
}
