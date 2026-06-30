package com.sawli.shortlinkx.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sawli.shortlinkx.dto.request.CreateShortUrlRequest;
import com.sawli.shortlinkx.dto.request.LoginRequest;
import com.sawli.shortlinkx.dto.request.RegisterRequest;
import com.sawli.shortlinkx.dto.response.AuthenticationResponse;
import com.sawli.shortlinkx.repository.UrlRepository;
import com.sawli.shortlinkx.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UrlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        urlRepository.deleteAll();
        userRepository.deleteAll();

        // Register a test user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Test Owner")
                .email("owner@test.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get token
        LoginRequest loginRequest = LoginRequest.builder()
                .email("owner@test.com")
                .password("Password@123")
                .build();

        String responseJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthenticationResponse authResponse = objectMapper.readValue(responseJson, AuthenticationResponse.class);
        this.jwtToken = "Bearer " + authResponse.getToken();
    }

    @Test
    void shouldCreateShortUrlWithCustomAliasSuccessfully() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-custom")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.originalUrl", is("https://www.google.com")))
                .andExpect(jsonPath("$.data.shortUrl", containsString("/google-custom")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()));
    }

    @Test
    void shouldReturnConflictWhenCustomAliasAlreadyExists() throws Exception {
        // Create first one
        CreateShortUrlRequest request1 = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-custom")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try creating second one with same alias
        CreateShortUrlRequest request2 = CreateShortUrlRequest.builder()
                .originalUrl("https://www.yahoo.com")
                .customAlias("google-custom")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    void shouldReturnBadRequestWhenCustomAliasIsInvalid() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("ab") // Too short
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldRedirectSuccessfullyWithoutAuthentication() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-redirect")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Redirection is a public route, should not need authorization header
        mockMvc.perform(get("/google-redirect"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }

    @Test
    void shouldReturnGoneForExpiredUrl() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("expired-alias")
                .expiresAt(LocalDateTime.now().plusSeconds(2))
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Force set expiration to past
        urlRepository.findByCustomAlias("expired-alias").ifPresent(mapping -> {
            mapping.setExpiresAt(LocalDateTime.now().minusMinutes(5));
            urlRepository.save(mapping);
        });

        mockMvc.perform(get("/expired-alias"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void shouldReturnDetailsForOwnerOnly() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-details")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Success for owner
        mockMvc.perform(get("/api/v1/urls/google-details")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.originalUrl", is("https://www.google.com")))
                .andExpect(jsonPath("$.data.isExpired", is(false)));

        // Failure for unauthenticated/other user
        mockMvc.perform(get("/api/v1/urls/google-details"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldUpdateShortUrlForOwnerOnly() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-update")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        CreateShortUrlRequest updateRequest = CreateShortUrlRequest.builder()
                .originalUrl("https://www.bing.com")
                .customAlias("google-upd-changed")
                .build();

        mockMvc.perform(put("/api/v1/urls/google-update")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify redirection points to updated URL
        mockMvc.perform(get("/google-upd-changed"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.bing.com"));
    }

    @Test
    void shouldDeleteShortUrlForOwnerOnly() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-delete")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/urls/google-delete")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());

        // Verify lookup fails now
        mockMvc.perform(get("/google-delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldEnforceRateLimiting() throws Exception {
        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .build();

        // Send 100 requests (which should succeed)
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/v1/urls")
                            .header("Authorization", jwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // The 101st request should be rate limited (HTTP 429)
        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Rate limit exceeded")));
    }
}
