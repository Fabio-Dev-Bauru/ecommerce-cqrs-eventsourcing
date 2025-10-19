package com.ecommerce.order.command.domain.valueobject;

import com.ecommerce.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class CustomerId implements ValueObject {
    
    private final String value;

    public CustomerId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId cannot be null or blank");
        }
        this.value = value;
    }
}

