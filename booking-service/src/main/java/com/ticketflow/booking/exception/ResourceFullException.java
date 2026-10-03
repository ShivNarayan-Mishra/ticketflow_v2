package com.ticketflow.booking.exception;

public class ResourceFullException extends RuntimeException {
    public ResourceFullException(String message) {
        super(message);
    }
}