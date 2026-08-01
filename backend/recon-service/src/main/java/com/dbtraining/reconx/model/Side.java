package com.dbtraining.reconx.model;

/**
 * Represents the direction of a trade.
 *
 * <p>A trade is either a purchase ({@link #BUY}) or a sale ({@link #SELL}).
 * Using an enum instead of a {@code String} provides compile-time type safety
 * and prevents invalid trade directions from being represented.</p>
 */
public enum Side {

    /**
     * Indicates that the trade is a purchase of the instrument.
     */
    BUY,

    /**
     * Indicates that the trade is a sale of the instrument.
     */
    SELL
}