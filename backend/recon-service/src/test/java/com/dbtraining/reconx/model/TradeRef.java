package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeRefTest {

    @Test
    void of_validFormat_succeeds() {
        TradeRef ref = TradeRef.of("EQU-20260602-0001");

        assertThat(ref.value()).isEqualTo("EQU-20260602-0001");
        assertThat(ref.toString()).isEqualTo("EQU-20260602-0001");
    }

    @Test
    void of_invalidFormat_throwsNamingExpectedShape() {
        assertThatThrownBy(() -> TradeRef.of("foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AAA-YYYYMMDD-NNNN");
    }

    @Test
    void of_nullValue_throws() {
        assertThatThrownBy(() -> TradeRef.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void of_wrongPrefixLength_throws() {
        // 2 letters instead of 3
        assertThatThrownBy(() -> TradeRef.of("EQ-20260602-0001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_lowercasePrefix_throws() {
        assertThatThrownBy(() -> TradeRef.of("equ-20260602-0001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void of_wrongDigitGroupLengths_throws() {
        // date group too short, suffix group too long
        assertThatThrownBy(() -> TradeRef.of("EQU-2026060-00001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_sameValue_areEqual() {
        assertThat(TradeRef.of("EQU-20260602-0001"))
                .isEqualTo(TradeRef.of("EQU-20260602-0001"));
        assertThat(TradeRef.of("EQU-20260602-0001").hashCode())
                .isEqualTo(TradeRef.of("EQU-20260602-0001").hashCode());
    }

    @Test
    void equals_differentValue_areNotEqual() {
        assertThat(TradeRef.of("EQU-20260602-0001"))
                .isNotEqualTo(TradeRef.of("EQU-20260602-0002"));
    }
}