package com.sawli.shortlinkx.controller;

import com.sawli.shortlinkx.dto.request.CreateShortUrlRequest;
import com.sawli.shortlinkx.dto.response.ApiResponse;
import com.sawli.shortlinkx.dto.response.CreateShortUrlResponse;
import com.sawli.shortlinkx.dto.response.UrlDetailsResponse;
import com.sawli.shortlinkx.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller handling URL creation, details viewing, updates, deletion, and redirection routing.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "2. URL Management", description = "API endpoints for creating, updating, deleting, viewing, and resolving short URLs")
public class UrlController {

    private final UrlService urlService;

    /**
     * Endpoint to shorten a URL.
     * Route: POST /api/v1/urls
     */
    @PostMapping("/api/v1/urls")
    @Operation(summary = "Create short URL", description = "Shortens an original URL. Optionally accepts a custom alias and an expiration date.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Short URL created successfully",
            content = @Content(schema = @Schema(implementation = CreateShortUrlResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (invalid URL format, or alias contains invalid characters)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Alias conflict - custom alias already in use")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Rate limit exceeded (exceeded 100 links per user per hour)")
    public ResponseEntity<ApiResponse<CreateShortUrlResponse>> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request
    ) {
        CreateShortUrlResponse response = urlService.createShortUrl(request);
        ApiResponse<CreateShortUrlResponse> body = ApiResponse.success(response, "Short URL created successfully.");
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    /**
     * Endpoint to resolve a short URL and redirect to the original destination.
     * Route: GET /{shortCode}
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to Original URL", description = "Resolves the short code or custom alias, increments clicks, and redirects the client with HTTP 302.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Temporary redirect to original destination URL",
            headers = @Header(name = HttpHeaders.LOCATION, description = "The target original URL destination"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Short URL not found or does not exist")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "410", description = "Short URL has expired and is no longer available")
    public void redirectShortUrl(
            @Parameter(description = "Short code or custom alias identifier") @PathVariable String shortCode,
            HttpServletResponse response
    ) throws IOException {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        urlService.incrementClickCount(shortCode);

        response.setHeader(HttpHeaders.LOCATION, originalUrl);
        response.setStatus(HttpServletResponse.SC_FOUND);
    }

    /**
     * Endpoint to get details about a shortened URL.
     * Route: GET /api/v1/urls/{shortCode}
     */
    @GetMapping("/api/v1/urls/{shortCode}")
    @Operation(summary = "Get URL Details", description = "Retrieves information (original URL, click count, timestamps, expiration status) for a specific short code.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved details",
            content = @Content(schema = @Schema(implementation = UrlDetailsResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is invalid or expired")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - URL is owned by another user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    public ResponseEntity<ApiResponse<UrlDetailsResponse>> getUrlDetails(
            @Parameter(description = "Short code or custom alias identifier") @PathVariable String shortCode
    ) {
        UrlDetailsResponse response = urlService.getUrlDetails(shortCode);
        ApiResponse<UrlDetailsResponse> body = ApiResponse.success(response, "URL details retrieved successfully.");
        return ResponseEntity.ok(body);
    }

    /**
     * Endpoint to update destination or expiration date of a shortened URL.
     * Route: PUT /api/v1/urls/{shortCode}
     */
    @PutMapping("/api/v1/urls/{shortCode}")
    @Operation(summary = "Update short URL", description = "Allows updating the original destination URL, custom alias, or expiration date of a short URL.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - URL is owned by another user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Alias conflict - new alias already in use")
    public ResponseEntity<ApiResponse<Void>> updateShortUrl(
            @Parameter(description = "Short code or custom alias identifier") @PathVariable String shortCode,
            @Valid @RequestBody CreateShortUrlRequest request
    ) {
        urlService.updateShortUrl(shortCode, request);
        ApiResponse<Void> body = ApiResponse.success(null, "Short URL updated successfully.");
        return ResponseEntity.ok(body);
    }

    /**
     * Endpoint to delete a shortened URL.
     * Route: DELETE /api/v1/urls/{shortCode}
     */
    @DeleteMapping("/api/v1/urls/{shortCode}")
    @Operation(summary = "Delete short URL", description = "Deletes a short URL record from the system, evicting all caches.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully deleted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - URL is owned by another user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "URL not found")
    public ResponseEntity<ApiResponse<Void>> deleteShortUrl(
            @Parameter(description = "Short code or custom alias identifier") @PathVariable String shortCode
    ) {
        urlService.deleteShortUrl(shortCode);
        ApiResponse<Void> body = ApiResponse.success(null, "Short URL deleted successfully.");
        return ResponseEntity.ok(body);
    }
}
