package com.ecommerce.order.command.service;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderWithEventAndOutbox() {
        // Arrange
        OrderRequest request = createValidOrderRequest();

        // Act
        UUID orderId = orderService.createOrder(request);

        // Assert
        assertThat(orderId).isNotNull();

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(1);
        
        Event event = events.get(0);
        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(event.getAggregateId()).isEqualTo(orderId);
        assertThat(event.getVersion()).isEqualTo(1);
        assertThat(event.getEventData()).contains("CUST-123");

        List<Outbox> outboxRecords = outboxRepository.findAll();
        assertThat(outboxRecords).hasSize(1);
        
        Outbox outbox = outboxRecords.get(0);
        assertThat(outbox.getEventType()).isEqualTo("OrderCreated");
        assertThat(outbox.getAggregateId()).isEqualTo(orderId);
        assertThat(outbox.getProcessed()).isFalse();
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        // Arrange
        OrderRequest request = createValidOrderRequest();

        // Act
        UUID orderId = orderService.createOrder(request);

        // Assert
        Event event = eventRepository.findAll().get(0);
        assertThat(event.getEventData()).contains("2999.98");  // 1500*2 - 0.02
    }

    @Test
    void shouldThrowExceptionForInvalidRequest() {
        // Arrange
        OrderRequest request = OrderRequest.builder()
                .customerId("") // Invalid
                .items(List.of())
                .build();

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldPersistEventsAndOutboxAtomically() {
        // Arrange
        OrderRequest request = createValidOrderRequest();

        // Act
        orderService.createOrder(request);

        // Assert - Both should be saved in same transaction
        assertThat(eventRepository.count()).isEqualTo(outboxRepository.count());
    }

    private OrderRequest createValidOrderRequest() {
        return OrderRequest.builder()
                .customerId("CUST-123")
                .items(List.of(
                        OrderRequest.OrderItemRequest.builder()
                                .productId("PROD-001")
                                .productName("Laptop")
                                .quantity(2)
                                .unitPrice(new BigDecimal("1499.99"))
                                .build()
                ))
                .build();
    }
}

