package com.ecommerce.shared.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleShippingCommand {
    private UUID correlationId;
    private UUID orderId;
    private String customerId;
    private String shippingAddress;
    private String shippingMethod;
}

