package com.ecommerce.order.command.domain.valueobject;

import com.ecommerce.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class OrderItem implements ValueObject {
    
    private final ProductId productId;
    private final String productName;
    private final Quantity quantity;
    private final Money unitPrice;
    private final Money subtotal;

    public OrderItem(ProductId productId, String productName, Quantity quantity, Money unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price cannot be null");
        }

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity.getValue());
    }

    public static OrderItem create(String productId, String productName, int quantity, Money unitPrice) {
        return new OrderItem(
            new ProductId(productId),
            productName,
            new Quantity(quantity),
            unitPrice
        );
    }
}

