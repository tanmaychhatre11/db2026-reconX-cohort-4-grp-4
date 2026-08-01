package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV022 — DerivativeTrade with Builder pattern
 *
 * WHAT:    Option/derivative trade — underlying, strike, expiry, optionType.
 * HOW:     Same builder pattern. notional() = strike * quantity in the
 *          trade's currency (simplified — real derivatives use delta-adjusted).
 * WHY:     Expiry is validated against tradeDate, not LocalDate.now(), because
 *          expired options are valid historical records for reconciliation.
 * ============================================================================
 */
public final class DerivativeTrade implements TradeType {

    /**
     * Supported option types.
     */
    public enum OptionType {

        /** Call option. */
        CALL,

        /** Put option. */
        PUT
    }

    private final TradeRef tradeRef;
    private final String underlying;
    private final BigDecimal strike;
    private final BigDecimal quantity;
    private final LocalDate expiry;
    private final OptionType optionType;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private DerivativeTrade(Builder b) {
        this.tradeRef       = b.tradeRef;
        this.underlying     = b.underlying;
        this.strike         = b.strike;
        this.quantity       = b.quantity;
        this.expiry         = b.expiry;
        this.optionType     = b.optionType;
        this.currency       = b.currency;
        this.side           = b.side;
        this.tradeDate      = b.tradeDate;
        this.counterpartyId = b.counterpartyId;
    }

    /**
     * Creates a new builder.
     *
     * @return a new Builder instance
     */
    public static Builder builder() { return new Builder(); }

    @Override public TradeRef tradeRef()     { return tradeRef; }
    @Override public LocalDate tradeDate()   { return tradeDate; }
    @Override public AssetClass assetClass() { return AssetClass.DERIVATIVE; }

    /** Simplified notional = strike * quantity in the trade currency. */
    @Override public Money notional() {
        return new Money(strike.multiply(quantity), currency);
    }

    /**
     * @return the underlying instrument
     */
    public String underlying()       { return underlying; }

    /**
     * @return the option strike price
     */
    public BigDecimal strike()       { return strike; }

    /**
     * @return the option quantity
     */
    public BigDecimal quantity()     { return quantity; }

    /**
     * @return the option expiry date
     */
    public LocalDate expiry()        { return expiry; }

    /**
     * @return the option type
     */
    public OptionType optionType()   { return optionType; }

    /**
     * @return the trade currency
     */
    public Currency currency()       { return currency; }

    /**
     * @return the trade side
     */
    public Side side()               { return side; }

    /**
     * @return the counterparty identifier
     */
    public long counterpartyId()     { return counterpartyId; }

    @Override public boolean equals(Object o) {
        return (o instanceof DerivativeTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override public String toString() {
        return "DerivativeTrade[ref=%s, %s %s on %s, strike=%s %s, qty=%s, expiry=%s, side=%s]"
                .formatted(tradeRef, optionType, underlying, tradeDate, strike,
                        currency.getCurrencyCode(), quantity, expiry, side);
    }

    /**
     * Builder for creating immutable BondTrade instances.
     */
    public static final class Builder {
        private TradeRef tradeRef;
        private String underlying;
        private BigDecimal strike, quantity;
        private LocalDate expiry, tradeDate;
        private OptionType optionType;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        /**
         * Set the trade reference.
         * @param v trade reference
         * @return this builder
         */
        public Builder tradeRef(TradeRef v)        { this.tradeRef = v; return this; }

        /**
         * Set the underlying instrument.
         * @param v underlying name
         * @return this builder
         */
        public Builder underlying(String v)        { this.underlying = v; return this; }

        /**
         * Set the option strike price.
         * @param v strike price
         * @return this builder
         */
        public Builder strike(BigDecimal v)        { this.strike = v; return this; }

        /**
         * Set the option quantity.
         * @param v quantity
         * @return this builder
         */
        public Builder quantity(BigDecimal v)      { this.quantity = v; return this; }

        /**
         * Set the option expiry date.
         * @param v expiry date
         * @return this builder
         */
        public Builder expiry(LocalDate v)         { this.expiry = v; return this; }

        /**
         * Set the option type.
         * @param v CALL or PUT
         * @return this builder
         */
        public Builder optionType(OptionType v)    { this.optionType = v; return this; }

        /**
         * Set the option currency.
         * @param code ISO-4217 currency code
         * @return this builder
         */
        public Builder currency(String code)       { this.currency = Currency.getInstance(code); return this; }

        /**
         * Set the trade side.
         * @param v BUY or SELL
         * @return this builder
         */
        public Builder side(Side v)                { this.side = v; return this; }

        /**
         * Set the trade date.
         * @param v trade date
         * @return this builder
         */
        public Builder tradeDate(LocalDate v)      { this.tradeDate = v; return this; }

        /**
         * Set the counterparty identifier.
         * @param v counterparty id
         * @return this builder
         */
        public Builder counterpartyId(long v)      { this.counterpartyId = v; return this; }

        /**
         * Build a validated DerivativeTrade.
         * @return a new DerivativeTrade
         * @throws NullPointerException if a required field is missing
         * @throws IllegalStateException if any invariant is violated
         */
        public DerivativeTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(underlying, "underlying");
            Objects.requireNonNull(strike, "strike");
            Objects.requireNonNull(quantity, "quantity");
            Objects.requireNonNull(expiry, "expiry");
            Objects.requireNonNull(optionType, "optionType");
            Objects.requireNonNull(currency, "currency");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");

            if (underlying.isBlank()) {
                throw new IllegalStateException("underlying must not be blank");
            }
            if (strike.signum() <= 0) {
                throw new IllegalStateException("strike must be > 0");
            }
            if (quantity.signum() <= 0) {
                throw new IllegalStateException("quantity must be > 0");
            }
            if (!expiry.isAfter(tradeDate)) {
                throw new IllegalStateException("expiry cannot be before tradeDate");
            }

            return new DerivativeTrade(this);
        }
    }
}
