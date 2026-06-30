package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when a user exceeds the URL creation rate limit (HTTP 429 Too Many Requests).
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
