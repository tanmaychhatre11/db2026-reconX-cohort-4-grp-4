package com.dbtraining.reconx.model;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * ============================================================================
 * TICKET-ADV018 — Sealed interface TradeType
 *
 * WHAT:    Sealed root of the trade hierarchy. Only the four named
 *          permitted classes can implement it. Any new asset class needs an
 *          explicit code change here — by design.
 * HOW:     `sealed ... permits ...` on Java 21.
 * WHY:     Without sealing, anyone could write their own `Trade` subclass and
 *          slip through the reconciliation engine's pattern-matching switch.
 *          Sealing turns the engine's switch into an exhaustive one — the
 *          compiler enforces that every case is handled.
 * OBSERVE: Removing `permits BondTrade` causes a compile error in
 *          ReconciliationEngine's switch expression.
 * HINT:    See Day 2 trainer guide §"Sprint 1A — sealed hierarchy" for the
 *          design discussion.
 * ============================================================================
 *
 * TICKET-ADV027 — Comparable natural ordering (most-recent trade first)
 * TICKET-ADV028 — equals/hashCode based on tradeRef (the natural key)
 *
 * Comparator lives on the sealed interface, so every impl shares the same
 * ordering rule — there is no per-class compareTo override to forget to
 * update when adding a new field.
 */
public sealed interface TradeType
        extends Comparable<TradeType>
        permits EquityTrade, FXTrade, BondTrade, DerivativeTrade {

    /**
     * Stable natural key for the trade.
     * @return the trade reference
     */
    TradeRef tradeRef();

    /**
     * Reconciliation notional for this trade.
     * @return a Money instance representing the trade value
     */
    Money notional();

    /**
     * Business date the trade was entered.
     * @return the trade date
     */
    LocalDate tradeDate();

    /**
     * Asset class discriminator for routing and persistence.
     * @return the trade's asset class
     */
    AssetClass assetClass();

    /**
     * Shared natural ordering for all TradeType implementations.
     * Newer trades come first, with ties broken by tradeRef.
     */
    Comparator<TradeType> NATURAL = Comparator
            .comparing(TradeType::tradeDate).reversed()
            .thenComparing(t -> t.tradeRef().value());

    /**
     * Compare this trade with another using the shared natural ordering.
     * @param other the trade to compare against
     * @return a negative integer, zero, or a positive integer as this trade is
     *         less than, equal to, or greater than the other trade
     */
    @Override
    default int compareTo(TradeType other) {
        return NATURAL.compare(this, other);
    }

    /**
     * Identifies the supported asset classes in the trading system.
     */
    enum AssetClass {

        /**
         * Equity instruments such as stocks and shares.
         */
        EQUITY,

        /**
         * Foreign exchange (FX) instruments.
         */
        FX,

        /**
         * Fixed-income instruments such as bonds.
         */
        BOND,

        /**
         * Derivative instruments such as options and futures.
         */
        DERIVATIVE
    }
}
