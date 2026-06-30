package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when a requested short URL code is not found in the system.
 */
public class UrlNotFoundException extends RuntimeException {

    /**
     * Constructs a new UrlNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public UrlNotFoundException(String message) {
        super(message);
    }
}
