package com.sawli.shortlinkx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity class representing the mapping between a shortened code and its original URL.
 * Mapped to the "url_mappings" database table.
 */
@Entity
@Table(name = "url_mappings", indexes = {
    @Index(name = "idx_short_code", columnList = "short_code", unique = true),
    @Index(name = "idx_custom_alias", columnList = "custom_alias", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "custom_alias", unique = true, length = 20)
    private String customAlias;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Checks if this URL mapping has expired.
     *
     * @return true if the current time is past the expiration time, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Set creation and update timestamps before inserting a new record.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.clickCount == null) {
            this.clickCount = 0L;
        }
    }

    /**
     * Set update timestamp before modifying an existing record.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
