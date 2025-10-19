package com.ecommerce.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentCommand {
    private UUID correlationId;
    private UUID orderId;
    private String customerId;
    private BigDecimal amount;
    private String paymentMethod;
}

