package com.ecommerce.shared.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String customerId;
    private List<String> orderItems;
    private double totalAmount;
    private String timestamp;
    private String correlationId;
    private String causationId;
    private int version;
}
