package com.ecommerce.order.command.service;

import com.ecommerce.order.command.domain.Order;
import com.ecommerce.order.command.domain.mapper.DomainEventMapper;
import com.ecommerce.order.command.domain.valueobject.CustomerId;
import com.ecommerce.order.command.domain.valueobject.Money;
import com.ecommerce.order.command.domain.valueobject.OrderItem;
import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import com.ecommerce.order.command.util.JsonUtil;
import com.ecommerce.shared.domain.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        UUID correlationId = UUID.randomUUID();
        UUID causationId = UUID.randomUUID();
        
        log.info("Creating order for customer: {} with correlationId: {}", 
                orderRequest.getCustomerId(), correlationId);

        // Converter DTO para Value Objects
        CustomerId customerId = new CustomerId(orderRequest.getCustomerId());
        List<OrderItem> items = orderRequest.getItems().stream()
                .map(item -> OrderItem.create(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        Money.of(item.getUnitPrice())
                ))
                .collect(Collectors.toList());

        // Criar agregado Order (Event Sourcing)
        Order order = Order.createOrder(customerId, items, correlationId, causationId);
        UUID orderId = order.getId();

        // Persistir eventos no Event Store
        List<DomainEvent> uncommittedEvents = order.getUncommittedEvents();
        for (DomainEvent domainEvent : uncommittedEvents) {
            persistEvent(domainEvent);
            persistOutbox(domainEvent);
        }

        // Marcar eventos como commitados
        order.markEventsAsCommitted();

        log.info("Order created successfully with ID: {} and total amount: {}", 
                orderId, order.getTotalAmount());

        return orderId;
    }

    private void persistEvent(DomainEvent domainEvent) {
        Object externalEvent = DomainEventMapper.toExternalEvent(domainEvent);
        String eventData = JsonUtil.toJson(externalEvent);

        Event event = Event.builder()
                .aggregateId(domainEvent.getAggregateId())
                .eventType(domainEvent.getEventType())
                .eventData(eventData)
                .correlationId(domainEvent.getCorrelationId())
                .causationId(domainEvent.getCausationId())
                .version(domainEvent.getVersion())
                .createdAt(domainEvent.getTimestamp())
                .build();

        eventRepository.save(event);
        log.debug("Event {} saved to Event Store with ID: {}", 
                domainEvent.getEventType(), event.getId());
    }

    private void persistOutbox(DomainEvent domainEvent) {
        Object externalEvent = DomainEventMapper.toExternalEvent(domainEvent);
        String eventData = JsonUtil.toJson(externalEvent);

        Outbox outbox = Outbox.builder()
                .aggregateId(domainEvent.getAggregateId())
                .eventType(domainEvent.getEventType())
                .eventData(eventData)
                .correlationId(domainEvent.getCorrelationId())
                .causationId(domainEvent.getCausationId())
                .version(domainEvent.getVersion())
                .createdAt(domainEvent.getTimestamp())
                .processed(false)
                .build();

        outboxRepository.save(outbox);
        log.debug("Event {} saved to Outbox with ID: {}", 
                domainEvent.getEventType(), outbox.getId());
    }
}
