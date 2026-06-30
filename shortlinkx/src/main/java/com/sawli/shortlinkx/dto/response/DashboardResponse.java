package com.sawli.shortlinkx.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response payload containing dashboard statistics and recent URLs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload containing metrics and links distribution statistics")
public class DashboardResponse {

    @Schema(description = "Total shortened URLs created by this user", example = "25")
    private long totalUrls;

    @Schema(description = "Total active (non-expired) URLs owned by this user", example = "23")
    private long activeUrls;

    @Schema(description = "Total expired URLs owned by this user", example = "2")
    private long expiredUrls;

    @Schema(description = "Total clicks compiled across all links owned by this user", example = "142")
    private long totalClicks;

    @Schema(description = "The target original destination URL that has received the most clicks", example = "https://www.github.com")
    private String mostClickedUrl;

    @Schema(description = "List of up to 5 recently created links by this user")
    private List<UserUrlResponse> recentUrls;
}
