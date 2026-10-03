package com.ticketflow.exception;

public class ClaimConflictException extends RuntimeException {
    public ClaimConflictException(String message) {
        super(message);
    }
}
