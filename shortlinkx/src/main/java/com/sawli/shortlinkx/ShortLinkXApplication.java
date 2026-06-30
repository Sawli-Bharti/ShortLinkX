package com.sawli.shortlinkx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the ShortLinkX URL Shortener Spring Boot Application.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class ShortLinkXApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkXApplication.class, args);
    }
}
