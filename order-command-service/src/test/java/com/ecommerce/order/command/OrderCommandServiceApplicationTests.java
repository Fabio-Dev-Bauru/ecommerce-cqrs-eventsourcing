package com.ecommerce.order.command;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import com.ecommerce.order.command.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderCommandServiceApplicationTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateOrder() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderId("123");
        orderRequest.setCustomerId("456");
        orderRequest.setOrderItems("item1,item2");
        orderRequest.setTotalAmount(100.0);

        orderService.createOrder(orderRequest);

        Event event = eventRepository.findAll().get(0);
        Outbox outbox = outboxRepository.findAll().get(0);

        assertThat(event.getEventType()).isEqualTo("OrderCreated");
        assertThat(outbox.getEventType()).isEqualTo("OrderCreated");
    }
}
