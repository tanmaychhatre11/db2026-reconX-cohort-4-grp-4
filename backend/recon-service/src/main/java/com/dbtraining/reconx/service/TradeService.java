package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.dto.TradeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;
    private final TradeStreamService tradeStreamService;
    private final ObjectMapper objectMapper;

    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics,
                        TradeStreamService tradeStreamService,
                        ObjectMapper objectMapper) {
        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
        this.tradeStreamService = tradeStreamService;
        this.objectMapper = objectMapper;
    }

    public Trade create(TradeRequest req, String actor) {

        if (tradeRepo.findByTradeRef(req.tradeRef()).isPresent()) {
            throw new DuplicateTradeRefException(req.tradeRef());
        }

        var instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() ->
                        new TradeNotFoundException("Instrument " + req.instrumentId()));

        var counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() ->
                        new TradeNotFoundException("Counterparty " + req.counterpartyId()));

        Trade trade = new Trade();

        trade.setTradeRef(req.tradeRef());
        trade.setInstrument(instrument);
        trade.setCounterparty(counterparty);
        trade.setAssetClass(req.assetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());
        trade.setStatus("PENDING");

        Trade saved = tradeRepo.save(trade);

        try {
            Map<String, Object> snapshot = Map.of(
                    "tradeRef", saved.getTradeRef(),
                    "instrumentId", saved.getInstrument().getId(),
                    "counterpartyId", saved.getCounterparty().getId(),
                    "assetClass", saved.getAssetClass(),
                    "side", saved.getSide(),
                    "quantity", saved.getQuantity(),
                    "price", saved.getPrice(),
                    "tradeDate", saved.getTradeDate(),
                    "status", saved.getStatus()
            );

            String after = objectMapper.writeValueAsString(snapshot);

            events.publish(new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_CREATED,
                    Instant.now(),
                    actor,
                    null,
                    after
            ));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to publish TradeEvent", e);
        }

        metrics.incrementTradeCreated();
        metrics.recordTradeValue(saved.getQuantity().multiply(saved.getPrice()).doubleValue());
        tradeStreamService.broadcast(saved);
        return saved;
    }

    public Trade update(Long id, TradeRequest req, String actor) {
        Trade trade = tradeRepo.findById(id)
                .orElseThrow(() -> new TradeNotFoundException(String.valueOf(id)));

        var instrument = instRepo.findById(req.instrumentId())
                .orElseThrow(() ->
                        new TradeNotFoundException("Instrument " + req.instrumentId()));

        var counterparty = cpRepo.findById(req.counterpartyId())
                .orElseThrow(() ->
                        new TradeNotFoundException("Counterparty " + req.counterpartyId()));

        trade.setTradeRef(req.tradeRef());
        trade.setInstrument(instrument);
        trade.setCounterparty(counterparty);
        trade.setAssetClass(req.assetClass());
        trade.setSide(req.side());
        trade.setQuantity(req.quantity());
        trade.setPrice(req.price());
        trade.setTradeDate(req.tradeDate());

        Trade updated = tradeRepo.save(trade);
        tradeStreamService.broadcast(updated);
        return updated;
    }

    public Trade updateStatus(Long id, String status, String actor) {

        Trade trade = tradeRepo.findById(id)
            .orElseThrow(() -> new TradeNotFoundException(String.valueOf(id)));

        trade.setStatus(status);

        Trade updated = tradeRepo.save(trade);
        tradeStreamService.broadcast(updated);
        return updated;
    }  

    public void softDelete(Long id, String actor) {

        Trade trade = tradeRepo.findById(id)
            .orElseThrow(() -> new TradeNotFoundException(String.valueOf(id)));

        trade.softDelete();

        Trade deleted = tradeRepo.save(trade);
        tradeStreamService.broadcast(deleted);
    }

    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from, LocalDate to, String status, Long counterpartyId, Pageable pageable) {
        Specification<Trade> spec = Specification
                .where(tradeDateBetween(from, to))
                .and(hasStatus(status))
                .and(hasCounterparty(counterpartyId));

        return tradeRepo.findAll(spec, pageable);
    }
}
