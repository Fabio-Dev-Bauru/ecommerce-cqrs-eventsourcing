package com.ecommerce.order.command.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateMoneySuccessfully() {
        Money money = Money.of(new BigDecimal("100.00"));
        
        assertThat(money.getAmount()).isEqualByComparingTo("100.00");
        assertThat(money.getCurrency()).isEqualTo("BRL");
    }

    @Test
    void shouldAddMoneyCorrectly() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("50.00"));
        
        Money result = money1.add(money2);
        
        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldSubtractMoneyCorrectly() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("30.00"));
        
        Money result = money1.subtract(money2);
        
        assertThat(result.getAmount()).isEqualByComparingTo("70.00");
    }

    @Test
    void shouldMultiplyMoneyCorrectly() {
        Money money = Money.of(new BigDecimal("25.00"));
        
        Money result = money.multiply(4);
        
        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount cannot be null or negative");
    }

    @Test
    void shouldThrowExceptionForDifferentCurrencies() {
        Money brl = Money.of(new BigDecimal("100.00"), "BRL");
        Money usd = Money.of(new BigDecimal("50.00"), "USD");
        
        assertThatThrownBy(() -> brl.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot operate on different currencies");
    }

    @Test
    void shouldCompareMoneyCorrectly() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("50.00"));
        
        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isLessThan(money1)).isTrue();
    }
}

