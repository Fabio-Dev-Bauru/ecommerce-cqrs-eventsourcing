package com.ecommerce.saga.service;

import com.ecommerce.saga.domain.SagaStatus;
import com.ecommerce.saga.domain.SagaStep;
import com.ecommerce.saga.entity.SagaInstance;
import com.ecommerce.saga.repository.SagaInstanceRepository;
import com.ecommerce.shared.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderSagaOrchestratorTest {

    @Autowired
    private OrderSagaOrchestrator orchestrator;

    @Autowired
    private SagaInstanceRepository sagaRepository;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        sagaRepository.deleteAll();
    }

    @Test
    void shouldStartSagaSuccessfully() {
        // Arrange
        OrderCreatedEvent event = createOrderCreatedEvent();

        // Act
        orchestrator.startSaga(event);

        // Assert
        List<SagaInstance> sagas = sagaRepository.findAll();
        assertThat(sagas).hasSize(1);

        SagaInstance saga = sagas.get(0);
        assertThat(saga.getCorrelationId()).isEqualTo(event.getCorrelationId());
        assertThat(saga.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.PAYMENT_PENDING);
        assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.PAYMENT_PROCESSING);

        verify(kafkaTemplate, times(1)).send(eq("payment-commands"), any(), any());
    }

    @Test
    void shouldHandlePaymentSuccessAndProceedToInventory() {
        // Arrange
        OrderCreatedEvent orderEvent = createOrderCreatedEvent();
        orchestrator.startSaga(orderEvent);

        PaymentProcessedEvent paymentEvent = PaymentProcessedEvent.builder()
                .correlationId(orderEvent.getCorrelationId())
                .orderId(orderEvent.getOrderId())
                .paymentId(UUID.randomUUID())
                .amount(orderEvent.getTotalAmount())
                .status("SUCCESS")
                .timestamp(Instant.now())
                .build();

        // Act
        orchestrator.handlePaymentProcessed(paymentEvent);

        // Assert
        SagaInstance saga = sagaRepository.findByCorrelationId(orderEvent.getCorrelationId()).get();
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.INVENTORY_PENDING);
        assertThat(saga.getCurrentStep()).isEqualTo(SagaStep.INVENTORY_PROCESSING);
        assertThat(saga.getCompletedSteps()).contains(SagaStep.PAYMENT_PROCESSING);

        verify(kafkaTemplate, times(1)).send(eq("inventory-commands"), any(), any());
    }

    @Test
    void shouldCompensateWhenPaymentFails() {
        // Arrange
        OrderCreatedEvent orderEvent = createOrderCreatedEvent();
        orchestrator.startSaga(orderEvent);

        PaymentProcessedEvent paymentEvent = PaymentProcessedEvent.builder()
                .correlationId(orderEvent.getCorrelationId())
                .orderId(orderEvent.getOrderId())
                .status("FAILED")
                .failureReason("Insufficient funds")
                .timestamp(Instant.now())
                .build();

        // Act
        orchestrator.handlePaymentProcessed(paymentEvent);

        // Assert
        SagaInstance saga = sagaRepository.findByCorrelationId(orderEvent.getCorrelationId()).get();
        assertThat(saga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(saga.getErrorMessage()).contains("Payment failed");

        verify(kafkaTemplate, times(1)).send(eq("order-events"), any(), any());
    }

    private OrderCreatedEvent createOrderCreatedEvent() {
        return OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId("CUST-123")
                .items(List.of(
                        OrderCreatedEvent.OrderItem.builder()
                                .productId("PROD-001")
                                .productName("Laptop")
                                .quantity(1)
                                .unitPrice(new BigDecimal("1500.00"))
                                .subtotal(new BigDecimal("1500.00"))
                                .build()
                ))
                .totalAmount(new BigDecimal("1500.00"))
                .timestamp(Instant.now())
                .correlationId(UUID.randomUUID())
                .causationId(UUID.randomUUID())
                .version(1)
                .build();
    }
}

