package com.sawli.shortlinkx.controller;

import com.sawli.shortlinkx.dto.response.ApiResponse;
import com.sawli.shortlinkx.dto.response.DashboardResponse;
import com.sawli.shortlinkx.dto.response.UserProfileResponse;
import com.sawli.shortlinkx.dto.response.UserUrlResponse;
import com.sawli.shortlinkx.entity.User;
import com.sawli.shortlinkx.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling current logged-in user analytics, dashboard metrics, and owned URLs.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "3. User", description = "API endpoints for user profiles and links lookup (dashboard excluded)")
public class UserController {

    private final UserService userService;

    /**
     * Endpoint to fetch the current user profile statistics.
     * Route: GET /api/v1/users/me
     */
    @GetMapping("/me")
    @Operation(summary = "Get user profile", description = "Retrieves profile statistics for the authenticated user (name, email, totals).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved profile statistics",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is invalid or expired")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser
    ) {
        UserProfileResponse profile = userService.getProfile(currentUser);
        ApiResponse<UserProfileResponse> body = ApiResponse.success(profile, "User profile retrieved successfully.");
        return ResponseEntity.ok(body);
    }

    /**
     * Endpoint to fetch paginated/sorted URLs owned by the current user, with optional search filters.
     * Route: GET /api/v1/users/me/urls
     */
    @GetMapping("/me/urls")
    @Operation(summary = "Get owned URLs", description = "Retrieves a paginated list of URLs owned by the current user. Supports alias, original URL, and status filters.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved URLs",
            content = @Content(schema = @Schema(implementation = Page.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is invalid or expired")
    public ResponseEntity<ApiResponse<Page<UserUrlResponse>>> getMyUrls(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Filter by custom alias pattern") @RequestParam(required = false) String alias,
            @Parameter(description = "Filter by original URL pattern") @RequestParam(required = false) String originalUrl,
            @Parameter(description = "Filter by status: ACTIVE or EXPIRED", schema = @Schema(allowableValues = {"ACTIVE", "EXPIRED"})) @RequestParam(required = false) String status,
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page to return") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field sorting parameter") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direction of sort: asc or desc", schema = @Schema(allowableValues = {"asc", "desc"})) @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<UserUrlResponse> data = userService.getMyUrls(currentUser, alias, originalUrl, status, page, size, sortBy, sortDir);
        ApiResponse<Page<UserUrlResponse>> body = ApiResponse.success(data, "User URLs retrieved successfully.");
        return ResponseEntity.ok(body);
    }

    /**
     * Endpoint to fetch current user's dashboard performance metrics.
     * Route: GET /api/v1/users/me/dashboard
     */
    @GetMapping("/me/dashboard")
    @Tag(name = "4. Dashboard", description = "API endpoints for user click and url distribution statistics")
    @Operation(summary = "Get user dashboard metrics", description = "Retrieves total, active, expired URL metrics, click totals, most clicked original URL, and 5 recent URLs.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard statistics",
            content = @Content(schema = @Schema(implementation = DashboardResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT token is invalid or expired")
    public ResponseEntity<ApiResponse<DashboardResponse>> getMyDashboard(
            @AuthenticationPrincipal User currentUser
    ) {
        DashboardResponse data = userService.getDashboard(currentUser);
        ApiResponse<DashboardResponse> body = ApiResponse.success(data, "User dashboard retrieved successfully.");
        return ResponseEntity.ok(body);
    }
}
