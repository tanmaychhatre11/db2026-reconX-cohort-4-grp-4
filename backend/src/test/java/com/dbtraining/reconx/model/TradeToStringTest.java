package com.dbtraining.reconx.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TradeToStringTest {

    @Test
    void equityTradeToStringShouldNotContainCounterpartyId() {
        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260602-0001"))
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("150.50"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(123456789L)
                .build();

        String text = trade.toString();

        assertFalse(text.contains("123456789"));
        assertTrue(text.contains("EQU-20260602-0001"));
        assertTrue(text.contains("AAPL"));
        assertTrue(text.contains("BUY"));
        assertTrue(text.contains("150.50"));
    }

    @Test
    void fxTradeToStringShouldNotContainCounterpartyId() {
        FXTrade trade = FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260602-0001"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(new BigDecimal("1.12345"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(987654321L)
                .build();

        String text = trade.toString();

        assertFalse(text.contains("987654321"));
        assertTrue(text.contains("FXT-20260602-0001"));
        assertTrue(text.contains("EUR"));
        assertTrue(text.contains("USD"));
        assertTrue(text.contains("1.12345"));
    }

    @Test
    void bondTradeToStringShouldNotContainCounterpartyId() {
        BondTrade trade = BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260602-0001"))
                .isin("US0378331005")
                .faceValue(new BigDecimal("1000"))
                .couponRate(new BigDecimal("5.25"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .maturityDate(LocalDate.of(2030, 6, 2))
                .counterpartyId(55555L)
                .build();

        String text = trade.toString();

        assertFalse(text.contains("55555"));
        assertTrue(text.contains("US0378331005"));
        assertTrue(text.contains("5.25"));
    }

    @Test
    void derivativeTradeToStringShouldNotContainCounterpartyId() {
        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260602-0001"))
                .underlying("AAPL")
                .strike(new BigDecimal("200"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2027, 6, 2))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(99999L)
                .build();

        String text = trade.toString();

        assertFalse(text.contains("99999"));
        assertTrue(text.contains("CALL"));
        assertTrue(text.contains("AAPL"));
        assertTrue(text.contains("200"));
    }

    @Test
    void bigDecimalShouldBeRenderedInPlainNotation() {
        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260602-9999"))
                .instrumentSymbol("IBM")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100.50"))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 2))
                .counterpartyId(1L)
                .build();

        String text = trade.toString();

        assertTrue(text.contains("100.50"));
        assertFalse(text.matches(".*\\dE[+-]?\\d.*"));
    }
}