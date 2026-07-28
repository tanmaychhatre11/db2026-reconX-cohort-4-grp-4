package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * ============================================================================
 * TICKET-ADV023 — TradeFactory: build a TradeType by asset-class string
 *
 * WHAT:    Single entry point that takes an asset-class string + a map of
 *          field values and returns the right TradeType impl.
 * HOW:     Switch on the asset-class string, dispatch to the correct
 *          builder. Map values are cast/parsed per asset class.
 * WHY:     The Kafka consumer + REST POST endpoint both need to convert an
 *          untyped payload into a typed TradeType. Centralising the
 *          construction here means the parsing logic lives in one place.
 * OBSERVE: TradeFactoryTest.create_unknownAssetClass_throws fails when a
 *          new TradeType impl is added without updating the switch.
 * HINT:    Sealed hierarchy guarantees that every concrete TradeType MUST be
 *          listed in TradeType.permits — so this switch can be made
 *          exhaustive over assetClass enum.
 * ============================================================================
 */
public final class TradeFactory {

    private TradeFactory() { }

    /**
     * TODO(TICKET-ADV023):
     *   1. Parse assetClass string into TradeType.AssetClass enum (toUpperCase first).
     *   2. switch on the enum and dispatch to the matching equity/fx/bond/derivative
     *      helper below.
     *   3. The switch must be exhaustive — every TradeType.AssetClass case handled.
     */
       public static TradeType create(String assetClass, Map<String, Object> p) {

            TradeType.AssetClass type =
                TradeType.AssetClass.valueOf(assetClass.toUpperCase());

            return switch (type) {
                case EQUITY -> equity(p);
                case FX -> fx(p);
                case BOND -> bond(p);
                case DERIVATIVE -> derivative(p);
            };
        }

    /**
     * TODO(TICKET-ADV023):
     *   Build an EquityTrade from the map. Expected keys: tradeRef, symbol,
     *   quantity, price, currency, side, tradeDate, counterpartyId.
     */
    private static EquityTrade equity(Map<String, Object> p) {
        
         return EquityTrade.builder()
            .tradeRef((TradeRef) p.get("tradeRef"))
            .instrumentSymbol((String) p.get("symbol"))
            .quantity((BigDecimal) p.get("quantity"))
            .price((BigDecimal) p.get("price"))
            .currency((String) p.get("currency"))
            .side((Side) p.get("side"))
            .tradeDate((LocalDate) p.get("tradeDate"))
            .counterpartyId((long) p.get("counterpartyId"))
            .build();
    }

    /**
     * TODO(TICKET-ADV023):
     *   Build an FXTrade from the map. Expected keys: tradeRef, ccy1, ccy2,
     *   notionalCcy1, fxRate, side, tradeDate, counterpartyId.
     */
    private static FXTrade fx(Map<String, Object> p) {
        return FXTrade.builder()
            .tradeRef((TradeRef) p.get("tradeRef"))
            .ccy1((String) p.get("ccy1"))
            .ccy2((String) p.get("ccy2"))
            .notionalCcy1((BigDecimal) p.get("notionalCcy1"))
            .fxRate((BigDecimal) p.get("fxRate"))
            .side((Side) p.get("side"))
            .tradeDate((LocalDate) p.get("tradeDate"))
            .counterpartyId((long) p.get("counterpartyId"))
            .build();
    }

    /**
     * TODO(TICKET-ADV023):
     *   Build a BondTrade from the map. Expected keys: tradeRef, isin,
     *   faceValue, couponRate, maturityDate, currency, side, tradeDate,
     *   counterpartyId.
     */
    private static BondTrade bond(Map<String, Object> p) {
        return BondTrade.builder()
            .tradeRef((TradeRef) p.get("tradeRef"))
            .isin((String) p.get("isin"))
            .faceValue((BigDecimal) p.get("faceValue"))
            .couponRate((BigDecimal) p.get("couponRate"))
            .maturityDate((LocalDate) p.get("maturityDate"))
            .currency((String) p.get("currency"))
            .side((Side) p.get("side"))
            .tradeDate((LocalDate) p.get("tradeDate"))
            .counterpartyId((long) p.get("counterpartyId"))
            .build();
    }

    /**
     * TODO(TICKET-ADV023):
     *   Build a DerivativeTrade from the map. Expected keys: tradeRef,
     *   underlying, strike, quantity, expiry, optionType, currency, side,
     *   tradeDate, counterpartyId.
     */
    private static DerivativeTrade derivative(Map<String, Object> p) {
        return DerivativeTrade.builder()
            .tradeRef((TradeRef) p.get("tradeRef"))
            .underlying((String) p.get("underlying"))
            .strike((BigDecimal) p.get("strike"))
            .quantity((BigDecimal) p.get("quantity"))
            .expiry((LocalDate) p.get("expiry"))
            .optionType((DerivativeTrade.OptionType) p.get("optionType"))
            .currency((String) p.get("currency"))
            .side((Side) p.get("side"))
            .tradeDate((LocalDate) p.get("tradeDate"))
            .counterpartyId((long) p.get("counterpartyId"))
            .build();
    }
}
