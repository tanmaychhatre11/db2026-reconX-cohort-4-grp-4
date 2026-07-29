package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FXTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        FXTrade trade = sampleFx("FXT-20260603-0001");

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("FXT-20260603-0001"));
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.FX);
        // notional = notionalCcy1 * fxRate, quoted in ccy2
        assertThat(trade.notional().amount()).isEqualByComparingTo(new BigDecimal("110000"));
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void builder_missingFxRate_throws() {
        assertThatThrownBy(() -> FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260603-0001"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                // .fxRate(...) intentionally omitted
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("fxRate");
    }

    @Test
    void builder_equalCurrencies_throws() {
        assertThatThrownBy(() -> FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260603-0002"))
                .ccy1("EUR")
                .ccy2("EUR")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("differ");
    }

    @Test
    void builder_badIsoCode_throwsAtSetterCall() {
        assertThatThrownBy(() -> FXTrade.builder().ccy1("EURR"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builder_nonPositiveFxRate_throws() {
        assertThatThrownBy(() -> FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260603-0003"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(BigDecimal.ZERO)
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("fxRate");
    }
    
    @Test
    void equality_byTradeRef() {
        FXTrade a = sampleFx("FXT-20260603-0001");
        FXTrade b = sampleFx("FXT-20260603-0001");
        FXTrade c = sampleFx("FXT-20260603-0002");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        assertThat(a).isNotEqualTo(c);
    }

    private FXTrade sampleFx(String ref) {
        return FXTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("100000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}