package com.ecommerce.order.command.domain.valueobject;

import com.ecommerce.shared.domain.ValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class Quantity implements ValueObject {
    
    private final Integer value;

    public Quantity(Integer value) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.value = value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        return new Quantity(this.value - other.value);
    }
}

