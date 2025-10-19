package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent {
    private UUID correlationId;
    private UUID orderId;
    private UUID paymentId;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED
    private String failureReason;
    private Instant timestamp;
}

