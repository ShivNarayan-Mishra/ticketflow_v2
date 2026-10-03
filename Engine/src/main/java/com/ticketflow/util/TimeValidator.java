package com.ticketflow.util;

import java.time.LocalDateTime;

public class TimeValidator {

    private TimeValidator() {
        // static-only class, never instantiated
    }

    // True if a claim starting at startTime can still be cancelled right now
    public static boolean isCancellable(LocalDateTime startTime) {
        return LocalDateTime.now().isBefore(startTime);
    }
}