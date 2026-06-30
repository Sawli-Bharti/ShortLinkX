package com.sawli.shortlinkx.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload containing profile details and user click/link count metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing the authenticated user profile information")
public class UserProfileResponse {

    @Schema(description = "User's full name", example = "John Doe")
    private String fullName;

    @Schema(description = "User's registered email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Total count of shortened URLs created by this user", example = "25")
    private long totalUrls;

    @Schema(description = "Total clicks compiled across all links owned by this user", example = "142")
    private long totalClicks;
}
