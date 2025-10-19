package com.ecommerce.order.command.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private String orderId;
    private String customerId;
    private String orderItems;
    private double totalAmount;
}
