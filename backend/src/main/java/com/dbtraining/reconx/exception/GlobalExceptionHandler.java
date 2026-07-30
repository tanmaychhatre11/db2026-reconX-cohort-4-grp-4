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
            return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
            );
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
            return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
            );
    }

    /**
     * Handles invalid trade data supplied by the client.
     *
     * @param ex the exception describing why the trade is invalid
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(InvalidTradeException.class)
    public ProblemDetail invalid(InvalidTradeException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    /**
     * Handles reconciliation mismatches detected during trade reconciliation.
     *
     * @param ex the exception describing the reconciliation mismatch
     * @return a {@link ProblemDetail} with HTTP 422 (Unprocessable Entity)
     */
    @ExceptionHandler(ReconciliationMismatchException.class)
    public ProblemDetail mismatch(ReconciliationMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
    }

    /**
     * Handles bean validation failures for request bodies.
     *
     * @param ex the validation exception containing field-level validation errors
     * @return a {@link ProblemDetail} with HTTP 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {

        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail
        );
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
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }
}