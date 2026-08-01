package com.dbtraining.reconx.exception;

/**
 * Exception thrown when a requested reconciliation break cannot be found.
 * <p>
 * This exception is typically raised when a client attempts to retrieve
 * or resolve a {@code ReconBreak} using an identifier that does not exist.
 * It is translated by the global exception handler into an HTTP 404
 * (Not Found) response.
 */
public class ReconBreakNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for a missing reconciliation break.
     *
     * @param id the identifier of the reconciliation break that could not be found
     */
    public ReconBreakNotFoundException(Long id) {
        super("Recon break not found: " + id);
    }

    /**
     * Creates a new exception for a missing reconciliation break.
     *
     * @param id the identifier of the reconciliation break that could not be found
     */
    public ReconBreakNotFoundException(String id) {
        super("Recon break not found: " + id);
    }
}