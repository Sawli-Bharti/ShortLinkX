package com.sawli.shortlinkx.constants;

/**
 * Global application constants.
 * Includes base API paths, URL validation regex patterns, error messages, and configuration limits.
 */
public final class ApplicationConstants {

    // Private constructor to prevent instantiation
    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility/constants class and cannot be instantiated");
    }

    /**
     * Base path for the URL Shortener REST API.
     */
    public static final String API_BASE_PATH = "/api/v1/urls";

    /**
     * Regular expression pattern for validating HTTP/HTTPS URLs.
     */
    public static final String URL_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    /**
     * Minimum length for a generated short code.
     */
    public static final int MIN_SHORT_CODE_LENGTH = 6;

    /**
     * Maximum length for a generated short code.
     */
    public static final int MAX_SHORT_CODE_LENGTH = 8;

    /**
     * Alphanumeric character pool used for generating short codes.
     */
    public static final String ALPHANUMERIC_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // --- Error Messages ---
    public static final String ERROR_URL_NOT_FOUND = "The requested short code could not be resolved.";
    public static final String ERROR_INVALID_URL = "The provided URL is invalid or malformed.";
    public static final String ERROR_BLANK_URL = "Original URL cannot be empty or blank.";
    public static final String ERROR_INTERNAL_SERVER = "An unexpected error occurred. Please try again later.";
    public static final String ERROR_VALIDATION_FAILED = "Request validation failed.";
    public static final String ERROR_INVALID_ALIAS_FORMAT = "Custom alias can only contain letters, numbers, hyphens, and underscores.";
    public static final String ERROR_INVALID_ALIAS_LENGTH = "Custom alias must be between 3 and 20 characters.";
    public static final String ERROR_ALIAS_CONFLICT = "The custom alias is already taken.";
    public static final String ERROR_INVALID_EXPIRATION = "Expiration time must be in the future.";
    public static final String ERROR_URL_EXPIRED = "The requested short URL has expired.";
}
