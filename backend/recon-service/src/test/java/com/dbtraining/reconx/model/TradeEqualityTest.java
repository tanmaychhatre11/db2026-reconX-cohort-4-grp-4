package com.dbtraining.reconx.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TradeEqualityTest {

    @Test
    void equityTradesWithSameTradeRefShouldBeEqual() {
        TradeRef ref = TradeRef.of("EQU-20260602-0001");

        EquityTrade t1 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(1)
                .build();

        EquityTrade t2 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("MSFT")
                .quantity(new BigDecimal("250"))
                .price(new BigDecimal("500"))
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(99)
                .build();

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test
    void hashSetShouldContainOnlyOneTradeWithSameTradeRef() {
        TradeRef ref = TradeRef.of("EQU-20260602-0002");

        EquityTrade t1 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("IBM")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.now())
                .counterpartyId(1)
                .build();

        EquityTrade t2 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("GOOG")
                .quantity(new BigDecimal("20"))
                .price(new BigDecimal("200"))
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.now())
                .counterpartyId(2)
                .build();

        Set<TradeType> trades = new HashSet<>();
        trades.add(t1);
        trades.add(t2);

        assertEquals(1, trades.size());
    }

    @Test
    void crossTypeTradesShouldNotBeEqual() {
        TradeRef ref = TradeRef.of("EQU-20260602-0003");

        EquityTrade equity = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("TSLA")
                .quantity(new BigDecimal("50"))
                .price(new BigDecimal("300"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.now())
                .counterpartyId(1)
                .build();

        FXTrade fx = FXTrade.builder()
                .tradeRef(ref)
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("1000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.now())
                .counterpartyId(1)
                .build();

        assertNotEquals(equity, fx);
        assertNotEquals(fx, equity);
    }

    @Test
    void compareToShouldBeConsistentWithEquals() {
        TradeRef ref = TradeRef.of("EQU-20260602-0004");

        EquityTrade t1 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("NFLX")
                .quantity(new BigDecimal("5"))
                .price(new BigDecimal("700"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(1)
                .build();

        EquityTrade t2 = EquityTrade.builder()
                .tradeRef(ref)
                .instrumentSymbol("AMZN")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("900"))
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(2)
                .build();

        assertEquals(t1, t2);
        assertEquals(0, t1.compareTo(t2));
    }
}