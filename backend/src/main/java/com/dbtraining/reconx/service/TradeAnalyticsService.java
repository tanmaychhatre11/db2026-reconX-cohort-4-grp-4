package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV034 — Trade analytics with Collectors (groupingBy + summarizing)
 * TICKET-ADV035 — VWAP calculator using Streams + custom collector
 * TICKET-ADV036 — P&amp;L per instrument: stream reduction
 * ============================================================================
 */
@Service
public class TradeAnalyticsService {

    /** TICKET-ADV034 — count + sum of notional per counterparty. */
    public Map<Long, NotionalSummary> notionalByCounterparty(List<? extends TradeType> trades) {
        if (trades == null || trades.isEmpty()) {
            return Map.of();
        }

        return trades.stream()
                .collect(Collectors.groupingBy(
                        this::counterpartyIdOf,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new NotionalSummary(
                                        list.size(),
                                        list.stream()
                                                .map(t -> t.notional().amount())
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                ))));
    }

    /**
     * TICKET-ADV035 — VWAP = SUM(price * qty) / SUM(qty). Equity-only — only
     * EquityTrade has a meaningful price-volume pair.
     */
    public Map<String, BigDecimal> vwapByInstrument(List<EquityTrade> equityTrades) {
        if (equityTrades == null || equityTrades.isEmpty()) {
            return Map.of();
        }

        return equityTrades.stream()
                .collect(Collectors.groupingBy(
                        EquityTrade::instrumentSymbol,
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            BigDecimal totalNotional = list.stream()
                                    .map(t -> t.price().multiply(t.quantity()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal totalQty = list.stream()
                                    .map(EquityTrade::quantity)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            if (totalQty.compareTo(BigDecimal.ZERO) == 0) {
                                return BigDecimal.ZERO;
                            }
                            return totalNotional.divide(totalQty, 8, RoundingMode.HALF_UP);
                        })));
    }

    /** TICKET-ADV036 — {@code P&amp;L} per instrument symbol (sign by Side). */
    public Map<String, BigDecimal> pnlByInstrument(List<EquityTrade> equityTrades) {
        if (equityTrades == null || equityTrades.isEmpty()) {
            return Map.of();
        }

        return equityTrades.stream()
                .collect(Collectors.groupingBy(
                        EquityTrade::instrumentSymbol,
                        Collectors.mapping(this::pnl, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    private BigDecimal pnl(EquityTrade t) {
        BigDecimal abs = t.price().multiply(t.quantity());
        return t.side() == Side.SELL ? abs : abs.negate();
    }

    private long counterpartyIdOf(TradeType t) {
        if (t instanceof EquityTrade e) {
            return e.counterpartyId();
        }
        if (t instanceof com.dbtraining.reconx.model.FXTrade fx) {
            return fx.counterpartyId();
        }
        if (t instanceof com.dbtraining.reconx.model.BondTrade b) {
            return b.counterpartyId();
        }
        if (t instanceof com.dbtraining.reconx.model.DerivativeTrade d) {
            return d.counterpartyId();
        }
        throw new IllegalStateException("Unsupported trade type: " + t.getClass().getName());
    }

    public record NotionalSummary(long count, BigDecimal total) {}
}
