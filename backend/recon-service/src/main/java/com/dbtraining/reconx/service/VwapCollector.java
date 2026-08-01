package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;

import java.math.BigDecimal;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public class VwapCollector implements Collector<EquityTrade, VwapCollector.Accumulator, BigDecimal> {

    static class Accumulator {
        private BigDecimal totalValue = BigDecimal.ZERO;
        private BigDecimal totalQuantity = BigDecimal.ZERO;
    }

    @Override
    public Supplier<Accumulator> supplier() {
        return Accumulator::new;
    }

    @Override
    public BiConsumer<Accumulator, EquityTrade> accumulator() {
        return (acc, trade) -> {
            acc.totalValue = acc.totalValue.add(
                    trade.price().multiply(trade.quantity())
            );
            acc.totalQuantity = acc.totalQuantity.add(
                    trade.quantity()
            );
        };
    }

    @Override
    public BinaryOperator<Accumulator> combiner() {
        return (left, right) -> {
            Accumulator combined = new Accumulator();
            combined.totalValue = left.totalValue.add(right.totalValue);
            combined.totalQuantity = left.totalQuantity.add(right.totalQuantity);
            return combined;
        };
    }

    @Override
    public Function<Accumulator, BigDecimal> finisher() {
        return acc -> {
            if (acc.totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            return acc.totalValue.divide(
                    acc.totalQuantity,
                    6,
                    java.math.RoundingMode.HALF_UP
            );
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}