package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when trying to access or redirect an expired short URL (HTTP 410 Gone).
 */
public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String message) {
        super(message);
    }
}
