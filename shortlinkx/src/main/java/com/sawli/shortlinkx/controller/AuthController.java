package com.sawli.shortlinkx.controller;

import com.sawli.shortlinkx.dto.request.LoginRequest;
import com.sawli.shortlinkx.dto.request.RegisterRequest;
import com.sawli.shortlinkx.dto.response.ApiResponse;
import com.sawli.shortlinkx.dto.response.AuthenticationResponse;
import com.sawli.shortlinkx.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling user registration and login.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "API endpoints for user registration, token generation, and authentication management")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint to register a new user in the platform.
     * Route: POST /api/v1/auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user account with unique email address.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload constraints (blank name/email or password too short)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email address is already in use")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        ApiResponse<Void> body = ApiResponse.success(null, "User registered successfully");
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    /**
     * Endpoint to authenticate an existing user.
     * Route: POST /api/v1/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Validates user credentials and returns a valid JWT authentication token.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully logged in",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class)))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials (email or password incorrect)")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
