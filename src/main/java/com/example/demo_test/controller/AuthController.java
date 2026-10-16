package com.example.demo_test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_test.dto.ApiResponse;
import com.example.demo_test.dto.AuthResponse;
import com.example.demo_test.dto.LoginRequest;
import com.example.demo_test.dto.RegisterRequest;
import com.example.demo_test.dto.UserResponse;
import com.example.demo_test.entity.User;
import com.example.demo_test.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Authentication")
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user (role USER)")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .message("User successfully registered")
                        .data(authService.register(request))
                        .build());
    }

    @Operation(summary = "Login and get a JWT token")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        return ApiResponse.<AuthResponse>builder()
                .message("Login successful")
                .data(authService.login(request))
                .build();
    }

    @Operation(summary = "Get the currently logged-in user")
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal User user) {

        return ApiResponse.<UserResponse>builder()
                .message("Success fetch current user")
                .data(authService.toResponse(user))
                .build();
    }
}
