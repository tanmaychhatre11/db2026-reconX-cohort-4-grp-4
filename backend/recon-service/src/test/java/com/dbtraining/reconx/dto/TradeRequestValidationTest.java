package com.dbtraining.reconx.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TradeRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validTradeRequestShouldHaveNoViolations() {
        TradeRequest request = new TradeRequest(
                "EQU-20260602-0001",
                1L,
                10L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("150.25"),
                LocalDate.of(2026, 6, 2)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeQuantityShouldFailValidation() {
        TradeRequest request = new TradeRequest(
                "EQU-20260602-0001",
                1L,
                10L,
                "EQUITY",
                "BUY",
                new BigDecimal("-100"),
                new BigDecimal("150.25"),
                LocalDate.of(2026, 6, 2)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());

        ConstraintViolation<TradeRequest> violation =
                violations.iterator().next();

        assertEquals("quantity", violation.getPropertyPath().toString());
    }

    @Test
    void negativePriceShouldFailValidation() {
        TradeRequest request = new TradeRequest(
                "EQU-20260602-0001",
                1L,
                10L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("-1"),
                LocalDate.of(2026, 6, 2)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("price",
                violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void invalidTradeRefShouldFailValidation() {
        TradeRequest request = new TradeRequest(
                "INVALID",
                1L,
                10L,
                "EQUITY",
                "BUY",
                new BigDecimal("100"),
                new BigDecimal("150"),
                LocalDate.of(2026, 6, 2)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("tradeRef"))
        );
    }

    @Test
    void invalidSideShouldFailValidation() {
        TradeRequest request = new TradeRequest(
                "EQU-20260602-0001",
                1L,
                10L,
                "EQUITY",
                "HOLD",
                new BigDecimal("100"),
                new BigDecimal("150"),
                LocalDate.of(2026, 6, 2)
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.stream()
                        .anyMatch(v -> v.getPropertyPath().toString().equals("side"))
        );
    }

    @Test
    void nullFieldsShouldFailValidation() {
        TradeRequest request = new TradeRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<TradeRequest>> violations =
                validator.validate(request);

        assertEquals(8, violations.size());
    }
}