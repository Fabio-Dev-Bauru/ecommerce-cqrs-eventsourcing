package com.ecommerce.order.command.domain;

import com.ecommerce.order.command.domain.event.OrderCreatedDomainEvent;
import com.ecommerce.order.command.domain.valueobject.CustomerId;
import com.ecommerce.order.command.domain.valueobject.Money;
import com.ecommerce.order.command.domain.valueobject.OrderItem;
import com.ecommerce.shared.domain.AggregateRoot;
import com.ecommerce.shared.domain.DomainEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
public class Order extends AggregateRoot {
    
    private CustomerId customerId;
    private List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Constructor privado para reconstrução do estado
    private Order() {
        super();
    }

    // Factory method para criar novo pedido
    public static Order createOrder(CustomerId customerId, List<OrderItem> items, 
                                   UUID correlationId, UUID causationId) {
        
        if (customerId == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        // Calcular total
        Money total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);

        // Criar ordem
        Order order = new Order();
        UUID orderId = UUID.randomUUID();
        order.id = orderId;

        // Aplicar evento
        OrderCreatedDomainEvent event = OrderCreatedDomainEvent.builder()
                .aggregateId(orderId)
                .customerId(customerId)
                .items(new ArrayList<>(items))
                .totalAmount(total)
                .timestamp(Instant.now())
                .correlationId(correlationId)
                .causationId(causationId)
                .version(1)
                .build();

        order.applyNewEvent(event);
        
        return order;
    }

    // Aplicar eventos (Event Sourcing)
    @Override
    protected void apply(DomainEvent event) {
        if (event instanceof OrderCreatedDomainEvent) {
            apply((OrderCreatedDomainEvent) event);
        }
        // Outros eventos serão adicionados aqui
    }

    private void apply(OrderCreatedDomainEvent event) {
        this.id = event.getAggregateId();
        this.customerId = event.getCustomerId();
        this.items = new ArrayList<>(event.getItems());
        this.totalAmount = event.getTotalAmount();
        this.status = OrderStatus.PENDING;
        this.createdAt = event.getTimestamp();
        this.updatedAt = event.getTimestamp();
        this.version = event.getVersion();
        
        log.debug("Order {} created for customer {} with total amount {}", 
                this.id, this.customerId.getValue(), this.totalAmount);
    }

    // Reconstruir do histórico de eventos
    public static Order fromHistory(List<DomainEvent> history) {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("Event history cannot be null or empty");
        }

        Order order = new Order();
        order.loadFromHistory(history);
        return order;
    }

    // Business methods (para futuros comandos)
    public void cancel(String reason) {
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        // TODO: Criar e aplicar OrderCancelledEvent
        log.info("Cancelling order {} with reason: {}", this.id, reason);
    }

    public void confirmPayment(UUID paymentId) {
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Order is not waiting for payment");
        }

        // TODO: Criar e aplicar PaymentConfirmedEvent
        log.info("Payment confirmed for order {} with paymentId: {}", this.id, paymentId);
    }
}

