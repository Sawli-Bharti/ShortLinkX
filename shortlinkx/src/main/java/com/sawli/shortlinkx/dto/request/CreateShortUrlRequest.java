package com.sawli.shortlinkx.dto.request;

import com.sawli.shortlinkx.constants.ApplicationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * Request payload for creating or updating a short URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload to create or update a shortened URL")
public class CreateShortUrlRequest {

    @NotBlank(message = ApplicationConstants.ERROR_BLANK_URL)
    @URL(message = ApplicationConstants.ERROR_INVALID_URL)
    @Schema(description = "The target original long URL destination to shorten", example = "https://www.github.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String originalUrl;

    @Size(min = 3, max = 20, message = ApplicationConstants.ERROR_INVALID_ALIAS_LENGTH)
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = ApplicationConstants.ERROR_INVALID_ALIAS_FORMAT)
    @Schema(description = "Optional custom alias (3-20 characters, letters, numbers, hyphens, and underscores only)", example = "my-github-profile", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String customAlias;

    @Schema(description = "Optional expiration date and time. If omitted, link remains active permanently.", example = "2026-12-31T23:59:59", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime expiresAt;
}
