package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when a provided URL has an invalid format or fails validation.
 */
public class InvalidUrlException extends RuntimeException {

    /**
     * Constructs a new InvalidUrlException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidUrlException(String message) {
        super(message);
    }
}
