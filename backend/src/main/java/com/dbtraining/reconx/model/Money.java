package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV024 — Immutable value object: Money
 *
 * WHAT:    Record bundling a {@link BigDecimal} amount with a {@link Currency}.
 *          Used everywhere a monetary value crosses a boundary (DTO, event,
 *          metric).
 * HOW:     Compact constructor enforces: non-null amount, non-null currency,
 *          non-negative amount. {@link BigDecimal} (not double) prevents
 *          accumulating floating-point error on aggregations.
 * WHY:     Passing raw BigDecimal around loses currency context — a USD 100
 *          can be silently added to a EUR 100. Money makes the mismatch
 *          fail at the type level: {@code plus()} throws if currencies differ.
 * OBSERVE: {@code Money.of("100.00","USD").plus(Money.of("50","EUR"))} throws.
 *          {@code Money.of("100","USD").plus(Money.of("50","USD"))} returns 150 USD.
 * ============================================================================
 */
public record Money(BigDecimal amount, Currency currency) {
    /**
     * Creates a validated monetary value.
     *
     * @param amount the monetary amount
     * @param currency the currency associated with the amount
     */
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative: " + amount);
        }
    }

    /**
     * Create Money from strings.
     * @param amount decimal amount string
     * @param currencyCode ISO-4217 currency code
     * @return a new Money instance
     */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    /**
     * Create Money from a BigDecimal amount.
     * @param amount monetary amount
     * @param currencyCode ISO-4217 currency code
     * @return a new Money instance
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    /**
     * Add another Money of the same currency.
     * @param other the amount to add
     * @return a new Money instance representing the sum
     * @throws IllegalArgumentException if currencies differ
     */
    public Money plus(Money other) {
        Objects.requireNonNull(other, "other");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add %s to %s: currency mismatch".formatted(other.currency, currency));
        }
        return new Money(amount.add(other.amount), currency);
    }

    /**
     * Multiply the monetary amount by a factor.
     * @param multiplier multiplication factor
     * @return a new Money instance with the scaled amount
     */
    public Money times(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        return new Money(amount.multiply(multiplier), currency);
    }
}
