package com.ecommerce.saga.controller;

import com.ecommerce.saga.entity.SagaInstance;
import com.ecommerce.saga.repository.SagaInstanceRepository;
import com.ecommerce.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/sagas")
@RequiredArgsConstructor
public class SagaController {

    private final SagaInstanceRepository sagaRepository;

    @GetMapping("/{correlationId}")
    public ResponseEntity<ApiResponse> getSagaByCorrelationId(@PathVariable UUID correlationId) {
        log.info("GET request for saga with correlationId: {}", correlationId);

        return sagaRepository.findByCorrelationId(correlationId)
                .map(saga -> ResponseEntity.ok(
                        ApiResponse.success("Saga retrieved successfully", saga)
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Saga not found", HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse> getSagaByOrderId(@PathVariable UUID orderId) {
        log.info("GET request for saga with orderId: {}", orderId);

        return sagaRepository.findByOrderId(orderId)
                .map(saga -> ResponseEntity.ok(
                        ApiResponse.success("Saga retrieved successfully", saga)
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Saga not found", HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllSagas() {
        log.info("GET request for all sagas");

        List<SagaInstance> sagas = sagaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Sagas retrieved successfully", sagas));
    }
}

