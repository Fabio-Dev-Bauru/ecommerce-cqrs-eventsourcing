package com.ecommerce.order.command.domain;

import com.ecommerce.order.command.domain.event.OrderCreatedDomainEvent;
import com.ecommerce.order.command.domain.valueobject.CustomerId;
import com.ecommerce.order.command.domain.valueobject.Money;
import com.ecommerce.order.command.domain.valueobject.OrderItem;
import com.ecommerce.shared.domain.DomainEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        CustomerId customerId = new CustomerId("CUST-123");
        OrderItem item = OrderItem.create("PROD-001", "Laptop", 2, Money.of(new BigDecimal("1500.00")));
        UUID correlationId = UUID.randomUUID();
        UUID causationId = UUID.randomUUID();

        // Act
        Order order = Order.createOrder(customerId, List.of(item), correlationId, causationId);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo("3000.00");
        assertThat(order.getUncommittedEvents()).hasSize(1);
        
        DomainEvent event = order.getUncommittedEvents().get(0);
        assertThat(event).isInstanceOf(OrderCreatedDomainEvent.class);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void shouldThrowExceptionWhenCustomerIdIsNull() {
        OrderItem item = OrderItem.create("PROD-001", "Laptop", 1, Money.of(new BigDecimal("1500.00")));

        assertThatThrownBy(() -> Order.createOrder(null, List.of(item), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CustomerId cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenItemsAreEmpty() {
        CustomerId customerId = new CustomerId("CUST-123");

        assertThatThrownBy(() -> Order.createOrder(customerId, List.of(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order must have at least one item");
    }

    @Test
    void shouldCalculateTotalAmountCorrectly() {
        CustomerId customerId = new CustomerId("CUST-123");
        OrderItem item1 = OrderItem.create("PROD-001", "Laptop", 2, Money.of(new BigDecimal("1500.00")));
        OrderItem item2 = OrderItem.create("PROD-002", "Mouse", 3, Money.of(new BigDecimal("50.00")));

        Order order = Order.createOrder(customerId, List.of(item1, item2), UUID.randomUUID(), UUID.randomUUID());

        // 2 * 1500 + 3 * 50 = 3000 + 150 = 3150
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo("3150.00");
    }
}

