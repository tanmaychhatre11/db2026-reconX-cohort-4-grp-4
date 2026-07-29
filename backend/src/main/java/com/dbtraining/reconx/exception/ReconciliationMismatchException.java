package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 422 Unprocessable: internal vs external trade do not match. */
public class ReconciliationMismatchException extends ReconException {
    /**
     * Create a reconciliation mismatch exception.
     * @param message detail of the mismatch
     */
    public ReconciliationMismatchException(String message) { super(message); }
}
