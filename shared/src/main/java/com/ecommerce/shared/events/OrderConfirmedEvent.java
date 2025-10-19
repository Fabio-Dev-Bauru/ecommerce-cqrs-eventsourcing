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
public class OrderConfirmedEvent {
    private UUID correlationId;
    private UUID orderId;
    private UUID paymentId;
    private UUID trackingNumber;
    private Instant timestamp;
}

