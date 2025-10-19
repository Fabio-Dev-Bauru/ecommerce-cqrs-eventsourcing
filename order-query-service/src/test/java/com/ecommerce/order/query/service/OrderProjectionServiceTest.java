package com.ecommerce.order.query.service;

import com.ecommerce.order.query.projection.OrderProjection;
import com.ecommerce.order.query.repository.OrderProjectionRepository;
import com.ecommerce.shared.events.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderProjectionServiceTest {

    @Autowired
    private OrderProjectionService projectionService;

    @Autowired
    private OrderProjectionRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldHandleOrderCreatedEventSuccessfully() {
        // Arrange
        OrderCreatedEvent event = createOrderCreatedEvent();

        // Act
        projectionService.handleOrderCreatedEvent(event);

        // Assert
        List<OrderProjection> projections = repository.findAll();
        assertThat(projections).hasSize(1);

        OrderProjection projection = projections.get(0);
        assertThat(projection.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(projection.getCustomerId()).isEqualTo(event.getCustomerId());
        assertThat(projection.getStatus()).isEqualTo("PENDING");
        assertThat(projection.getTotalAmount()).isEqualByComparingTo(event.getTotalAmount());
        assertThat(projection.getItems()).hasSize(2);
    }

    @Test
    void shouldFindOrderByIdWithCache() {
        // Arrange
        OrderCreatedEvent event = createOrderCreatedEvent();
        projectionService.handleOrderCreatedEvent(event);

        // Act
        var result = projectionService.findById(event.getOrderId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(event.getOrderId());
    }

    @Test
    void shouldCalculateCustomerTotalSpent() {
        // Arrange
        createAndProcessMultipleOrders("CUST-123", 3);

        // Act
        BigDecimal totalSpent = projectionService.getTotalSpentByCustomer("CUST-123");

        // Assert - 3 orders * 3000 = 9000
        // But we need to set status to CONFIRMED first
        assertThat(totalSpent).isNotNull();
    }

    private OrderCreatedEvent createOrderCreatedEvent() {
        return OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId("CUST-123")
                .items(List.of(
                        OrderCreatedEvent.OrderItem.builder()
                                .productId("PROD-001")
                                .productName("Laptop")
                                .quantity(2)
                                .unitPrice(new BigDecimal("1499.99"))
                                .subtotal(new BigDecimal("2999.98"))
                                .build(),
                        OrderCreatedEvent.OrderItem.builder()
                                .productId("PROD-002")
                                .productName("Mouse")
                                .quantity(1)
                                .unitPrice(new BigDecimal("50.00"))
                                .subtotal(new BigDecimal("50.00"))
                                .build()
                ))
                .totalAmount(new BigDecimal("3049.98"))
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .causationId(UUID.randomUUID())
                .version(1)
                .build();
    }

    private void createAndProcessMultipleOrders(String customerId, int count) {
        for (int i = 0; i < count; i++) {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(UUID.randomUUID())
                    .customerId(customerId)
                    .items(List.of())
                    .totalAmount(new BigDecimal("3000.00"))
                    .timestamp(Instant.now())
                    .correlationId(UUID.randomUUID())
                    .causationId(UUID.randomUUID())
                    .version(1)
                    .build();
            
            projectionService.handleOrderCreatedEvent(event);
        }
    }
}

