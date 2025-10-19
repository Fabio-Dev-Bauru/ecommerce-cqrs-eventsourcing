package com.ecommerce.order.command.exception;

import java.util.UUID;

public class OrderNotFoundException extends BusinessException {
    
    public OrderNotFoundException(UUID orderId) {
        super("ORDER_NOT_FOUND", String.format("Order with ID %s not found", orderId));
    }
}

