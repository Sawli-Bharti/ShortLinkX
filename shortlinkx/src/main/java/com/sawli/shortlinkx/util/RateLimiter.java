package com.sawli.shortlinkx.util;

import com.sawli.shortlinkx.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight, in-memory rate limiter using a sliding window ConcurrentHashMap.
 */
@Component
public class RateLimiter {

    private final ConcurrentHashMap<Long, List<Long>> userRequests = new ConcurrentHashMap<>();
    private static final int LIMIT = 100;
    private static final long ONE_HOUR_IN_MS = 3600000L;

    /**
     * Checks if the user is within the rate limit (100 URL creations per hour).
     * Throws {@link RateLimitExceededException} if the limit is exceeded.
     *
     * @param userId the unique identifier of the user
     */
    public void checkRateLimit(Long userId) {
        long now = System.currentTimeMillis();
        userRequests.compute(userId, (key, timestamps) -> {
            if (timestamps == null) {
                timestamps = new ArrayList<>();
            }
            // Remove timestamps older than 1 hour
            timestamps.removeIf(t -> now - t > ONE_HOUR_IN_MS);

            if (timestamps.size() >= LIMIT) {
                throw new RateLimitExceededException("Rate limit exceeded. You can only create " + LIMIT + " URLs per hour.");
            }

            timestamps.add(now);
            return timestamps;
        });
    }
}
