package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BondTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        BondTrade trade = sampleBond("BND-20260603-0001");

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("BND-20260603-0001"));
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.BOND);
        // notional is valued at face
        assertThat(trade.notional().amount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    void builder_missingFaceValue_throws() {
        assertThatThrownBy(() -> BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260603-0001"))
                .isin("DE0001102309")
                // .faceValue(...) intentionally omitted
                .couponRate(new BigDecimal("0.025"))
                .maturityDate(LocalDate.of(2030, 6, 3))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("faceValue");
    }

    @Test
    void builder_maturityBeforeTradeDate_throws() {
        assertThatThrownBy(() -> BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260603-0002"))
                .isin("DE0001102309")
                .faceValue(new BigDecimal("1000000"))
                .couponRate(new BigDecimal("0.025"))
                .maturityDate(LocalDate.of(2020, 1, 1)) // before tradeDate
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("maturityDate");
    }

    @Test
    void equality_byTradeRef() {
        // TODO(TICKET-ADV028): two BondTrades with the same tradeRef are equal and share hashCode;
        //                     a third with a different tradeRef is not equal.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV028 not implemented yet");
    }

    private BondTrade sampleBond(String ref) {
        return BondTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .isin("DE0001102309")
                .faceValue(new BigDecimal("1000000"))
                .couponRate(new BigDecimal("0.025"))
                .maturityDate(LocalDate.of(2030, 6, 3))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}