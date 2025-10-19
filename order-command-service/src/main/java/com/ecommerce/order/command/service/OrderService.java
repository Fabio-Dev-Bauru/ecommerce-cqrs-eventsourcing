package com.ecommerce.order.command.service;

import com.ecommerce.order.command.dto.OrderRequest;
import com.ecommerce.order.command.entity.Event;
import com.ecommerce.order.command.entity.Outbox;
import com.ecommerce.order.command.repository.EventRepository;
import com.ecommerce.order.command.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    public void createOrder(OrderRequest orderRequest) {
        // Lógica para criar evento e outbox
        Event event = new Event(null, "OrderCreated", orderRequest.toString(), "timestamp");
        Outbox outbox = new Outbox(null, "OrderCreated", orderRequest.toString(), "timestamp", false);

        eventRepository.save(event);
        outboxRepository.save(outbox);
    }
}
