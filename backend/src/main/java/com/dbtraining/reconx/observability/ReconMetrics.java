package com.dbtraining.reconx.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * WHAT: Wraps a single Micrometer Timer measuring reconciliation-engine wall time.
 * HOW:  Built once at bean construction (never inside a method — Micrometer would
 *       reject re-registering a meter with the same name on every call).
 * WHY:  publishPercentileHistogram() emits the _bucket series that lets Prometheus
 *       compute server-side percentiles via histogram_quantile(); publishPercentiles(...)
 *       additionally pre-computes client-side percentiles as a separate _quantile
 *       series, useful for non-Prometheus backends.
 */
@Component
public class ReconMetrics {

    private final Timer reconciliationTimer;

    public ReconMetrics(MeterRegistry registry) {
        this.reconciliationTimer = Timer.builder("reconciliation_duration_seconds")
                .description("Wall time of a single reconciliation engine run")
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public Timer reconciliationTimer() {
        return reconciliationTimer;
    }
}