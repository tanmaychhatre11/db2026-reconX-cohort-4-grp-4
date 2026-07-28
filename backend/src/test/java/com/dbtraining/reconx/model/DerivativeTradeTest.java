package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerivativeTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        DerivativeTrade trade = sampleDerivative("DER-20260603-0001");

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("DER-20260603-0001"));
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
        // notional = strike * quantity
        assertThat(trade.notional().amount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void builder_missingStrike_throws() {
        assertThatThrownBy(() -> DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260603-0001"))
                .underlying("AAPL")
                // .strike(...) intentionally omitted
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2026, 12, 18))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("strike");
    }

    @Test
    void builder_expiryBeforeTradeDate_throws() {
        assertThatThrownBy(() -> DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260603-0002"))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2020, 1, 1)) // before tradeDate
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expiry");
    }

    @Test
    void builder_allowsExpiryAlreadyInThePast_relativeToNow() {
        // A historical option that has already expired (relative to today) but is
        // still after tradeDate is valid — must NOT be rejected against LocalDate.now().
        DerivativeTrade trade = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260603-0003"))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2020, 6, 1)) // long past, but after tradeDate below
                .optionType(DerivativeTrade.OptionType.PUT)
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2020, 1, 1))
                .counterpartyId(1L)
                .build();

        assertThat(trade.expiry()).isEqualTo(LocalDate.of(2020, 6, 1));
    }

    @Test
    void equality_byTradeRef() {
        // TODO(TICKET-ADV028): two DerivativeTrades with the same tradeRef are equal and share hashCode;
        //                     a third with a different tradeRef is not equal.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV028 not implemented yet");
    }

    private DerivativeTrade sampleDerivative(String ref) {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .underlying("AAPL")
                .strike(new BigDecimal("100"))
                .quantity(new BigDecimal("50"))
                .expiry(LocalDate.of(2026, 12, 18))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}