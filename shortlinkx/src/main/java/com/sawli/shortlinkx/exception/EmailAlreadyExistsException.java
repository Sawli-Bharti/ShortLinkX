package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when an email registration request contains a duplicate email.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
