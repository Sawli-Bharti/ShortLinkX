package com.sawli.shortlinkx.exception;

import com.sawli.shortlinkx.constants.ApplicationConstants;
import com.sawli.shortlinkx.dto.response.ApiResponse;
import com.sawli.shortlinkx.exception.EmailAlreadyExistsException;
import com.sawli.shortlinkx.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler that intercepts controller exceptions and
 * maps them to consistent API responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles {@link UrlNotFoundException} and returns HTTP 404 (Not Found).
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUrlNotFound(UrlNotFoundException ex) {
        log.warn("UrlNotFoundException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link InvalidUrlException} and returns HTTP 400 (Bad Request).
     */
    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidUrl(InvalidUrlException ex) {
        log.warn("InvalidUrlException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link AliasConflictException} and returns HTTP 409 (Conflict).
     */
    @ExceptionHandler(AliasConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleAliasConflict(AliasConflictException ex) {
        log.warn("AliasConflictException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles {@link RateLimitExceededException} and returns HTTP 429 (Too Many Requests).
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("RateLimitExceededException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handles {@link UrlExpiredException} and returns HTTP 410 (Gone).
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleUrlExpired(UrlExpiredException ex) {
        log.warn("UrlExpiredException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.GONE);
    }

    /**
     * Handles {@link EmailAlreadyExistsException} and returns HTTP 409 (Conflict).
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("EmailAlreadyExistsException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles Spring Security {@link org.springframework.security.core.AuthenticationException} and returns HTTP 401 (Unauthorized).
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        log.warn("AuthenticationException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("Invalid user email or password.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles Spring Security {@link org.springframework.security.access.AccessDeniedException} and returns HTTP 403 (Forbidden).
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles validation errors from `@Valid` annotations and returns HTTP 400 (Bad Request).
     * Concatenates error messages from all failed fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", errorMessage);
        ApiResponse<Void> response = ApiResponse.error(errorMessage.isEmpty() ? ApplicationConstants.ERROR_VALIDATION_FAILED : errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback handler for any general, unhandled exceptions.
     * Returns HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Internal Server Error: ", ex);
        ApiResponse<Void> response = ApiResponse.error(ApplicationConstants.ERROR_INTERNAL_SERVER);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
