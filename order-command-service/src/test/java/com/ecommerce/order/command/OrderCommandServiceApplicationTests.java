package com.ecommerce.order.command;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import com.ecommerce.order.command.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderCommandServiceApplicationTests {

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
    void contextLoads() {
    }

    @Test
    void testCreateOrder() {
        OrderRequest.OrderItemRequest item1 = OrderRequest.OrderItemRequest.builder()
                .productId("PROD-001")
                .productName("Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("999.99"))
                .build();

        OrderRequest.OrderItemRequest item2 = OrderRequest.OrderItemRequest.builder()
                .productId("PROD-002")
                .productName("Mouse")
                .quantity(2)
                .unitPrice(new BigDecimal("29.99"))
                .build();

        OrderRequest orderRequest = OrderRequest.builder()
                .customerId("CUST-123")
                .items(List.of(item1, item2))
                .build();

        UUID orderId = orderService.createOrder(orderRequest);

        assertThat(orderId).isNotNull();

        List<Event> events = eventRepository.findAll();
        List<Outbox> outboxRecords = outboxRepository.findAll();

        assertThat(events).hasSize(1);
        assertThat(outboxRecords).hasSize(1);

        Event event = events.get(0);
        Outbox outbox = outboxRecords.get(0);

        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(event.getAggregateId()).isEqualTo(orderId);
        assertThat(event.getVersion()).isEqualTo(1);

        assertThat(outbox.getEventType()).isEqualTo("OrderCreated");
        assertThat(outbox.getAggregateId()).isEqualTo(orderId);
        assertThat(outbox.getProcessed()).isFalse();
    }
}
