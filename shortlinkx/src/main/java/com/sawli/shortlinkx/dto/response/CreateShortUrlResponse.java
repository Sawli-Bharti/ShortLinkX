package com.sawli.shortlinkx.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response payload returning successful URL shortening creation details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload after successful URL shortening")
public class CreateShortUrlResponse {

    @Schema(description = "The original destination URL", example = "https://www.github.com")
    private String originalUrl;

    @Schema(description = "The unique randomly generated short code key", example = "aB2cD4e")
    private String shortCode;

    @Schema(description = "The fully qualified absolute redirection URL", example = "http://localhost:8082/aB2cD4e")
    private String shortUrl;

    @Schema(description = "Creation date and time of the shortened URL record")
    private LocalDateTime createdAt;

    @Schema(description = "Optional expiration date and time", example = "2026-12-31T23:59:59")
    private LocalDateTime expiresAt;
}
