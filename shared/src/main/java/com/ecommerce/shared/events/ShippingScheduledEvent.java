package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingScheduledEvent {
    private UUID correlationId;
    private UUID orderId;
    private UUID trackingNumber;
    private String status; // SUCCESS, FAILED
    private String failureReason;
    private Instant estimatedDelivery;
    private Instant timestamp;
}

