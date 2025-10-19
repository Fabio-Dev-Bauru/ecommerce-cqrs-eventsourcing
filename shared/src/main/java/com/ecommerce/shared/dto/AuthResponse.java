package com.ecommerce.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private String username;
    private List<String> roles;
    private Instant expiresAt;
}

