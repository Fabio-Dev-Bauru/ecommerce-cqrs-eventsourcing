package com.ecommerce.order.command.domain.valueobject;

import com.ecommerce.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ProductId implements ValueObject {
    
    private final String value;

    public ProductId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be null or blank");
        }
        this.value = value;
    }
}

