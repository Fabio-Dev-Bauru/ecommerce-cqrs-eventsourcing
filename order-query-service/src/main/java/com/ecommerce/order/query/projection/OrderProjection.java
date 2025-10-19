package com.ecommerce.order.query.projection;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_projection", indexes = {
    @Index(name = "idx_customer_id", columnList = "customerId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class OrderProjection implements Serializable {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private String customerId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<OrderItemProjection> items;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Integer version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemProjection implements Serializable {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}

