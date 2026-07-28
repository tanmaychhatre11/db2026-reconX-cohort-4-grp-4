package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class TradeTypeOrderingTest {

    @Test
    void heterogeneousTreeSet_sortsNewestTradeDateFirst_noNpe() {
        EquityTrade equity = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260601-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 1))
                .counterpartyId(1L).build();

        FXTrade fx = FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260603-0001"))
                .ccy1("EUR").ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();

        BondTrade bond = BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260602-0001"))
                .isin("DE0001102309")
                .faceValue(new BigDecimal("1000000"))
                .couponRate(new BigDecimal("0.025"))
                .maturityDate(LocalDate.of(2030, 6, 3))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(1L).build();

        DerivativeTrade derivative = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260604-0001"))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2026, 12, 18))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 4))
                .counterpartyId(1L).build();

        TreeSet<TradeType> set = new TreeSet<>();
        set.add(equity);
        set.add(fx);
        set.add(bond);
        set.add(derivative);

        // newest tradeDate first: 6/4 (derivative), 6/3 (fx), 6/2 (bond), 6/1 (equity)
        assertThat(set).containsExactly(derivative, fx, bond, equity);
    }

    @Test
    void sameTradeDate_tiesBrokenByTradeRefAscending() {
        EquityTrade a = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0002"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();

        EquityTrade b = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();

        // same tradeDate, so tiebreak is tradeRef ascending: 0001 before 0002
        assertThat(a.compareTo(b)).isGreaterThan(0);
        assertThat(b.compareTo(a)).isLessThan(0);

        TreeSet<TradeType> set = new TreeSet<>(List.of(a, b));
        assertThat(set).containsExactly(b, a);
    }

    @Test
    void compareTo_returnsZero_onlyWhenTradeRefEqual() {
        EquityTrade a = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();

        // same tradeRef, different other fields (qty/price differ)
        EquityTrade sameRef = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("999"))
                .price(new BigDecimal("50"))
                .currency("EUR").side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(2L).build();

        // different tradeRef, everything else identical
        EquityTrade differentRef = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0002"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();

        assertThat(a.compareTo(sameRef)).isEqualTo(0);
        assertThat(a.compareTo(differentRef)).isNotEqualTo(0);
    }
}