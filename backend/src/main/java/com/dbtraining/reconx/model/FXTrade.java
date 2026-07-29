package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV020 — FXTrade with Builder pattern
 *
 * WHAT:    FX spot/forward trade — two currencies, a notional in ccy1, and
 *          an fxRate.
 * HOW:     Same builder pattern as EquityTrade. notional() converts to ccy2
 *          via fxRate so reconciliation rolls up in the trade's quote ccy.
 * WHY:     FX has two natural sides — a EUR/USD trade is BOTH a buy of EUR
 *          AND a sell of USD. Modelling that with two distinct currency
 *          fields makes settlement-side reasoning explicit.
 * OBSERVE: notional().currency() == ccy2; .amount() == notionalCcy1 * fxRate.
 * ============================================================================
 */
public final class FXTrade implements TradeType {

    private final TradeRef tradeRef;
    private final Currency ccy1;
    private final Currency ccy2;
    private final BigDecimal notionalCcy1;
    private final BigDecimal fxRate;
    private final Side side;
    private final LocalDate tradeDate;
    private final long counterpartyId;

    private FXTrade(Builder b) {
        this.tradeRef       = b.tradeRef;
        this.ccy1           = b.ccy1;
        this.ccy2           = b.ccy2;
        this.notionalCcy1   = b.notionalCcy1;
        this.fxRate         = b.fxRate;
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
    @Override public AssetClass assetClass() { return AssetClass.FX; }

    /** Notional in ccy2 = notionalCcy1 * fxRate. */
    @Override public Money notional() {
        // done(TICKET-ADV020): return new Money(notionalCcy1 * fxRate, ccy2).
        return new Money(notionalCcy1.multiply(fxRate), ccy2);
    }

    /**
     * @return the base currency in the FX pair
     */
    public Currency ccy1()           { return ccy1; }

    /**
     * @return the quote currency in the FX pair
     */
    public Currency ccy2()           { return ccy2; }

    /**
     * @return the notional amount in the base currency
     */
    public BigDecimal notionalCcy1() { return notionalCcy1; }

    /**
     * @return the FX rate from ccy1 to ccy2
     */
    public BigDecimal fxRate()       { return fxRate; }

    /**
     * @return the trade side
     */
    public Side side()               { return side; }

    /**
     * @return the counterparty identifier
     */
    public long counterpartyId()     { return counterpartyId; }

    @Override public boolean equals(Object o) {
        return (o instanceof FXTrade other) && tradeRef.equals(other.tradeRef);
    }
    @Override public int hashCode() {
        return tradeRef.hashCode();
    }

    @Override public String toString() {
        return "FXTrade[ref=%s, %s/%s, notional=%s %s, rate=%s, side=%s]"
                .formatted(tradeRef, ccy1.getCurrencyCode(), ccy2.getCurrencyCode(),
                        notionalCcy1, ccy1.getCurrencyCode(), fxRate, side);
    }

    /**
     * Builder for creating immutable BondTrade instances.
     */
    public static final class Builder {
        private TradeRef tradeRef;
        private Currency ccy1, ccy2;
        private BigDecimal notionalCcy1, fxRate;
        private Side side;
        private LocalDate tradeDate;
        private long counterpartyId;

        /**
         * Set the trade reference.
         * @param v trade reference
         * @return this builder
         */
        public Builder tradeRef(TradeRef v)        { this.tradeRef = v; return this; }

        /**
         * Set the base currency of the FX trade.
         * @param code ISO-4217 currency code
         * @return this builder
         */
        public Builder ccy1(String code)           { this.ccy1 = Currency.getInstance(code); return this; }

        /**
         * Set the quote currency of the FX trade.
         * @param code ISO-4217 currency code
         * @return this builder
         */
        public Builder ccy2(String code)           { this.ccy2 = Currency.getInstance(code); return this; }

        /**
         * Set the notional amount in base currency.
         * @param v notional amount in ccy1
         * @return this builder
         */
        public Builder notionalCcy1(BigDecimal v)  { this.notionalCcy1 = v; return this; }

        /**
         * Set the FX rate from ccy1 to ccy2.
         * @param v FX rate
         * @return this builder
         */
        public Builder fxRate(BigDecimal v)        { this.fxRate = v; return this; }

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
         * Build a validated FXTrade.
         * @return a new FXTrade
         * @throws NullPointerException if a required field is missing
         * @throws IllegalStateException if any invariant is violated
         */
        public FXTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(ccy1, "ccy1");
            Objects.requireNonNull(ccy2, "ccy2");
            Objects.requireNonNull(notionalCcy1, "notionalCcy1");
            Objects.requireNonNull(fxRate, "fxRate");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");

            if (ccy1.equals(ccy2)) {
                throw new IllegalStateException("ccy1 and ccy2 must differ");
            }

            if (fxRate.signum() <= 0) {
                throw new IllegalStateException("fxRate must be > 0");
            }

            return new FXTrade(this);
        }
    }
}
