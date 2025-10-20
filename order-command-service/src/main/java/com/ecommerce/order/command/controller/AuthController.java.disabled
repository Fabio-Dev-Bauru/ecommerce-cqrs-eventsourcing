package com.ecommerce.order.command.controller;

import com.ecommerce.shared.dto.AuthResponse;
import com.ecommerce.shared.dto.LoginRequest;
import com.ecommerce.shared.response.ApiResponse;
import com.ecommerce.shared.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Em produção, validar contra banco de dados
        // Por enquanto, aceitar credenciais hardcoded para demo
        if (!"admin".equals(request.getUsername()) || !"admin123".equals(request.getPassword())) {
            log.warn("Invalid credentials for user: {}", request.getUsername());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid credentials", 
                            org.springframework.http.HttpStatus.UNAUTHORIZED));
        }

        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", "user-123");

        String token = jwtUtil.generateToken(request.getUsername(), claims);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(request.getUsername())
                .roles(roles)
                .expiresAt(Instant.now().plusMillis(jwtExpiration))
                .build();

        log.info("User {} authenticated successfully", request.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/test-token")
    public ResponseEntity<ApiResponse> generateTestToken() {
        log.info("Generating test token");

        List<String> roles = List.of("ROLE_USER");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("userId", "test-user");

        String token = jwtUtil.generateToken("test-user", claims);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username("test-user")
                .roles(roles)
                .expiresAt(Instant.now().plusMillis(jwtExpiration))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Test token generated", authResponse));
    }
}

