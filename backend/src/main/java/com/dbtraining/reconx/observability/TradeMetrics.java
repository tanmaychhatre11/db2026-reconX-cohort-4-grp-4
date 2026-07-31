package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import com.dbtraining.reconx.repository.TradeRepository;

/**
 * ============================================================================
 * TICKET-ADV083 — trade_created_total Counter
 * TICKET-ADV085 — recon_break_count Gauge (polled — wraps repo.countByStatus)
 * TICKET-ADV086 — trade_value_total DistributionSummary
 *
 * WHAT:    Holds Micrometer instruments published to /actuator/prometheus.
 * HOW:     Counters / Distribution Summaries are constructed once in the
 *          constructor and stored as final fields. Gauges are "polled" —
 *          Micrometer holds a weak reference and calls the lambda on scrape.
 * WHY:     Three different metric shapes matter:
 *            - Counter: monotonic count of events (created trades)
 *            - DistributionSummary: histogram of magnitudes (trade values)
 *            - Gauge: instantaneous value (open recon breaks)
 *
 * The TIMER for reconciliation duration lives as @Timed on
 * ReconciliationEngine.reconcile() (TICKET-ADV084) — not in this class.
 * ============================================================================
 */
@Component
public class TradeMetrics {

    private final Counter tradeCreated;
    private final DistributionSummary tradeValue;

    public TradeMetrics(MeterRegistry registry, ReconBreakRepository breakRepo) {
        this.tradeCreated = Counter.builder("trade_creation_total")
                .description("Total trades created")
                .register(registry);

        this.tradeValue = DistributionSummary.builder("trade_value_total")
                .description("Distribution of trade notional values")
                .baseUnit("USD")
                .publishPercentileHistogram()
                .register(registry);

        // TICKET-ADV085 — polled gauge wrapping a repository count.
        Gauge.builder("recon_break_count", breakRepo, r -> r.countByStatus("OPEN"))
                .description("Open recon breaks")
                .register(registry);

        String[] statuses = {
                "PENDING",
                "MATCHED",
                "UNMATCHED",
                "DISPUTED",
                "CANCELLED"
        };

        for (String status : statuses) {
            Gauge.builder("trades_by_status",
                    tradeRepo,
                    repo -> repo.countByStatus(status))
                    .description("Trades count by status")
                    .tag("status", status)
                    .register(registry);
        }
    }

   public void incrementTradeCreated() {
        tradeCreated.increment();
    }

    public void recordTradeValue(double value) {
        tradeValue.record(value);
    }
}
