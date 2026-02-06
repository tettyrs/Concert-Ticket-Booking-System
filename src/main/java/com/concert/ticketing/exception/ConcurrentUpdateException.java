package com.concert.ticketing.exception;

public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(String message) {
        super(message);
    }

    public ConcurrentUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
