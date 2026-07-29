package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.ReconResult;
import com.dbtraining.reconx.model.ReconSummary;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * TICKET-ADV038 — Custom collector that converts Stream<ReconResult>
 * into a ReconSummary containing total, matched, and broken counts.
 */
public class ReconSummaryCollector
        implements Collector<ReconResult, ReconSummaryCollector.Builder, ReconSummary> {


    /**
     * Mutable accumulation object used while collecting.
     */
    public static class Builder {

        private long total;
        private long matched;
        private long broken;


        public void add(ReconResult result) {
            total++;

            if (result.isMatched()) {
                matched++;
            } else {
                broken++;
            }
        }


        public void combine(Builder other) {
            this.total += other.total;
            this.matched += other.matched;
            this.broken += other.broken;
        }


        public ReconSummary build() {
            return new ReconSummary(total, matched, broken);
        }
    }


    @Override
    public Supplier<Builder> supplier() {
        return Builder::new;
    }


    @Override
    public BiConsumer<Builder, ReconResult> accumulator() {
        return Builder::add;
    }


    @Override
    public BinaryOperator<Builder> combiner() {
        return (left, right) -> {
            left.combine(right);
            return left;
        };
    }


    @Override
    public Function<Builder, ReconSummary> finisher() {
        return Builder::build;
    }


    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}