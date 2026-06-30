package com.sawli.shortlinkx.repository;

import com.sawli.shortlinkx.entity.UrlMapping;
import com.sawli.shortlinkx.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for performing database operations
 * on the {@link UrlMapping} entity.
 */
@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Finds a URL mapping by its unique short code.
     *
     * @param shortCode the 6-8 alphanumeric code
     * @return an {@link Optional} containing the found {@link UrlMapping}, or empty if not found
     */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * Checks if a URL mapping already exists for the specified short code.
     *
     * @param shortCode the 6-8 alphanumeric code
     * @return true if the short code already exists, false otherwise
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Finds a URL mapping by its unique custom alias.
     *
     * @param customAlias the custom alias
     * @return an {@link Optional} containing the found {@link UrlMapping}, or empty if not found
     */
    Optional<UrlMapping> findByCustomAlias(String customAlias);

    /**
     * Checks if a URL mapping already exists for the specified custom alias.
     *
     * @param customAlias the custom alias
     * @return true if the custom alias already exists, false otherwise
     */
    boolean existsByCustomAlias(String customAlias);

    /**
     * Checks if a URL mapping exists with either the short code or the custom alias.
     */
    boolean existsByShortCodeOrCustomAlias(String shortCode, String customAlias);

    /**
     * Finds a URL mapping by short code or custom alias.
     */
    Optional<UrlMapping> findByShortCodeOrCustomAlias(String shortCode, String customAlias);

    /**
     * Finds all active URL mappings (not expired).
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NULL OR u.expiresAt > :now")
    List<UrlMapping> findActiveUrls(@Param("now") LocalDateTime now);

    /**
     * Finds all expired URL mappings.
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.expiresAt IS NOT NULL AND u.expiresAt <= :now")
    List<UrlMapping> findExpiredUrls(@Param("now") LocalDateTime now);

    /**
     * Sums all clicks across all URLs created by the specified user.
     */
    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM UrlMapping u WHERE u.user = :user")
    Long sumClickCountByUser(@Param("user") User user);

    /**
     * Counts the total number of URLs created by the specified user.
     */
    Long countByUser(User user);

    /**
     * Finds paginated URLs created by a user, optionally filtered by custom alias.
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.user = :user AND (:alias IS NULL OR u.customAlias LIKE %:alias%)")
    Page<UrlMapping> findByUserAndAlias(@Param("user") User user, @Param("alias") String alias, Pageable pageable);

    /**
     * Finds all URLs created by a user, paginated.
     */
    Page<UrlMapping> findByUser(User user, Pageable pageable);

    /**
     * Counts active URLs for a user (not expired).
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.user = :user AND (u.expiresAt IS NULL OR u.expiresAt > :now)")
    long countActiveUrlsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Counts expired URLs for a user.
     */
    @Query("SELECT COUNT(u) FROM UrlMapping u WHERE u.user = :user AND u.expiresAt IS NOT NULL AND u.expiresAt <= :now")
    long countExpiredUrlsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Deletes all URLs that expired before the given timestamp.
     * Returns the count of deleted items.
     */
    int deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * Performs a case-insensitive search with optional filters for alias, originalUrl, and active/expired status.
     */
    @Query("SELECT u FROM UrlMapping u WHERE u.user = :user " +
           "AND (:alias IS NULL OR LOWER(u.customAlias) LIKE LOWER(CONCAT('%', :alias, '%'))) " +
           "AND (:originalUrl IS NULL OR LOWER(u.originalUrl) LIKE LOWER(CONCAT('%', :originalUrl, '%'))) " +
           "AND (:status IS NULL " +
           "     OR (:status = 'ACTIVE' AND (u.expiresAt IS NULL OR u.expiresAt > :now)) " +
           "     OR (:status = 'EXPIRED' AND (u.expiresAt IS NOT NULL AND u.expiresAt <= :now)))")
    Page<UrlMapping> searchUrls(
            @Param("user") User user,
            @Param("alias") String alias,
            @Param("originalUrl") String originalUrl,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
