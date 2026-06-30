package com.sawli.shortlinkx.service.impl;

import com.sawli.shortlinkx.dto.request.LoginRequest;
import com.sawli.shortlinkx.dto.request.RegisterRequest;
import com.sawli.shortlinkx.dto.response.AuthenticationResponse;
import com.sawli.shortlinkx.entity.Role;
import com.sawli.shortlinkx.entity.User;
import com.sawli.shortlinkx.exception.EmailAlreadyExistsException;
import com.sawli.shortlinkx.repository.UserRepository;
import com.sawli.shortlinkx.service.AuthService;
import com.sawli.shortlinkx.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for user registration and authentication.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email address is already in use.");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Register as USER by default
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user email or password."));

        String token = jwtService.generateToken(user);
        long expiresIn = jwtService.getExpirationTime();

        return AuthenticationResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }
}
