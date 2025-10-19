package com.ecommerce.order.command.service;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import com.ecommerce.order.command.util.JsonUtil;
import com.ecommerce.shared.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final EventRepository eventRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public UUID createOrder(OrderRequest orderRequest) {
        UUID orderId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        UUID causationId = UUID.randomUUID();
        Instant now = Instant.now();
        
        log.info("Creating order for customer: {} with correlationId: {}", 
                orderRequest.getCustomerId(), correlationId);

        // Calcular total e criar items do evento
        BigDecimal totalAmount = BigDecimal.ZERO;
        var eventItems = orderRequest.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderCreatedEvent.OrderItem.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());

        totalAmount = eventItems.stream()
                .map(OrderCreatedEvent.OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Criar evento de domínio
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(orderRequest.getCustomerId())
                .items(eventItems)
                .totalAmount(totalAmount)
                .timestamp(now)
                .correlationId(correlationId)
                .causationId(causationId)
                .version(1)
                .build();

        String eventData = JsonUtil.toJson(orderCreatedEvent);

        // Persistir evento no Event Store
        Event event = Event.builder()
                .aggregateId(orderId)
                .eventType("OrderCreated")
                .eventData(eventData)
                .correlationId(correlationId)
                .causationId(causationId)
                .version(1)
                .createdAt(now)
                .build();

        eventRepository.save(event);
        log.debug("Event saved to Event Store: {}", event.getId());

        // Persistir no Outbox para publicação garantida
        Outbox outbox = Outbox.builder()
                .aggregateId(orderId)
                .eventType("OrderCreated")
                .eventData(eventData)
                .correlationId(correlationId)
                .causationId(causationId)
                .version(1)
                .createdAt(now)
                .processed(false)
                .build();

        outboxRepository.save(outbox);
        log.debug("Event saved to Outbox: {}", outbox.getId());

        log.info("Order created successfully with ID: {} and total amount: {}", 
                orderId, totalAmount);

        return orderId;
    }
}
