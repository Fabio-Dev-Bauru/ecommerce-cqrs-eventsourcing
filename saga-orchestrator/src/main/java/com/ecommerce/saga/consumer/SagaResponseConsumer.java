package com.ecommerce.saga.consumer;

import com.ecommerce.saga.service.OrderSagaOrchestrator;
import com.ecommerce.shared.events.InventoryReservedEvent;
import com.ecommerce.shared.events.PaymentProcessedEvent;
import com.ecommerce.shared.events.ShippingScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaResponseConsumer {

    private final OrderSagaOrchestrator orchestrator;

    @KafkaListener(
        topics = "payment-events",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload PaymentProcessedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received PaymentProcessedEvent for correlationId: {}, status: {}",
                    event.getCorrelationId(), event.getStatus());

            orchestrator.handlePaymentProcessed(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "inventory-events",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "inventoryEventKafkaListenerContainerFactory"
    )
    public void consumeInventoryEvent(
            @Payload InventoryReservedEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received InventoryReservedEvent for correlationId: {}, status: {}",
                    event.getCorrelationId(), event.getStatus());

            orchestrator.handleInventoryReserved(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "shipping-events",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "shippingEventKafkaListenerContainerFactory"
    )
    public void consumeShippingEvent(
            @Payload ShippingScheduledEvent event,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received ShippingScheduledEvent for correlationId: {}, status: {}",
                    event.getCorrelationId(), event.getStatus());

            orchestrator.handleShippingScheduled(event);
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing ShippingScheduledEvent: {}", e.getMessage(), e);
        }
    }
}

