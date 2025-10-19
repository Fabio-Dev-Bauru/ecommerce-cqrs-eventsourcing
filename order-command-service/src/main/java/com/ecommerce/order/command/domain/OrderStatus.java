package com.ecommerce.order.command.domain;

public enum OrderStatus {
    PENDING,
    PAYMENT_PENDING,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    CANCELLED,
    SHIPPED,
    DELIVERED
}

