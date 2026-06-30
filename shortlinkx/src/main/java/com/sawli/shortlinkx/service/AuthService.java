package com.sawli.shortlinkx.service;

import com.sawli.shortlinkx.dto.request.LoginRequest;
import com.sawli.shortlinkx.dto.request.RegisterRequest;
import com.sawli.shortlinkx.dto.response.AuthenticationResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user in the system.
     */
    void register(RegisterRequest request);

    /**
     * Authenticates a user and returns a JWT access token.
     */
    AuthenticationResponse login(LoginRequest request);
}
