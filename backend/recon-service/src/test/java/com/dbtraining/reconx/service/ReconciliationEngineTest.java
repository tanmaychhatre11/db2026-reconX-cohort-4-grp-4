package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import com.dbtraining.reconx.observability.ReconConfigMBean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 * TICKET-ADV096 — priceTolerance now comes from the live ReconConfigMBean,
 *                 not from ReconciliationRule, so it's stubbed per-test here
 *                 to reproduce the old EXACT / PRICE_TOLERANCE_1PCT behavior.
 */
class ReconciliationEngineTest {

    private ReconConfigMBean reconConfig;
    private ReconciliationEngine engine;

    @BeforeEach
    void setUp() {
        reconConfig = mock(ReconConfigMBean.class);
        engine = new ReconciliationEngine(reconConfig);
    }

    @Test
    void testReconcile_exactMatch_returnsMatched() {
        when(reconConfig.getPriceTolerance()).thenReturn(0.0);

        List<ReconResult> results = engine.reconcile(
                List.of(equity("EQU-20260602-0001", "100.00", "10")),
                List.of(equity("EQU-20260602-0001", "100.00", "10")),
                ReconciliationRule.EXACT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
        assertThat(results.get(0).tradeRef()).isEqualTo("EQU-20260602-0001");
    }

    @ParameterizedTest(name = "price diff {0} stays within 1% tolerance -> MATCHED")
    @ValueSource(strings = {"0.10", "0.50", "0.99"})
    void testReconcile_priceTolerance_withinThreshold(String diff) {

        when(reconConfig.getPriceTolerance()).thenReturn(0.01);

        // given
        BigDecimal basePrice = new BigDecimal("100.00");

        EquityTrade internal = equity("EQU-20260603-0002", "100.00", "1000");

        EquityTrade external = equity(
                "EQU-20260603-0002",
                basePrice.add(new BigDecimal(diff)).toPlainString(),
                "1000"
        );

        // when
        List<ReconResult> out = engine.reconcile(
                List.of(internal),
                List.of(external),
                ReconciliationRule.PRICE_TOLERANCE_1PCT
        );

        // then
        assertThat(out).hasSize(1);
        assertThat(out.get(0).status())
                .isEqualTo(ReconResult.Status.MATCHED);
    }

    @Test
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        when(reconConfig.getPriceTolerance()).thenReturn(0.0);

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
        // no reconConfig stub needed here — matchOne() is never reached
        // since reconcile() early-returns before iterating internal trades
        List<ReconResult> results = engine.reconcile(
                List.of(),
                List.of(),
                ReconciliationRule.EXACT);

        assertThat(results).isEmpty();
    }

    @Test
    void testReconcile_allMismatched_returnsAllBreaks() {
        when(reconConfig.getPriceTolerance()).thenReturn(0.0);

        List<ReconResult> results = engine.reconcile(
                List.of(
                        equity("EQU-20260603-0001", "100.00", "10"),
                        equity("EQU-20260603-0002", "200.00", "20"),
                        equity("EQU-20260603-0003", "300.00", "30")
                ),
                List.of(
                        equity("EQU-20260603-0001", "999.00", "99"),
                        equity("EQU-20260603-0002", "999.00", "99"),
                        equity("EQU-20260603-0003", "999.00", "99")
                ),
                ReconciliationRule.EXACT
        );

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r -> r.status() == ReconResult.Status.BREAK);

        ReconSummary summary = results.stream().collect(new ReconSummaryCollector());
        assertThat(summary.total()).isEqualTo(3);
        assertThat(summary.matched()).isEqualTo(0);
        assertThat(summary.broken()).isEqualTo(3);
    }

    // TICKET-ADV096 — new test: confirms the engine reads a LIVE tolerance
    // value on every call rather than one cached at construction time.
    @Test
    void testReconcile_toleranceChangesBetweenCalls_affectsOutcome() {
        EquityTrade internal = equity("EQU-20260603-0099", "100.00", "1000");
        EquityTrade external = equity("EQU-20260603-0099", "100.50", "1000"); // 0.5% diff

        when(reconConfig.getPriceTolerance()).thenReturn(0.001); // 0.1% — too tight
        List<ReconResult> tight = engine.reconcile(
                List.of(internal), List.of(external), ReconciliationRule.PRICE_TOLERANCE_1PCT);
        assertThat(tight.get(0).status()).isEqualTo(ReconResult.Status.BREAK);

        when(reconConfig.getPriceTolerance()).thenReturn(0.01); // 1% — loose enough
        List<ReconResult> loose = engine.reconcile(
                List.of(internal), List.of(external), ReconciliationRule.PRICE_TOLERANCE_1PCT);
        assertThat(loose.get(0).status()).isEqualTo(ReconResult.Status.MATCHED);
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