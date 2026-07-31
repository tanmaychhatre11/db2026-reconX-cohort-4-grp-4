package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.observability.ReconMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReconciliationServiceTest {

    @Test
    void testRunRecon_returnsMatchedResult() {

        ReconciliationEngine engine = new ReconciliationEngine();

        ReconMetrics metrics = mock(ReconMetrics.class);
        io.micrometer.core.instrument.Timer timer =
                mock(io.micrometer.core.instrument.Timer.class);

        when(metrics.reconciliationTimer()).thenReturn(timer);
        when(timer.record(org.mockito.ArgumentMatchers.<java.util.function.Supplier<List<ReconResult>>>any()))
                .thenAnswer(invocation -> {
                    java.util.function.Supplier<List<ReconResult>> supplier =
                            invocation.getArgument(0);
                    return supplier.get();
                });

        ReconciliationService service =
                new ReconciliationService(engine, metrics);

        EquityTrade internal = equity(
                "EQU-20260603-0001",
                "100.00",
                "10"
        );

        EquityTrade external = equity(
                "EQU-20260603-0001",
                "100.00",
                "10"
        );

        List<ReconResult> results = service.runRecon(
                List.of(internal),
                List.of(external),
                ReconciliationRule.EXACT
        );

        assertThat(results).hasSize(1);

        ReconResult result = results.get(0);

        assertThat(result.tradeRef())
                .isEqualTo("EQU-20260603-0001");

        assertThat(result.status())
                .isEqualTo(ReconResult.Status.MATCHED);
    }

    private EquityTrade equity(String ref,
                               String price,
                               String qty) {

        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}