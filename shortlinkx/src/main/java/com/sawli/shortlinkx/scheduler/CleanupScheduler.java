package com.sawli.shortlinkx.scheduler;

import com.sawli.shortlinkx.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Cleanup scheduler automating deletion of expired URLs and logging execution metrics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final UrlRepository urlRepository;

    /**
     * Cleans up expired URL records once every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredUrls() {
        log.info("Starting expired URL cleanup job...");
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();

        int deletedCount = urlRepository.deleteByExpiresAtBefore(now);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Expired URL cleanup completed. Deleted: {} URLs. Execution time: {} ms", deletedCount, duration);
    }
}
