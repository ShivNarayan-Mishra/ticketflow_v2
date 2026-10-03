package com.ticketflow.exception;

public class CancellationWindowClosedException extends RuntimeException {
    public CancellationWindowClosedException(String message) {
        super(message);
    }
}
