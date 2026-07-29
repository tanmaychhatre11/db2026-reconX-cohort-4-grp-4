package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        List<ReconResult> results = engine.reconcile(
                List.of(equity("EQU-20260602-0001", "100.00", "10")),
                List.of(equity("EQU-20260602-0001", "100.00", "10")),
                ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260602-0001");
    }

    @Test
    void testReconcile_priceTolerance_withinThreshold() {
        List<ReconResult> results = engine.reconcile(
                List.of(equity("EQU-20260602-0002", "100.00", "10")),
                List.of(equity("EQU-20260602-0002", "100.50", "10")),
                ReconciliationRule.PRICE_TOLERANCE_1PCT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        List<ReconResult> results = engine.reconcile(
                List.of(equity("EQU-20260602-0003", "100.00", "10")),
                List.of(),
                ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.BREAK);
        assertThat(results.get(0).discrepancyType()).isEqualTo("MISSING_EXTERNAL");
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        List<ReconResult> results = engine.reconcile(
                List.of(),
                List.of(),
                ReconciliationRule.EXACT);

        assertThat(results).isEmpty();
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
