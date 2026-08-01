package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
// import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
// import java.util.ArrayList;
import com.dbtraining.reconx.observability.ReconConfigMBean;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 * TICKET-ADV037 — CompletableFuture: parallel recon by counterparty
 * TICKET-ADV047 — Edge cases: empty/single/all-mismatched inputs handled
 * TICKET-ADV084 — @Timed exports reconciliation_duration_seconds histogram
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

        // private final ExecutorService executor = Executors.newFixedThreadPool(4);
        private final ReconConfigMBean reconConfig;

        public ReconciliationEngine(ReconConfigMBean reconConfig) {
                this.reconConfig = reconConfig;
        }

        // @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()", percentiles = { 0.5, 0.95,
        //                 0.99 }, histogram = true)
        public List<ReconResult> reconcile(List<TradeType> internal,
                        List<TradeType> external,
                        ReconciliationRule rule) {

                if (internal == null || internal.isEmpty()) {
                        return List.of();
                }

                Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
                                .stream()
                                .collect(Collectors.toMap(
                                                t -> t.tradeRef().value(),
                                                Function.identity(),
                                                (a, b) -> a));

                return internal.parallelStream()
                                .map(in -> matchOne(
                                                in,
                                                externalByRef.get(in.tradeRef().value()),
                                                rule))
                                .toList();
        }

        /**
         * TICKET-ADV037 — split by counterparty and reconcile concurrently.
         */
        public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
                        Map<Long, List<TradeType>> internalByCp,
                        Map<Long, List<TradeType>> externalByCp,
                        ReconciliationRule rule) {

                List<CompletableFuture<List<ReconResult>>> futures = internalByCp.entrySet()
                                .stream()
                                .map(entry -> CompletableFuture.supplyAsync(() -> reconcile(
                                                entry.getValue(),
                                                externalByCp.getOrDefault(
                                                                entry.getKey(),
                                                                List.of()),
                                                rule)))
                                .toList();

                return CompletableFuture.allOf(
                                futures.toArray(new CompletableFuture[0]))
                                .thenApply(v -> futures.stream()
                                                .flatMap(f -> f.join().stream())
                                                .toList());
        }

        private ReconResult matchOne(TradeType internal,
                        TradeType external,
                        ReconciliationRule rule) {

                String ref = internal.tradeRef().value();

                if (external == null) {
                        return ReconResult.breakResult(
                                        ref,
                                        "MISSING_EXTERNAL",
                                        "No external trade found for " + ref);
                }

                BigDecimal[] iPair = priceQty(internal);
                BigDecimal[] ePair = priceQty(external);

                BigDecimal runtimeTolerance = BigDecimal.valueOf(reconConfig.getPriceTolerance());

                BigDecimal priceDiff = iPair[0].subtract(ePair[0]).abs();

                BigDecimal priceDiffPct =
                        iPair[0].compareTo(BigDecimal.ZERO) == 0
                                ? priceDiff
                                : priceDiff.divide(iPair[0], 10, java.math.RoundingMode.HALF_UP);

                BigDecimal qtyDiff = iPair[1].subtract(ePair[1]).abs();

                if (priceDiffPct.compareTo(runtimeTolerance) <= 0 && qtyDiff.compareTo(rule.qtyToleranceAbs()) <= 0) {
                        return ReconResult.matched(ref);
                }

                return ReconResult.breakResult(
                                ref,
                                "VALUE_MISMATCH",
                                "internal=%s/%s external=%s/%s"
                                                .formatted(
                                                                iPair[0],
                                                                iPair[1],
                                                                ePair[0],
                                                                ePair[1]));
        }

        private BigDecimal[] priceQty(TradeType t) {

                if (t instanceof com.dbtraining.reconx.model.EquityTrade e) {
                        return new BigDecimal[] {
                                        e.price(),
                                        e.quantity()
                        };
                }

                if (t instanceof com.dbtraining.reconx.model.FXTrade fx) {
                        return new BigDecimal[] {
                                        fx.fxRate(),
                                        fx.notionalCcy1()
                        };
                }

                if (t instanceof com.dbtraining.reconx.model.BondTrade b) {
                        return new BigDecimal[] {
                                        b.couponRate(),
                                        b.faceValue()
                        };
                }

                if (t instanceof com.dbtraining.reconx.model.DerivativeTrade d) {
                        return new BigDecimal[] {
                                        d.strike(),
                                        d.quantity()
                        };
                }

                throw new IllegalStateException(
                                "Unsupported trade type: " + t.getClass().getName());
        }
}
