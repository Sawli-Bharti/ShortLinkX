package com.sawli.shortlinkx.service.impl;

import com.sawli.shortlinkx.constants.ApplicationConstants;
import com.sawli.shortlinkx.dto.request.CreateShortUrlRequest;
import com.sawli.shortlinkx.dto.response.CreateShortUrlResponse;
import com.sawli.shortlinkx.dto.response.UrlDetailsResponse;
import com.sawli.shortlinkx.entity.UrlMapping;
import com.sawli.shortlinkx.entity.User;
import com.sawli.shortlinkx.exception.AliasConflictException;
import com.sawli.shortlinkx.exception.UrlExpiredException;
import com.sawli.shortlinkx.exception.UrlNotFoundException;
import com.sawli.shortlinkx.repository.UrlRepository;
import com.sawli.shortlinkx.repository.UserRepository;
import com.sawli.shortlinkx.service.UrlService;
import com.sawli.shortlinkx.util.RateLimiter;
import com.sawli.shortlinkx.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

/**
 * Service implementation containing business logic operations for URL shortening,
 * redirection, caching, rate limiting, and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final RateLimiter rateLimiter;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public CreateShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        // Enforce rate limit (100 URL creations per hour)
        rateLimiter.checkRateLimit(currentUser.getId());

        // Validate custom alias if provided
        String customAlias = request.getCustomAlias();
        if (customAlias != null && !customAlias.isBlank()) {
            if (urlRepository.existsByShortCodeOrCustomAlias(customAlias, customAlias)) {
                throw new AliasConflictException(ApplicationConstants.ERROR_ALIAS_CONFLICT);
            }
        } else {
            customAlias = null; // Normalize empty string to null
        }

        // Generate unique short code
        String shortCode;
        do {
            shortCode = shortCodeGenerator.generateCode();
        } while (urlRepository.existsByShortCodeOrCustomAlias(shortCode, shortCode));

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .customAlias(customAlias)
                .expiresAt(request.getExpiresAt())
                .user(currentUser)
                .build();

        UrlMapping savedMapping = urlRepository.save(mapping);

        String displayIdentifier = savedMapping.getCustomAlias() != null ? 
                savedMapping.getCustomAlias() : savedMapping.getShortCode();
        String shortUrl = buildShortUrl(displayIdentifier);

        // Evict user profiles & dashboards to recalculate total counts
        evictUserProfileCaches(currentUser.getEmail());

        log.info("Short URL created successfully for user {}: {} -> {}", currentUser.getEmail(), request.getOriginalUrl(), shortUrl);

        return CreateShortUrlResponse.builder()
                .originalUrl(savedMapping.getOriginalUrl())
                .shortCode(savedMapping.getShortCode())
                .shortUrl(shortUrl)
                .createdAt(savedMapping.getCreatedAt())
                .expiresAt(savedMapping.getExpiresAt())
                .build();
    }

    @Override
    @Cacheable(value = "shortUrls", key = "#shortCode")
    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        UrlMapping mapping = findByIdentifier(shortCode);
        if (mapping.isExpired()) {
            throw new UrlExpiredException(ApplicationConstants.ERROR_URL_EXPIRED);
        }
        log.info("Redirecting short code {} to original URL {}", shortCode, mapping.getOriginalUrl());
        return mapping.getOriginalUrl();
    }

    @Override
    @Cacheable(value = "urlDetails", key = "#shortCode")
    @Transactional(readOnly = true)
    public UrlDetailsResponse getUrlDetails(String shortCode) {
        UrlMapping mapping = findByIdentifier(shortCode);
        User currentUser = getCurrentAuthenticatedUser();
        verifyOwnership(mapping, currentUser);

        String displayIdentifier = mapping.getCustomAlias() != null ? 
                mapping.getCustomAlias() : mapping.getShortCode();
        String shortUrl = buildShortUrl(displayIdentifier);

        log.info("Fetching URL details for short code {}", shortCode);

        return UrlDetailsResponse.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(shortUrl)
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .lastAccessedAt(mapping.getLastAccessedAt())
                .expiresAt(mapping.getExpiresAt())
                .isExpired(mapping.isExpired())
                .build();
    }

    @Override
    @Transactional
    public void incrementClickCount(String shortCode) {
        urlRepository.findByShortCode(shortCode)
                .or(() -> urlRepository.findByCustomAlias(shortCode))
                .ifPresent(mapping -> {
                    mapping.setClickCount(mapping.getClickCount() + 1);
                    mapping.setLastAccessedAt(LocalDateTime.now());
                    urlRepository.save(mapping);

                    // Evict cache to refresh details and clicks count
                    evictUrlCaches(mapping);
                    evictUserProfileCaches(mapping.getUser().getEmail());
                });
    }

    @Override
    @Transactional
    public void updateShortUrl(String shortCode, CreateShortUrlRequest request) {
        UrlMapping mapping = findByIdentifier(shortCode);
        User currentUser = getCurrentAuthenticatedUser();
        verifyOwnership(mapping, currentUser);

        // Record old values for cache eviction
        String oldShortCode = mapping.getShortCode();
        String oldCustomAlias = mapping.getCustomAlias();

        // If custom alias is updated, check for collision
        String newAlias = request.getCustomAlias();
        if (newAlias != null && !newAlias.isBlank()) {
            if (!newAlias.equals(mapping.getCustomAlias())) {
                if (urlRepository.existsByShortCodeOrCustomAlias(newAlias, newAlias)) {
                    throw new AliasConflictException(ApplicationConstants.ERROR_ALIAS_CONFLICT);
                }
                mapping.setCustomAlias(newAlias);
            }
        } else {
            mapping.setCustomAlias(null); // Clear custom alias if set to empty
        }

        mapping.setOriginalUrl(request.getOriginalUrl());
        mapping.setExpiresAt(request.getExpiresAt());

        urlRepository.save(mapping);

        // Evict old cache keys
        evictKeyFromCache("shortUrls", oldShortCode);
        evictKeyFromCache("urlDetails", oldShortCode);
        if (oldCustomAlias != null) {
            evictKeyFromCache("shortUrls", oldCustomAlias);
            evictKeyFromCache("urlDetails", oldCustomAlias);
        }

        // Evict new cache keys as well
        evictUrlCaches(mapping);
        evictUserProfileCaches(currentUser.getEmail());

        log.info("Short URL updated successfully for user {}: {}", currentUser.getEmail(), shortCode);
    }

    @Override
    @Transactional
    public void deleteShortUrl(String shortCode) {
        UrlMapping mapping = findByIdentifier(shortCode);
        User currentUser = getCurrentAuthenticatedUser();
        verifyOwnership(mapping, currentUser);

        urlRepository.delete(mapping);

        // Evict caches
        evictUrlCaches(mapping);
        evictUserProfileCaches(currentUser.getEmail());

        log.info("Short URL deleted successfully for user {}: {}", currentUser.getEmail(), shortCode);
    }

    /**
     * Helper to find a mapping by short code or custom alias.
     */
    private UrlMapping findByIdentifier(String identifier) {
        return urlRepository.findByShortCode(identifier)
                .or(() -> urlRepository.findByCustomAlias(identifier))
                .orElseThrow(() -> new UrlNotFoundException(ApplicationConstants.ERROR_URL_NOT_FOUND));
    }

    /**
     * Helper to construct the absolute short URL.
     */
    private String buildShortUrl(String identifier) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{identifier}")
                .buildAndExpand(identifier)
                .toUriString();
    }

    /**
     * Retrieves the currently authenticated user from SecurityContext.
     */
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Asserts that the logged-in user owns the given UrlMapping.
     */
    private void verifyOwnership(UrlMapping mapping, User currentUser) {
        if (!mapping.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to access this URL.");
        }
    }

    /**
     * Evicts cached lookup and details keys for a specific UrlMapping.
     */
    private void evictUrlCaches(UrlMapping mapping) {
        if (mapping == null) return;
        evictKeyFromCache("shortUrls", mapping.getShortCode());
        evictKeyFromCache("urlDetails", mapping.getShortCode());
        if (mapping.getCustomAlias() != null) {
            evictKeyFromCache("shortUrls", mapping.getCustomAlias());
            evictKeyFromCache("urlDetails", mapping.getCustomAlias());
        }
    }

    /**
     * Evicts user profile and dashboard stats from cache.
     */
    private void evictUserProfileCaches(String email) {
        evictKeyFromCache("userProfiles", email);
        evictKeyFromCache("userDashboards", email);
    }

    /**
     * Direct eviction helper.
     */
    private void evictKeyFromCache(String cacheName, String key) {
        if (key == null) return;
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
