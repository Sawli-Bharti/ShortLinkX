package com.sawli.shortlinkx.service;

import com.sawli.shortlinkx.dto.request.CreateShortUrlRequest;
import com.sawli.shortlinkx.dto.response.CreateShortUrlResponse;
import com.sawli.shortlinkx.dto.response.UrlDetailsResponse;

/**
 * Service interface defining business logic operations for the URL Shortener application.
 */
public interface UrlService {

    /**
     * Shortens a long URL and saves the mapping in the database.
     *
     * @param request the payload containing the original URL to shorten
     * @return the created short URL response payload
     */
    CreateShortUrlResponse createShortUrl(CreateShortUrlRequest request);

    /**
     * Retrieves the original URL corresponding to a shortened code.
     *
     * @param shortCode the 6-8 alphanumeric code
     * @return the original URL string
     */
    String getOriginalUrl(String shortCode);

    /**
     * Retrieves metadata details and click analytics for a given short code.
     *
     * @param shortCode the 6-8 alphanumeric code
     * @return detailed URL mapping information
     */
    UrlDetailsResponse getUrlDetails(String shortCode);

    /**
     * Increments the click analytics count for a shortened URL.
     *
     * @param shortCode the 6-8 alphanumeric code
     */
    void incrementClickCount(String shortCode);

    /**
     * Updates an existing shortened URL.
     *
     * @param shortCode the current short code or custom alias identifying the mapping
     * @param request the payload containing updated mapping information
     */
    void updateShortUrl(String shortCode, CreateShortUrlRequest request);

    /**
     * Deletes an existing shortened URL.
     *
     * @param shortCode the short code or custom alias identifying the mapping to delete
     */
    void deleteShortUrl(String shortCode);
}
