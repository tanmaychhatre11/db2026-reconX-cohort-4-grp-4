package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV019 — EquityTrade with Builder pattern
 *
 * WHAT:    Concrete TradeType for equity (cash share) trades.
 * HOW:     Final class, all fields final, no setters. Construction is via the
 *          nested {@link Builder} which validates in {@link Builder#build()}.
 * WHY:     Eight required fields on a single constructor is unreadable at
 *          the call site. Builder gives named arguments, makes the validity
 *          check a single chokepoint, and the object stays immutable.
 * OBSERVE: Calling build() with a missing required field throws
 *          IllegalStateException — verified by EquityTradeTest.
 * HINT:    Same shape applied to FXTrade/BondTrade/DerivativeTrade.
 * ============================================================================
 *
 * TICKET-ADV028 — equals/hashCode from tradeRef (Object methods on a regular class)
 * TICKET-ADV030 — toString() omits PII, prints reference/symbol/qty/price/side
 */
public final class EquityTrade implements TradeType {

    private final TradeRef tradeRef;
    private final String instrumentSymbol;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final Currency currency;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private EquityTrade(Builder b) {
        this.tradeRef         = b.tradeRef;
        this.instrumentSymbol = b.instrumentSymbol;
        this.quantity         = b.quantity;
        this.price            = b.price;
        this.currency         = b.currency;
        this.side             = b.side;
        this.tradeDate        = b.tradeDate;
        this.counterpartyId   = b.counterpartyId;
    }

    /**
     * Start building an equity trade.
     * @return a new Builder instance
     */
    public static Builder builder() { return new Builder(); }

    /**
     * @return the equity trade reference
     */
    @Override public TradeRef tradeRef()    { return tradeRef; }

    /**
     * @return the business date this equity trade was entered
     */
    @Override public LocalDate tradeDate()  { return tradeDate; }

    /**
     * @return the asset class for this trade
     */
    @Override public AssetClass assetClass(){ return AssetClass.EQUITY; }

    /**
     * Notional = quantity * price in the trade currency.
     * @return the computed notional amount
     */
    @Override
    public Money notional() {
         return new Money(quantity.multiply(price), currency);
    }

    /**
     * @return the traded instrument symbol
     */
    public String instrumentSymbol() { return instrumentSymbol; }

    /**
     * @return the quantity of shares traded
     */
    public BigDecimal quantity()     { return quantity; }

    /**
     * @return the price per share for the trade
     */
    public BigDecimal price()        { return price; }

    /**
     * @return the currency in which the trade is denominated
     */
    public Currency currency()       { return currency; }

    /**
     * @return the buy/sell side of the trade
     */
    public Side side()               { return side; }

    /**
     * @return the counterparty identifier for the trade
     */
    public long counterpartyId()     { return counterpartyId; }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof EquityTrade other) && tradeRef.equals(other.tradeRef);
    }

    /**
     * {@inheritDoc}
     */
    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    /**
     * PII-safe string representation. Omits counterparty details.
     * @return a log-safe summary of the equity trade
     */
    @Override
    public String toString() {
        return "EquityTrade[ref=%s, symbol=%s, qty=%s, price=%s %s, side=%s]"
                .formatted(tradeRef, instrumentSymbol, quantity, price, currency.getCurrencyCode(), side);
    }

    /** Fluent builder. Required fields validated in {@link #build()}. */
    public static final class Builder {
        private TradeRef tradeRef;
        private String instrumentSymbol;
        private BigDecimal quantity;
        private BigDecimal price;
        private Currency currency;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        /**
         * Set the trade reference.
         * @param v trade reference
         * @return this builder
         */
        public Builder tradeRef(TradeRef v)           { this.tradeRef = v;        return this; }

        /**
         * Set the instrument symbol.
         * @param v instrument symbol
         * @return this builder
         */
        public Builder instrumentSymbol(String v)     { this.instrumentSymbol = v; return this; }

        /**
         * Set the quantity.
         * @param v quantity of shares
         * @return this builder
         */
        public Builder quantity(BigDecimal v)         { this.quantity = v;        return this; }

        /**
         * Set the price.
         * @param v price per share
         * @return this builder
         */
        public Builder price(BigDecimal v)            { this.price = v;           return this; }

        /**
         * Set the trade currency.
         * @param v currency
         * @return this builder
         */
        public Builder currency(Currency v)           { this.currency = v;        return this; }

        /**
         * Set the trade currency by ISO code.
         * @param code ISO-4217 currency code
         * @return this builder
         */
        public Builder currency(String code)          { return currency(Currency.getInstance(code)); }

        /**
         * Set the trade side.
         * @param v BUY or SELL
         * @return this builder
         */
        public Builder side(Side v)                   { this.side = v;            return this; }

        /**
         * Set the trade date.
         * @param v trade date
         * @return this builder
         */
        public Builder tradeDate(LocalDate v)         { this.tradeDate = v;       return this; }

        /**
         * Set the counterparty identifier.
         * @param v counterparty id
         * @return this builder
         */
        public Builder counterpartyId(long v)         { this.counterpartyId = v;  return this; }

        /**
         * Build a validated EquityTrade.
         * @return a new EquityTrade instance
         * @throws NullPointerException if a required field is missing
         * @throws IllegalStateException if any invariant is violated
         */
        public EquityTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(instrumentSymbol, "instrumentSymbol");
            Objects.requireNonNull(quantity, "quantity");
            Objects.requireNonNull(price, "price");
            Objects.requireNonNull(currency, "currency");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");

            if (quantity.signum() <= 0) {
                throw new IllegalStateException("quantity must be > 0");
            }

            if (price.signum() <= 0) {
                throw new IllegalStateException("price must be > 0");
            }

            return new EquityTrade(this);
        }
    }
}
