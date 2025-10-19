package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReservedEvent {
    private UUID correlationId;
    private UUID orderId;
    private List<ReservedItem> items;
    private String status; // SUCCESS, FAILED
    private String failureReason;
    private Instant timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservedItem {
        private String productId;
        private Integer quantity;
        private UUID reservationId;
    }
}

