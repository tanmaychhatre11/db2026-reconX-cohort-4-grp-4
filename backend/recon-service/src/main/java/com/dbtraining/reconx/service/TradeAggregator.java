package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeAggregator {
    private final AuditLogRepository auditRepository;
    private final ObjectMapper objectMapper;

    public TradeAggregator(AuditLogRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<JsonNode> rebuild(String tradeRef) {
        JsonNode state = null;

        for (AuditLogEntry entry : auditRepository.findByTradeRefOrderByEventTimestampAsc(tradeRef)) {
            switch (TradeEvent.EventType.valueOf(entry.getEventType())) {
                case TRADE_CREATED, TRADE_UPDATED -> state = parse(entry.getAfterState());
                case TRADE_CANCELLED -> state = null;
            }
        }

        return Optional.ofNullable(state);
    }

    private JsonNode parse(String snapshot) {
        if (snapshot == null || snapshot.isBlank()) return null;
        try {
            return objectMapper.readTree(snapshot);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Invalid audit snapshot", error);
        }
    }
}
