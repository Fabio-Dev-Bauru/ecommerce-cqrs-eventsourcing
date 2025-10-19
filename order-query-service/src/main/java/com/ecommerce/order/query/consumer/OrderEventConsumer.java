package com.ecommerce.order.query.consumer;

import com.ecommerce.order.query.service.OrderProjectionService;
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

    private final OrderProjectionService projectionService;

    @KafkaListener(
        topics = "${kafka.topics.order-events:order-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received OrderCreatedEvent from topic: {}, partition: {}, offset: {}, orderId: {}",
                    topic, partition, offset, event.getOrderId());

            projectionService.handleOrderCreatedEvent(event);
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed and acknowledged event for orderId: {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent for orderId: {}", event.getOrderId(), e);
            // Não fazer acknowledge em caso de erro - mensagem será reprocessada
            // Em produção, considerar Dead Letter Queue após N tentativas
        }
    }
}

