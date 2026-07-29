package com.dbtraining.reconx.service;

import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TradeAnalyticsServiceTest {

    private final TradeAnalyticsService service = new TradeAnalyticsService();

    @Test
    void pnlByInstrument_mixedBuySellFixture_returnsHandCalculableTotals() {
        List<EquityTrade> trades = List.of(
                equity("SAP.DE", "EQU-20260603-0001", "100.00", "10", Side.BUY),
                equity("SAP.DE", "EQU-20260603-0002", "101.00", "10", Side.SELL),
                equity("BMW.DE", "EQU-20260603-0003", "50.00", "20", Side.BUY),
                equity("BMW.DE", "EQU-20260603-0004", "55.00", "20", Side.SELL),
                equity("AIR.DE", "EQU-20260603-0005", "20.00", "5", Side.SELL)
        );

        Map<String, BigDecimal> pnl = service.pnlByInstrument(trades);

        assertThat(pnl).containsEntry("SAP.DE", new BigDecimal("10.00"));
        assertThat(pnl).containsEntry("BMW.DE", new BigDecimal("100.00"));
        assertThat(pnl).containsEntry("AIR.DE", new BigDecimal("100.00"));
    }

    @Test
    void pnlByInstrument_emptyInput_returnsEmptyMap() {
        assertThat(service.pnlByInstrument(List.of())).isEmpty();
    }

    @Test
    void pnlByInstrument_parallelStreamFixture_matchesSequentialExpectation() {
        List<EquityTrade> trades = List.of(
                equity("SAP.DE", "EQU-20260603-0010", "12.50", "4", Side.BUY),
                equity("SAP.DE", "EQU-20260603-0011", "13.50", "4", Side.SELL),
                equity("BMW.DE", "EQU-20260603-0012", "8.00", "10", Side.BUY),
                equity("BMW.DE", "EQU-20260603-0013", "10.00", "10", Side.SELL)
        );

        Map<String, BigDecimal> sequential = service.pnlByInstrument(trades);
        Map<String, BigDecimal> parallel = trades.parallelStream()
                .collect(java.util.stream.Collectors.groupingBy(
                        EquityTrade::instrumentSymbol,
                        java.util.stream.Collectors.mapping(
                                trade -> trade.side() == Side.SELL
                                        ? trade.price().multiply(trade.quantity())
                                        : trade.price().multiply(trade.quantity()).negate(),
                                java.util.stream.Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        assertThat(parallel).isEqualTo(sequential);
    }

    private EquityTrade equity(String symbol, String ref, String price, String qty, Side side) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol(symbol)
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR")
                .side(side)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
