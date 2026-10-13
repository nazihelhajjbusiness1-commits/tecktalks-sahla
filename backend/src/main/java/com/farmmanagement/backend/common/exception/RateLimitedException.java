package com.farmmanagement.backend.common.exception;

/**
 * Raised when an account is temporarily locked out after too many failed login
 * attempts. Mapped to HTTP 429 with a {@code Retry-After} header so clients can
 * back off instead of hammering the login endpoint.
 */
public class RateLimitedException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitedException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
