package com.example.demo_test.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresInMs;
    private Long userId;
    private String email;
    private String role;
}
