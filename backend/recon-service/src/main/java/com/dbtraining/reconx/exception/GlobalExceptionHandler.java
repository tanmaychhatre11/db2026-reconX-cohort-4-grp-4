package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;
/**
 * ============================================================================
 * TICKET-ADV062 — RFC 7807 ProblemDetail for every ReconException
 *
 * Maps domain-specific exceptions to appropriate HTTP status codes and
 * returns RFC 9457 {@link ProblemDetail} responses for REST clients.
 * This ensures that all API errors have a consistent, machine-readable
 * structure.
 * ============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    /**
     * Handles requests for trades that do not exist.
     *
     * @param ex the exception indicating that the requested trade could not be found
     * @return a {@link ProblemDetail} with HTTP 404 (Not Found)
     */
    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail notFound(TradeNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("https://reconx.dbtraining.com/errors/trade-not-found"));
        pd.setTitle("Trade not found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    /**
     * Handles attempts to create a trade with a duplicate trade reference.
     *
     * @param ex the exception indicating that the trade reference already exists
     * @return a {@link ProblemDetail} with HTTP 409 (Conflict)
     */
    @ExceptionHandler(DuplicateTradeRefException.class)
    public ProblemDetail duplicate(DuplicateTradeRefException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("https://reconx.dbtraining.com/errors/duplicate-trade-ref"));
        pd.setTitle("Duplicate trade reference");
        return pd;
    }

    /**
     * Handles invalid trade data supplied by the client.
     *
     * @param ex the exception describing why the trade is invalid
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(InvalidTradeException.class)
    public ProblemDetail invalid(InvalidTradeException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("https://reconx.dbtraining.com/errors/invalid-trade"));
        pd.setTitle("Invalid trade");
        return pd;
    }
    /**
     * Handles reconciliation mismatches detected during trade reconciliation.
     *
     * @param ex the exception describing the reconciliation mismatch
     * @return a {@link ProblemDetail} with HTTP 422 (Unprocessable Entity)
     */
    @ExceptionHandler(ReconciliationMismatchException.class)
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setType(URI.create("https://reconx.dbtraining.com/errors/reconciliation-mismatch"));
        pd.setTitle("Reconciliation mismatch");
        return pd;
    }

    /**
     * Handles bean validation failures for request bodies.
     *
     * @param ex the validation exception containing field-level validation errors
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        pd.setTitle("Validation failed");
        return pd;
    }

    /**
     * Handles constraint violations raised outside request-body validation,
     * such as method parameter validation.
     *
     * @param ex the constraint violation exception
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail constraint(ConstraintViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Catch-all handler for any exception not covered by a more specific
     * handler above. Ensures every unhandled error still returns a
     * consistent {@code application/problem+json} response instead of
     * Spring's default error body, and logs the full stack trace for
     * diagnosis since the response itself hides the internal details.
     *
     * @param ex the uncaught exception
     * @return a {@link ProblemDetail} with HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred — please contact support with the correlationId");
        pd.setTitle("Internal server error");
        return pd;
    }
}