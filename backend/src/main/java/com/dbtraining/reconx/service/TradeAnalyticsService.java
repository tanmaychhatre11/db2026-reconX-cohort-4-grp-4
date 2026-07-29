package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.TradeType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV034 — Trade analytics with Collectors (groupingBy + summarizing)
 * TICKET-ADV035 — VWAP calculator using Streams + custom collector
 * TICKET-ADV036 — P&L per instrument: stream reduction
 * ============================================================================
 */
@Service
public class TradeAnalyticsService {

    /**
     * TICKET-ADV034 — count + sum of notional per counterparty.
     */
    public Map<Long, NotionalSummary> notionalByCounterparty(List<? extends TradeType> trades) {

        return trades.stream().collect(
                Collectors.groupingBy(
                        this::counterpartyIdOf,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> new NotionalSummary(
                                        list.size(),
                                        list.stream()
                                                .map(t -> t.notional().amount())
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                )
                        )
                )
        );
    }

    /**
     * TICKET-ADV035 — VWAP = SUM(price * qty) / SUM(qty).
     */
    public Map<String, BigDecimal> vwapByInstrument(List<EquityTrade> equityTrades) {
        throw new UnsupportedOperationException("TICKET-ADV035");
    }

    /**
     * TICKET-ADV036 — P&L per instrument.
     */
    public Map<String, BigDecimal> pnlByInstrument(List<EquityTrade> equityTrades) {
        throw new UnsupportedOperationException("TICKET-ADV036");
    }

    private BigDecimal pnl(EquityTrade t) {
        throw new UnsupportedOperationException("TICKET-ADV036");
    }

    private long counterpartyIdOf(TradeType t) {

        if (t instanceof EquityTrade e) {
            return e.counterpartyId();
        }

        if (t instanceof FXTrade fx) {
            return fx.counterpartyId();
        }

        if (t instanceof BondTrade b) {
            return b.counterpartyId();
        }

        if (t instanceof DerivativeTrade d) {
            return d.counterpartyId();
        }

        throw new IllegalArgumentException("Unknown TradeType: " + t.getClass().getName());
    }

    public record NotionalSummary(long count, BigDecimal total) {
    }
}