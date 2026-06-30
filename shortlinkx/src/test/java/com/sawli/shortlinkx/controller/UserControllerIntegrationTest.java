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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        urlRepository.deleteAll();
        userRepository.deleteAll();

        // Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .fullName("Sawli Bharti")
                .email("abc@gmail.com")
                .password("Password@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("abc@gmail.com")
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
    void shouldReturnUserProfileStatistics() throws Exception {
        // Create 2 URLs for the user to verify stats
        CreateShortUrlRequest urlRequest1 = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google")
                .build();
        CreateShortUrlRequest urlRequest2 = CreateShortUrlRequest.builder()
                .originalUrl("https://www.yahoo.com")
                .customAlias("yahoo")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest2)))
                .andExpect(status().isCreated());

        // Call redirection to increment click counts
        mockMvc.perform(get("/google")).andExpect(status().isFound());

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.fullName", is("Sawli Bharti")))
                .andExpect(jsonPath("$.data.email", is("abc@gmail.com")))
                .andExpect(jsonPath("$.data.totalUrls", is(2)))
                .andExpect(jsonPath("$.data.totalClicks", is(1)));
    }

    @Test
    void shouldReturnMyUrlsPaginatedAndSorted() throws Exception {
        // Create some URLs
        for (int i = 1; i <= 5; i++) {
            CreateShortUrlRequest urlRequest = CreateShortUrlRequest.builder()
                    .originalUrl("https://url" + i + ".com")
                    .customAlias("alias-" + i)
                    .build();

            mockMvc.perform(post("/api/v1/urls")
                            .header("Authorization", jwtToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(urlRequest)))
                    .andExpect(status().isCreated());
        }

        // Get paginated (size 3)
        mockMvc.perform(get("/api/v1/users/me/urls")
                        .header("Authorization", jwtToken)
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements", is(5)));

        // Search by alias
        mockMvc.perform(get("/api/v1/users/me/urls")
                        .header("Authorization", jwtToken)
                        .param("alias", "alias-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].originalUrl", is("https://url3.com")));

        // Search by originalUrl case-insensitive
        mockMvc.perform(get("/api/v1/users/me/urls")
                        .header("Authorization", jwtToken)
                        .param("originalUrl", "URL4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].customAlias", is("alias-4")));

        // Search by status ACTIVE
        mockMvc.perform(get("/api/v1/users/me/urls")
                        .header("Authorization", jwtToken)
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(5)));
    }

    @Test
    void shouldReturnUserDashboardMetrics() throws Exception {
        // Create a URL
        CreateShortUrlRequest urlRequest = CreateShortUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google-dash")
                .build();

        mockMvc.perform(post("/api/v1/urls")
                        .header("Authorization", jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest)))
                .andExpect(status().isCreated());

        // Call dashboard endpoint
        mockMvc.perform(get("/api/v1/users/me/dashboard")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalUrls", is(1)))
                .andExpect(jsonPath("$.data.activeUrls", is(1)))
                .andExpect(jsonPath("$.data.expiredUrls", is(0)))
                .andExpect(jsonPath("$.data.recentUrls", hasSize(1)));
    }
}
