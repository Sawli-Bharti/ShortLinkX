package com.sawli.shortlinkx.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload containing individual URL mapping statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload representing an individual short link metadata")
public class UserUrlResponse {

    @Schema(description = "Database ID of the URL mapping", example = "1")
    private Long id;

    @Schema(description = "The target original destination URL", example = "https://www.github.com")
    private String originalUrl;

    @Schema(description = "The unique randomly generated short code key", example = "aB2cD4e")
    private String shortCode;

    @Schema(description = "The optional custom alias configured", example = "my-github-profile")
    private String customAlias;

    @Schema(description = "The absolute path short URL", example = "http://localhost:8082/my-github-profile")
    private String shortUrl;

    @Schema(description = "Total click-through redirections count for this mapping", example = "15")
    private Long clickCount;

    @Schema(description = "Creation date and time of the URL mapping")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date and time of the URL mapping")
    private LocalDateTime updatedAt;

    @Schema(description = "Expiration date and time (null if permanent)")
    private LocalDateTime expiresAt;

    @JsonProperty("isExpired")
    @Schema(description = "Flag representing whether the link has expired", example = "false")
    private boolean isExpired;
}
