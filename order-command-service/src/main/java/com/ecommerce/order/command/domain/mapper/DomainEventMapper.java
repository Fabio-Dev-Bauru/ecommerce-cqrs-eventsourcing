package com.ecommerce.order.command.domain.mapper;

import com.ecommerce.order.command.domain.event.OrderCreatedDomainEvent;
import com.ecommerce.order.command.domain.valueobject.OrderItem;
import com.ecommerce.shared.domain.DomainEvent;
import com.ecommerce.shared.events.OrderCreatedEvent;

import java.util.stream.Collectors;

public class DomainEventMapper {

    private DomainEventMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Object toExternalEvent(DomainEvent domainEvent) {
        if (domainEvent instanceof OrderCreatedDomainEvent) {
            return toOrderCreatedEvent((OrderCreatedDomainEvent) domainEvent);
        }
        throw new IllegalArgumentException("Unknown domain event type: " + domainEvent.getClass());
    }

    private static OrderCreatedEvent toOrderCreatedEvent(OrderCreatedDomainEvent domainEvent) {
        return OrderCreatedEvent.builder()
                .orderId(domainEvent.getAggregateId())
                .customerId(domainEvent.getCustomerId().getValue())
                .items(domainEvent.getItems().stream()
                        .map(DomainEventMapper::toOrderCreatedEventItem)
                        .collect(Collectors.toList()))
                .totalAmount(domainEvent.getTotalAmount().getAmount())
                .timestamp(domainEvent.getTimestamp())
                .correlationId(domainEvent.getCorrelationId())
                .causationId(domainEvent.getCausationId())
                .version(domainEvent.getVersion())
                .build();
    }

    private static OrderCreatedEvent.OrderItem toOrderCreatedEventItem(OrderItem item) {
        return OrderCreatedEvent.OrderItem.builder()
                .productId(item.getProductId().getValue())
                .productName(item.getProductName())
                .quantity(item.getQuantity().getValue())
                .unitPrice(item.getUnitPrice().getAmount())
                .subtotal(item.getSubtotal().getAmount())
                .build();
    }
}

