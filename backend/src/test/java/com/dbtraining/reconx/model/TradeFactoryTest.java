package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeFactoryTest {

    @Test
    void create_equity_returnsTypedEquityTrade() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "EQU-20260603-0001");
        p.put("symbol", "SAP.DE");
        p.put("quantity", "100");
        p.put("price", "100");
        p.put("currency", "EUR");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1);

        TradeType trade = TradeFactory.create("EQUITY", p);

        assertThat(trade).isInstanceOf(EquityTrade.class);
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.EQUITY);
    }

    @Test
    void create_fx_returnsTypedFxTrade() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "FXT-20260603-0001");
        p.put("ccy1", "EUR");
        p.put("ccy2", "USD");
        p.put("notionalCcy1", "100000");
        p.put("fxRate", "1.10");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1);

        TradeType trade = TradeFactory.create("FX", p);

        assertThat(trade).isInstanceOf(FXTrade.class);
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.FX);
    }

    @Test
    void create_bond_returnsTypedBondTrade() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "BND-20260603-0001");
        p.put("isin", "DE0001102309");
        p.put("faceValue", "1000000");
        p.put("couponRate", "0.025");
        p.put("maturityDate", "2030-06-03");
        p.put("currency", "EUR");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1);

        TradeType trade = TradeFactory.create("BOND", p);

        assertThat(trade).isInstanceOf(BondTrade.class);
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.BOND);
    }

    @Test
    void create_derivative_returnsTypedDerivativeTrade() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "DER-20260603-0001");
        p.put("underlying", "AAPL");
        p.put("strike", "100");
        p.put("quantity", "50");
        p.put("expiry", "2026-12-18");
        p.put("optionType", "CALL");
        p.put("currency", "USD");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1);

        TradeType trade = TradeFactory.create("DERIVATIVE", p);

        assertThat(trade).isInstanceOf(DerivativeTrade.class);
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }

    @Test
    void create_unknownDiscriminator_throws() {
        Map<String, Object> p = new HashMap<>();

        assertThatThrownBy(() -> TradeFactory.create("FOO", p))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_missingRequiredKey_throwsNullPointerException() {
        Map<String, Object> p = new HashMap<>();
        p.put("tradeRef", "EQU-20260603-0002");
        p.put("symbol", "SAP.DE");
        p.put("quantity", "100");
        // "price" intentionally omitted
        p.put("currency", "EUR");
        p.put("side", "BUY");
        p.put("tradeDate", "2026-06-03");
        p.put("counterpartyId", 1);

        assertThatThrownBy(() -> TradeFactory.create("EQUITY", p))
            .isInstanceOf(NullPointerException.class);
    }
}