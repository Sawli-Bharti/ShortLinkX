package com.sawli.shortlinkx.service.impl;

import com.sawli.shortlinkx.dto.response.DashboardResponse;
import com.sawli.shortlinkx.dto.response.UserProfileResponse;
import com.sawli.shortlinkx.dto.response.UserUrlResponse;
import com.sawli.shortlinkx.entity.UrlMapping;
import com.sawli.shortlinkx.entity.User;
import com.sawli.shortlinkx.repository.UrlRepository;
import com.sawli.shortlinkx.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementation for retrieving user profiles, dashboard metrics, and lists of owned URLs.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UrlRepository urlRepository;

    @Override
    @Cacheable(value = "userProfiles", key = "#currentUser.email")
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User currentUser) {
        long totalUrls = urlRepository.countByUser(currentUser);
        long totalClicks = urlRepository.sumClickCountByUser(currentUser);

        return UserProfileResponse.builder()
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .totalUrls(totalUrls)
                .totalClicks(totalClicks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserUrlResponse> getMyUrls(
            User currentUser,
            String alias,
            String originalUrl,
            String status,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Normalize empty/blank parameters to null
        String searchAlias = (alias != null && !alias.isBlank()) ? alias : null;
        String searchOriginalUrl = (originalUrl != null && !originalUrl.isBlank()) ? originalUrl : null;
        String searchStatus = (status != null && !status.isBlank()) ? status.toUpperCase() : null;

        Page<UrlMapping> urlPage = urlRepository.searchUrls(
                currentUser,
                searchAlias,
                searchOriginalUrl,
                searchStatus,
                LocalDateTime.now(),
                pageable
        );

        return urlPage.map(this::mapToUserUrlResponse);
    }

    @Override
    @Cacheable(value = "userDashboards", key = "#currentUser.email")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User currentUser) {
        long totalUrls = urlRepository.countByUser(currentUser);
        long totalClicks = urlRepository.sumClickCountByUser(currentUser);
        LocalDateTime now = LocalDateTime.now();

        long activeUrls = urlRepository.countActiveUrlsByUser(currentUser, now);
        long expiredUrls = urlRepository.countExpiredUrlsByUser(currentUser, now);

        // Find the top clicked URL mapping
        Page<UrlMapping> topClicked = urlRepository.findByUser(
                currentUser,
                PageRequest.of(0, 1, Sort.by("clickCount").descending())
        );
        String mostClickedUrl = topClicked.hasContent() ? 
                topClicked.getContent().get(0).getOriginalUrl() : "N/A";

        // Find 5 most recent URLs
        Page<UrlMapping> recentPage = urlRepository.findByUser(
                currentUser,
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        );
        List<UserUrlResponse> recentUrls = recentPage.getContent().stream()
                .map(this::mapToUserUrlResponse)
                .toList();

        return DashboardResponse.builder()
                .totalUrls(totalUrls)
                .activeUrls(activeUrls)
                .expiredUrls(expiredUrls)
                .totalClicks(totalClicks)
                .mostClickedUrl(mostClickedUrl)
                .recentUrls(recentUrls)
                .build();
    }

    private UserUrlResponse mapToUserUrlResponse(UrlMapping mapping) {
        String displayIdentifier = mapping.getCustomAlias() != null ? 
                mapping.getCustomAlias() : mapping.getShortCode();
        
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{identifier}")
                .buildAndExpand(displayIdentifier)
                .toUriString();

        return UserUrlResponse.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .customAlias(mapping.getCustomAlias())
                .shortUrl(shortUrl)
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .expiresAt(mapping.getExpiresAt())
                .isExpired(mapping.isExpired())
                .build();
    }
}
