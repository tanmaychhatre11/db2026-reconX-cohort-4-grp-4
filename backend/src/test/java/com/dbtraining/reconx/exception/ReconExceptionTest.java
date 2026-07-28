package com.dbtraining.reconx.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReconExceptionTest {

    @Test
    void reconException_isAbstract() {
        assertThat(java.lang.reflect.Modifier.isAbstract(ReconException.class.getModifiers()))
                .isTrue();
    }

    @Test
    void reconException_extendsRuntimeException() {
        assertThat(RuntimeException.class.isAssignableFrom(ReconException.class)).isTrue();
    }

    // ---- InvalidTradeException ----

    @Test
    void invalidTradeException_messageConstructor() {
        InvalidTradeException e = new InvalidTradeException("bad trade");

        assertThat(e).isInstanceOf(ReconException.class);
        assertThat(e.getMessage()).isEqualTo("bad trade");
    }

    @Test
    void invalidTradeException_messageAndCauseConstructor_preservesCause() {
        Throwable cause = new IllegalStateException("root cause");
        InvalidTradeException e = new InvalidTradeException("bad trade");
        e.initCause(cause);

        assertThat(e.getMessage()).isEqualTo("bad trade");
        assertThat(e.getCause()).isSameAs(cause);
    }

    // ---- TradeNotFoundException ----

    @Test
    void tradeNotFoundException_messageConstructor() {
        TradeNotFoundException e = new TradeNotFoundException("trade not found: EQU-20260602-0001");

        assertThat(e).isInstanceOf(ReconException.class);
        assertThat(e.getMessage()).contains("EQU-20260602-0001");
    }

    @Test
    void tradeNotFoundException_messageAndCauseConstructor_preservesCause() {
        Throwable cause = new RuntimeException("db timeout");
        TradeNotFoundException e = new TradeNotFoundException("trade not found: EQU-20260602-0001");
        e.initCause(cause);

        assertThat(e.getCause()).isSameAs(cause);
    }

    // ---- DuplicateTradeRefException ----

    @Test
    void duplicateTradeRefException_messageConstructor() {
        DuplicateTradeRefException e = new DuplicateTradeRefException("duplicate tradeRef: EQU-20260602-0001");

        assertThat(e).isInstanceOf(ReconException.class);
        assertThat(e.getMessage()).contains("EQU-20260602-0001");
    }

    @Test
    void duplicateTradeRefException_messageAndCauseConstructor_preservesCause() {
        Throwable cause = new RuntimeException("constraint violation");
        DuplicateTradeRefException e = new DuplicateTradeRefException("duplicate tradeRef: EQU-20260602-0001");
        e.initCause(cause);

        assertThat(e.getCause()).isSameAs(cause);
    }

    // ---- ReconciliationMismatchException ----

    @Test
    void reconciliationMismatchException_messageConstructor() {
        ReconciliationMismatchException e = new ReconciliationMismatchException("price mismatch");

        assertThat(e).isInstanceOf(ReconException.class);
        assertThat(e.getMessage()).isEqualTo("price mismatch");
    }

    @Test
    void reconciliationMismatchException_messageAndCauseConstructor_preservesCause() {
        Throwable cause = new RuntimeException("upstream feed error");
        ReconciliationMismatchException e = new ReconciliationMismatchException("price mismatch");
        e.initCause(cause);

        assertThat(e.getMessage()).isEqualTo("price mismatch");
        assertThat(e.getCause()).isSameAs(cause);
    }

    // ---- Structural: all four subtypes extend ReconException ----

    @Test
    void allFourSubtypes_extendReconException() {
        assertThat(ReconException.class.isAssignableFrom(InvalidTradeException.class)).isTrue();
        assertThat(ReconException.class.isAssignableFrom(TradeNotFoundException.class)).isTrue();
        assertThat(ReconException.class.isAssignableFrom(DuplicateTradeRefException.class)).isTrue();
        assertThat(ReconException.class.isAssignableFrom(ReconciliationMismatchException.class)).isTrue();
    }
}