package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 400 Bad Request: a trade failed business validation. */
public class InvalidTradeException extends ReconException {
    /**
     * Create an invalid trade exception.
     * @param message description of the validation failure
     */
    public InvalidTradeException(String message) { super(message); }
}
