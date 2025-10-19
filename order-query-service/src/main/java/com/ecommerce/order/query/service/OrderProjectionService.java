package com.ecommerce.order.query.service;

import com.ecommerce.order.query.projection.OrderProjection;
import com.ecommerce.order.query.repository.OrderProjectionRepository;
import com.ecommerce.shared.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProjectionService {

    private final OrderProjectionRepository repository;

    @Transactional
    @CacheEvict(value = {"orders", "ordersByCustomer"}, allEntries = true)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for orderId: {}", event.getOrderId());

        List<OrderProjection.OrderItemProjection> items = event.getItems().stream()
                .map(item -> OrderProjection.OrderItemProjection.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        OrderProjection projection = OrderProjection.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .items(items)
                .totalAmount(event.getTotalAmount())
                .status("PENDING")
                .createdAt(event.getTimestamp())
                .updatedAt(event.getTimestamp())
                .version(event.getVersion())
                .build();

        repository.save(projection);
        
        log.info("OrderProjection created for orderId: {} with {} items and total: {}",
                event.getOrderId(), items.size(), event.getTotalAmount());
    }

    @Cacheable(value = "orders", key = "#orderId")
    public Optional<OrderProjection> findById(UUID orderId) {
        log.debug("Finding order by ID: {}", orderId);
        return repository.findById(orderId);
    }

    @Cacheable(value = "ordersByCustomer", key = "#customerId + '-' + #pageable.pageNumber")
    public Page<OrderProjection> findByCustomerId(String customerId, Pageable pageable) {
        log.debug("Finding orders for customer: {}", customerId);
        return repository.findByCustomerId(customerId, pageable);
    }

    @Cacheable(value = "ordersByStatus", key = "#status + '-' + #pageable.pageNumber")
    public Page<OrderProjection> findByStatus(String status, Pageable pageable) {
        log.debug("Finding orders by status: {}", status);
        return repository.findByStatus(status, pageable);
    }

    public Page<OrderProjection> findByCustomerIdAndStatus(String customerId, String status, Pageable pageable) {
        log.debug("Finding orders for customer: {} with status: {}", customerId, status);
        return repository.findByCustomerIdAndStatus(customerId, status, pageable);
    }

    public List<OrderProjection> findOrdersByDateRange(Instant startDate, Instant endDate) {
        log.debug("Finding orders between {} and {}", startDate, endDate);
        return repository.findOrdersByDateRange(startDate, endDate);
    }

    public Long countByCustomerId(String customerId) {
        log.debug("Counting orders for customer: {}", customerId);
        return repository.countByCustomerId(customerId);
    }

    @Cacheable(value = "customerTotalSpent", key = "#customerId")
    public BigDecimal getTotalSpentByCustomer(String customerId) {
        log.debug("Getting total spent for customer: {}", customerId);
        BigDecimal total = repository.getTotalSpentByCustomer(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }
}

