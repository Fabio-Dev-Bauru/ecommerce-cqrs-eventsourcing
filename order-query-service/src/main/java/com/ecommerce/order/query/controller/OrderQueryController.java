package com.ecommerce.order.query.controller;

import com.ecommerce.order.query.dto.OrderQueryResponse;
import com.ecommerce.order.query.projection.OrderProjection;
import com.ecommerce.order.query.service.OrderProjectionService;
import com.ecommerce.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderProjectionService projectionService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable UUID orderId) {
        log.info("GET request for order: {}", orderId);

        return projectionService.findById(orderId)
                .map(projection -> ResponseEntity.ok(
                        ApiResponse.success("Order retrieved successfully", toResponse(projection))
                ))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Order not found", HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse> getOrdersByCustomer(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        log.info("GET request for orders of customer: {}", customerId);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderProjection> orders = projectionService.findByCustomerId(customerId, pageable);
        Page<OrderQueryResponse> response = orders.map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("GET request for orders with status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderProjection> orders = projectionService.findByStatus(status, pageable);
        Page<OrderQueryResponse> response = orders.map(this::toResponse);

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Search request - customerId: {}, status: {}", customerId, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<OrderProjection> orders;
        if (customerId != null && status != null) {
            orders = projectionService.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            orders = projectionService.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            orders = projectionService.findByStatus(status, pageable);
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least one search parameter is required", HttpStatus.BAD_REQUEST));
        }

        Page<OrderQueryResponse> response = orders.map(this::toResponse);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        
        log.info("GET request for orders between {} and {}", startDate, endDate);

        List<OrderProjection> orders = projectionService.findOrdersByDateRange(startDate, endDate);
        List<OrderQueryResponse> response = orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", response));
    }

    @GetMapping("/customer/{customerId}/stats")
    public ResponseEntity<ApiResponse> getCustomerStats(@PathVariable String customerId) {
        log.info("GET request for stats of customer: {}", customerId);

        Long orderCount = projectionService.countByCustomerId(customerId);
        BigDecimal totalSpent = projectionService.getTotalSpentByCustomer(customerId);

        Map<String, Object> stats = Map.of(
                "customerId", customerId,
                "totalOrders", orderCount,
                "totalSpent", totalSpent
        );

        return ResponseEntity.ok(ApiResponse.success("Customer stats retrieved successfully", stats));
    }

    private OrderQueryResponse toResponse(OrderProjection projection) {
        List<OrderQueryResponse.OrderItemResponse> items = projection.getItems().stream()
                .map(item -> OrderQueryResponse.OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderQueryResponse.builder()
                .orderId(projection.getOrderId())
                .customerId(projection.getCustomerId())
                .items(items)
                .totalAmount(projection.getTotalAmount())
                .status(projection.getStatus())
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .build();
    }
}

