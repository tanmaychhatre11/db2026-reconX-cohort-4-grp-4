package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void of_createsMoneyFromStringAmountAndCurrencyCode() {
        Money m = Money.of("100", "USD");

        assertThat(m.amount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(m.currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void equals_sameAmountAndCurrency_areEqual() {
        assertThat(Money.of("100", "USD")).isEqualTo(Money.of("100", "USD"));
        assertThat(Money.of("100", "USD").hashCode())
                .isEqualTo(Money.of("100", "USD").hashCode());
    }

    @Test
    void constructor_rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null, java.util.Currency.getInstance("USD")))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void constructor_rejectsNullCurrency() {
        assertThatThrownBy(() -> new Money(new BigDecimal("100"), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void constructor_rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-1", "USD"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void plus_sameCurrency_returnsNewSummedMoney() {
        Money a = Money.of("100", "USD");
        Money b = Money.of("50", "USD");

        Money result = a.plus(b);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(result.currency().getCurrencyCode()).isEqualTo("USD");
        // originals are untouched — Money is immutable
        assertThat(a.amount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(b.amount()).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void plus_differentCurrency_throwsCurrencyMismatch() {
        Money usd = Money.of("100", "USD");
        Money eur = Money.of("50", "EUR");

        assertThatThrownBy(() -> usd.plus(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void times_returnsScaledMoney() {
        Money price = Money.of("100", "USD");

        Money result = price.times(new BigDecimal("3"));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("300"));
        assertThat(result.currency().getCurrencyCode()).isEqualTo("USD");
    }
}