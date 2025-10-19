package com.ecommerce.order.command.domain.event;

import com.ecommerce.order.command.domain.valueobject.CustomerId;
import com.ecommerce.order.command.domain.valueobject.Money;
import com.ecommerce.order.command.domain.valueobject.OrderItem;
import com.ecommerce.shared.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
public class OrderCreatedDomainEvent implements DomainEvent {
    
    private final UUID aggregateId;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Money totalAmount;
    private final Instant timestamp;
    private final UUID correlationId;
    private final UUID causationId;
    private final Integer version;

    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}

