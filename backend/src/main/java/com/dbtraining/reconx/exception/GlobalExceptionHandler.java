package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    /**
     * Handles requests for trades that do not exist.
     *
     * @param ex the exception indicating that the requested trade could not be found
     * @return a {@link ProblemDetail} with HTTP 404 (Not Found)
     */
    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail notFound(TradeNotFoundException ex) {
        // TODO(TICKET-ADV062): return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    /**
     * Handles attempts to create a trade with a duplicate trade reference.
     *
     * @param ex the exception indicating that the trade reference already exists
     * @return a {@link ProblemDetail} with HTTP 409 (Conflict)
     */
    @ExceptionHandler(DuplicateTradeRefException.class)
    public ProblemDetail duplicate(DuplicateTradeRefException ex) {
        // TODO(TICKET-ADV062): map DuplicateTradeRefException -> HttpStatus.CONFLICT (409).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    /**
     * Handles invalid trade data supplied by the client.
     *
     * @param ex the exception describing why the trade is invalid
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(InvalidTradeException.class)
    public ProblemDetail invalid(InvalidTradeException ex) {
        // TODO(TICKET-ADV062): map InvalidTradeException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    /**
     * Handles reconciliation mismatches detected during trade reconciliation.
     *
     * @param ex the exception describing the reconciliation mismatch
     * @return a {@link ProblemDetail} with HTTP 422 (Unprocessable Entity)
     */
    @ExceptionHandler(ReconciliationMismatchException.class)
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        // TODO(TICKET-ADV062): map ReconciliationMismatchException -> HttpStatus.UNPROCESSABLE_ENTITY (422).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }

    /**
     * Handles bean validation failures for request bodies.
     *
     * @param ex the validation exception containing field-level validation errors
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        // TODO(TICKET-ADV062): join field errors ("field: message; ...") and return BAD_REQUEST ProblemDetail.
        //   Hint: ex.getBindingResult().getFieldErrors().stream().map(...).collect(Collectors.joining("; "))
        throw new UnsupportedOperationException("TICKET-ADV062");
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
        // TODO(TICKET-ADV062): map ConstraintViolationException -> HttpStatus.BAD_REQUEST (400).
        throw new UnsupportedOperationException("TICKET-ADV062");
    }
}