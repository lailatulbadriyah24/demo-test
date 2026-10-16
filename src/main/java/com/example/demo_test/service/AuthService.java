package com.example.demo_test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.AuthResponse;
import com.example.demo_test.dto.LoginRequest;
import com.example.demo_test.dto.RegisterRequest;
import com.example.demo_test.dto.UserResponse;
import com.example.demo_test.entity.Role;
import com.example.demo_test.entity.User;
import com.example.demo_test.repository.UserRepository;
import com.example.demo_test.security.JwtService;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        logger.info("Registering user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            logger.warn("Register failed: email {} already used", email);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);

        logger.info("User registered with ID: {}", saved.getId());

        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> {
                    logger.warn("Login failed for email: {}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
                });

        return AuthResponse.builder()
                .token(jwtService.generateToken(user.getEmail(), user.getRole().name()))
                .tokenType("Bearer")
                .expiresInMs(jwtService.getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
