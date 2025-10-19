package com.ecommerce.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReserveInventoryCommand {
    private UUID correlationId;
    private UUID orderId;
    private List<InventoryItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryItem {
        private String productId;
        private Integer quantity;
    }
}

