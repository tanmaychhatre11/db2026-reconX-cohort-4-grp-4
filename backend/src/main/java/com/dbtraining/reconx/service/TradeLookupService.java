package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;

import java.util.NoSuchElementException;

/**
 * ============================================================================
 * TICKET-ADV039 — Optional chaining for null-safe lookups
 *
 * WHAT: Walks a trade reference to its Counterparty using Optional
 *       combinators only.
 * HOW:  Trade already carries its Counterparty via a @ManyToOne association,
 *       so this is a single map + orElseThrow — no second repository call
 *       is needed once the Trade is found.
 * WHY:  No isPresent()/get() anywhere — this discipline is what Day 4's
 *       controllers reuse for 404 handling.
 * ============================================================================
 */
public class TradeLookupService {

    private final TradeRepository tradeRepo;

    public TradeLookupService(TradeRepository tradeRepo) {
        this.tradeRepo = tradeRepo;
    }

    public Counterparty counterpartyForTradeRef(String tradeRef) {
        return tradeRepo.findByTradeRef(tradeRef)
                .map(com.dbtraining.reconx.repository.entity.Trade::getCounterparty)
                .orElseThrow(() -> new NoSuchElementException(
                        "No counterparty resolvable for trade " + tradeRef));
    }
}