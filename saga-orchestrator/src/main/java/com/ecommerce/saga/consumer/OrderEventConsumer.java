package com.ecommerce.saga.consumer;

import com.ecommerce.saga.service.OrderSagaOrchestrator;
import com.ecommerce.shared.events.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderSagaOrchestrator orchestrator;

    @KafkaListener(
        topics = "order-events",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void consumeOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received OrderCreatedEvent from topic: {}, offset: {}, orderId: {}",
                    topic, offset, event.getOrderId());

            orchestrator.startSaga(event);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed OrderCreatedEvent for orderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for orderId: {}", event.getOrderId(), e);
            // Não fazer acknowledge - mensagem será reprocessada
        }
    }
}

