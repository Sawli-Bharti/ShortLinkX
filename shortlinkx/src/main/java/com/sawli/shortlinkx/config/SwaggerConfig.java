package com.sawli.shortlinkx.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for API documentation (Swagger/OpenAPI 3).
 * Maps title, description, version, security constraints, and grouped routes.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the overall OpenAPI configuration and metadata, adding JWT security headers.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShortLinkX URL Shortener API")
                        .description("SaaS Multi-User URL Shortener REST API with Redis Caching, Rate Limiting, and Click Analytics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ShortLinkX Support")
                                .email("support@shortlinkx.com")))
                .addSecurityItem(new SecurityRequirement().addList("JWT Token"))
                .components(new Components()
                        .addSecuritySchemes("JWT Token", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Enter JWT Bearer token in the format 'Bearer <token>'")));
    }

    /**
     * Group APIs for Authentication.
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1. Authentication")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    /**
     * Group APIs for URL Management.
     */
    @Bean
    public GroupedOpenApi urlApi() {
        return GroupedOpenApi.builder()
                .group("2. URL Management")
                .pathsToMatch("/api/v1/urls/**", "/{shortCode}")
                .build();
    }

    /**
     * Group APIs for User Management.
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("3. User")
                .pathsToMatch("/api/v1/users/**")
                .pathsToExclude("/api/v1/users/me/dashboard")
                .build();
    }

    /**
     * Group APIs for Dashboard Metrics.
     */
    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder()
                .group("4. Dashboard")
                .pathsToMatch("/api/v1/users/me/dashboard")
                .build();
    }
}
