package com.ecommerce.order.query.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderQueryResponse implements Serializable {
    
    private UUID orderId;
    private String customerId;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse implements Serializable {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}

