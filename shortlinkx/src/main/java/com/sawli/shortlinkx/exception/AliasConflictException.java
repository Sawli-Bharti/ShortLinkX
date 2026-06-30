package com.sawli.shortlinkx.exception;

/**
 * Exception thrown when a requested custom alias is already in use (HTTP 409 Conflict).
 */
public class AliasConflictException extends RuntimeException {
    public AliasConflictException(String message) {
        super(message);
    }
}
