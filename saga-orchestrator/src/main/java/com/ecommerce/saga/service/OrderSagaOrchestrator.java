package com.ecommerce.saga.service;

import com.ecommerce.saga.domain.SagaStatus;
import com.ecommerce.saga.domain.SagaStep;
import com.ecommerce.saga.entity.SagaInstance;
import com.ecommerce.saga.repository.SagaInstanceRepository;
import com.ecommerce.shared.commands.ProcessPaymentCommand;
import com.ecommerce.shared.commands.ReserveInventoryCommand;
import com.ecommerce.shared.commands.ScheduleShippingCommand;
import com.ecommerce.shared.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {

    private final SagaInstanceRepository sagaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void startSaga(OrderCreatedEvent event) {
        log.info("Starting Order Saga for orderId: {} with correlationId: {}", 
                event.getOrderId(), event.getCorrelationId());

        SagaInstance saga = SagaInstance.builder()
                .correlationId(event.getCorrelationId())
                .orderId(event.getOrderId())
                .status(SagaStatus.STARTED)
                .currentStep(SagaStep.ORDER_CREATED)
                .build();

        // Armazenar dados da saga
        saga.putSagaData("customerId", event.getCustomerId());
        saga.putSagaData("totalAmount", event.getTotalAmount());
        saga.putSagaData("items", event.getItems());

        saga = sagaRepository.save(saga);
        saga.addCompletedStep(SagaStep.ORDER_CREATED);

        // Iniciar primeiro passo: Payment
        processPayment(saga, event);
    }

    private void processPayment(SagaInstance saga, OrderCreatedEvent orderEvent) {
        log.info("Processing payment for saga: {}", saga.getCorrelationId());

        saga.setStatus(SagaStatus.PAYMENT_PENDING);
        saga.setCurrentStep(SagaStep.PAYMENT_PROCESSING);
        sagaRepository.save(saga);

        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .customerId(orderEvent.getCustomerId())
                .amount(orderEvent.getTotalAmount())
                .paymentMethod("CREDIT_CARD")
                .build();

        kafkaTemplate.send("payment-commands", command.getCorrelationId().toString(), command);
        log.debug("Payment command sent for saga: {}", saga.getCorrelationId());
    }

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Handling PaymentProcessed event for correlationId: {}", event.getCorrelationId());

        SagaInstance saga = sagaRepository.findByCorrelationId(event.getCorrelationId())
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + event.getCorrelationId()));

        if ("SUCCESS".equals(event.getStatus())) {
            saga.setStatus(SagaStatus.PAYMENT_AUTHORIZED);
            saga.addCompletedStep(SagaStep.PAYMENT_PROCESSING);
            saga.putSagaData("paymentId", event.getPaymentId());
            sagaRepository.save(saga);

            // Próximo passo: Reservar inventário
            reserveInventory(saga);
        } else {
            log.error("Payment failed for saga: {} - Reason: {}", 
                    saga.getCorrelationId(), event.getFailureReason());
            failSaga(saga, "Payment failed: " + event.getFailureReason());
        }
    }

    @SuppressWarnings("unchecked")
    private void reserveInventory(SagaInstance saga) {
        log.info("Reserving inventory for saga: {}", saga.getCorrelationId());

        saga.setStatus(SagaStatus.INVENTORY_PENDING);
        saga.setCurrentStep(SagaStep.INVENTORY_PROCESSING);
        sagaRepository.save(saga);

        List<OrderCreatedEvent.OrderItem> orderItems = 
                (List<OrderCreatedEvent.OrderItem>) saga.getSagaData("items");

        List<ReserveInventoryCommand.InventoryItem> items = orderItems.stream()
                .map(item -> ReserveInventoryCommand.InventoryItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        ReserveInventoryCommand command = ReserveInventoryCommand.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .items(items)
                .build();

        kafkaTemplate.send("inventory-commands", command.getCorrelationId().toString(), command);
        log.debug("Inventory command sent for saga: {}", saga.getCorrelationId());
    }

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Handling InventoryReserved event for correlationId: {}", event.getCorrelationId());

        SagaInstance saga = sagaRepository.findByCorrelationId(event.getCorrelationId())
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + event.getCorrelationId()));

        if ("SUCCESS".equals(event.getStatus())) {
            saga.setStatus(SagaStatus.INVENTORY_RESERVED);
            saga.addCompletedStep(SagaStep.INVENTORY_PROCESSING);
            saga.putSagaData("reservationItems", event.getItems());
            sagaRepository.save(saga);

            // Próximo passo: Agendar envio
            scheduleShipping(saga);
        } else {
            log.error("Inventory reservation failed for saga: {} - Reason: {}", 
                    saga.getCorrelationId(), event.getFailureReason());
            compensateSaga(saga, "Inventory reservation failed: " + event.getFailureReason());
        }
    }

    private void scheduleShipping(SagaInstance saga) {
        log.info("Scheduling shipping for saga: {}", saga.getCorrelationId());

        saga.setStatus(SagaStatus.SHIPPING_PENDING);
        saga.setCurrentStep(SagaStep.SHIPPING_PROCESSING);
        sagaRepository.save(saga);

        ScheduleShippingCommand command = ScheduleShippingCommand.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .customerId((String) saga.getSagaData("customerId"))
                .shippingAddress("Default Address") // TODO: Get from customer service
                .shippingMethod("STANDARD")
                .build();

        kafkaTemplate.send("shipping-commands", command.getCorrelationId().toString(), command);
        log.debug("Shipping command sent for saga: {}", saga.getCorrelationId());
    }

    @Transactional
    public void handleShippingScheduled(ShippingScheduledEvent event) {
        log.info("Handling ShippingScheduled event for correlationId: {}", event.getCorrelationId());

        SagaInstance saga = sagaRepository.findByCorrelationId(event.getCorrelationId())
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + event.getCorrelationId()));

        if ("SUCCESS".equals(event.getStatus())) {
            saga.setStatus(SagaStatus.SHIPPING_SCHEDULED);
            saga.addCompletedStep(SagaStep.SHIPPING_PROCESSING);
            saga.putSagaData("trackingNumber", event.getTrackingNumber());
            sagaRepository.save(saga);

            // Saga completa com sucesso!
            completeSaga(saga);
        } else {
            log.error("Shipping scheduling failed for saga: {} - Reason: {}", 
                    saga.getCorrelationId(), event.getFailureReason());
            compensateSaga(saga, "Shipping scheduling failed: " + event.getFailureReason());
        }
    }

    @Transactional
    protected void completeSaga(SagaInstance saga) {
        log.info("Completing saga: {}", saga.getCorrelationId());

        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCurrentStep(SagaStep.ORDER_CONFIRMED);
        saga.setCompletedAt(Instant.now());
        saga.addCompletedStep(SagaStep.ORDER_CONFIRMED);
        sagaRepository.save(saga);

        // Publicar evento de confirmação do pedido
        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .paymentId((UUID) saga.getSagaData("paymentId"))
                .trackingNumber((UUID) saga.getSagaData("trackingNumber"))
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send("order-events", event.getCorrelationId().toString(), event);
        
        log.info("Saga completed successfully: {} for orderId: {}", 
                saga.getCorrelationId(), saga.getOrderId());
    }

    @Transactional
    protected void compensateSaga(SagaInstance saga, String reason) {
        log.warn("Starting compensation for saga: {} - Reason: {}", saga.getCorrelationId(), reason);

        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(reason);
        sagaRepository.save(saga);

        // Compensar na ordem inversa dos passos completados
        List<SagaStep> completedSteps = saga.getCompletedSteps();

        // Compensar Shipping se foi agendado
        if (completedSteps.contains(SagaStep.SHIPPING_PROCESSING)) {
            saga.setCurrentStep(SagaStep.COMPENSATION_SHIPPING);
            // TODO: Enviar comando de cancelamento de shipping
            log.info("Compensating shipping for saga: {}", saga.getCorrelationId());
        }

        // Compensar Inventory se foi reservado
        if (completedSteps.contains(SagaStep.INVENTORY_PROCESSING)) {
            saga.setCurrentStep(SagaStep.COMPENSATION_INVENTORY);
            // TODO: Enviar comando de liberação de inventário
            log.info("Compensating inventory for saga: {}", saga.getCorrelationId());
        }

        // Compensar Payment se foi autorizado
        if (completedSteps.contains(SagaStep.PAYMENT_PROCESSING)) {
            saga.setCurrentStep(SagaStep.COMPENSATION_PAYMENT);
            // TODO: Enviar comando de estorno de pagamento
            log.info("Compensating payment for saga: {}", saga.getCorrelationId());
        }

        saga.setStatus(SagaStatus.COMPENSATION_COMPLETED);
        sagaRepository.save(saga);

        // Publicar evento de cancelamento do pedido
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .reason(reason)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send("order-events", event.getCorrelationId().toString(), event);

        log.info("Saga compensated successfully: {}", saga.getCorrelationId());
    }

    @Transactional
    protected void failSaga(SagaInstance saga, String reason) {
        log.error("Saga failed: {} - Reason: {}", saga.getCorrelationId(), reason);

        saga.setStatus(SagaStatus.FAILED);
        saga.setErrorMessage(reason);
        saga.setCompletedAt(Instant.now());
        sagaRepository.save(saga);

        // Publicar evento de cancelamento
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .correlationId(saga.getCorrelationId())
                .orderId(saga.getOrderId())
                .reason(reason)
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send("order-events", event.getCorrelationId().toString(), event);
    }
}

