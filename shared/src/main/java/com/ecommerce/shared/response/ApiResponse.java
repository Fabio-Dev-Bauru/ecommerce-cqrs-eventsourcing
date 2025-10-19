package com.ecommerce.shared.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Builder
public class ApiResponse {
    private final String message;
    private final HttpStatus status;
    private final Object data;
    @Builder.Default
    private final Instant timestamp = Instant.now();

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .message(message)
                .status(HttpStatus.OK)
                .data(data)
                .build();
    }

    public static ApiResponse created(String message, Object data) {
        return ApiResponse.builder()
                .message(message)
                .status(HttpStatus.CREATED)
                .data(data)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status) {
        return ApiResponse.builder()
                .message(message)
                .status(status)
                .build();
    }

    public static ApiResponse error(String message, HttpStatus status, Object data) {
        return ApiResponse.builder()
                .message(message)
                .status(status)
                .data(data)
                .build();
    }
}
