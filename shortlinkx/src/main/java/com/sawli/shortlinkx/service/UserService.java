package com.sawli.shortlinkx.service;

import com.sawli.shortlinkx.dto.response.DashboardResponse;
import com.sawli.shortlinkx.dto.response.UserUrlResponse;
import com.sawli.shortlinkx.entity.User;
import org.springframework.data.domain.Page;

/**
 * Service interface for retrieving user details and user-specific URL lists.
 */
public interface UserService {

    /**
     * Retrieves the profile statistics for the current user.
     */
    com.sawli.shortlinkx.dto.response.UserProfileResponse getProfile(User currentUser);

    /**
     * Retrieves a paginated list of URLs owned by the current user, optionally filtered by alias, originalUrl, and status.
     */
    Page<UserUrlResponse> getMyUrls(
            User currentUser,
            String alias,
            String originalUrl,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir
    );

    /**
     * Retrieves dashboard metrics for the current user.
     */
    DashboardResponse getDashboard(User currentUser);
}
