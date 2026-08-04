package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/dlq")
@PreAuthorize("hasRole('ADMIN')")
public class DlqAdminController {
    private final DlqMessageRepository repository;
    private final TradeEventProducer producer;
    private final ObjectMapper objectMapper;

    public DlqAdminController(DlqMessageRepository repository,
                              TradeEventProducer producer,
                              ObjectMapper objectMapper) {
        this.repository = repository;
        this.producer = producer;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/replay")
    public Map<String, Object> replay(
            @RequestParam UUID eventId,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        DlqMessage message = repository.findByEventId(eventId.toString())
                .orElseThrow(() -> new IllegalArgumentException("DLQ event not found: " + eventId));

        if (dryRun) {
            return Map.of(
                    "dryRun", true,
                    "eventId", message.getEventId(),
                    "wouldReplayTo", message.getOriginalTopic());
        }

        try {
            producer.publish(objectMapper.readValue(message.getPayload(), TradeEvent.class));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Unable to deserialize DLQ event", error);
        }

        repository.delete(message);
        return Map.of("replayed", true, "eventId", message.getEventId());
    }
}
