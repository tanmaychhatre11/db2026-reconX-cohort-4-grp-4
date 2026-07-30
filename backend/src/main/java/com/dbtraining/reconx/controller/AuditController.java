package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import com.dbtraining.reconx.dto.AuditRevisionResponse;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import java.time.Instant;
import org.hibernate.envers.DefaultRevisionEntity;

/**
 * TICKET-ADV071 — GET /api/v1/audit/trades/{tradeRef}
 * TICKET-ADV138 — GET /api/v1/audit/trades/{tradeRef}/events
 */
@RestController
@RequestMapping("/v1/audit")
@Tag(name = "audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditLogRepository auditRepo;
    private final TradeRepository tradeRepo;
    private final EntityManager entityManager;

    public AuditController(AuditLogRepository auditRepo,
                       TradeRepository tradeRepo,
                       EntityManager entityManager) {
        this.auditRepo = auditRepo;
        this.tradeRepo = tradeRepo;
        this.entityManager = entityManager;
    }

    @GetMapping("/trades/{tradeRef}")
    @Operation(summary = "Get audit history for a trade (by tradeRef)")
    public List<AuditRevisionResponse> history(@PathVariable String tradeRef) {

        Trade trade = tradeRepo.findByTradeRef(tradeRef)
            .orElseThrow(() -> new RuntimeException("Trade not found: " + tradeRef));

        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Number> revisions = reader.getRevisions(Trade.class, trade.getId());

        return revisions.stream()
            .map(revision -> {

                Trade snapshot = reader.find(
                        Trade.class,
                        trade.getId(),
                        revision
                );

                DefaultRevisionEntity revisionEntity =
        reader.findRevision(DefaultRevisionEntity.class, revision);

return new AuditRevisionResponse(
        revision.longValue(),
        Instant.ofEpochMilli(revisionEntity.getTimestamp()),
        "ADD",
        "system",
        snapshot
);
            })
            .toList();
    }

    @GetMapping("/trades/{tradeRef}/events")
    @Operation(summary = "Stream of all Kafka-sourced events for a trade")
    public List<AuditLogEntry> events(@PathVariable String tradeRef) {
        // TODO(TICKET-ADV138): once the audit-log Kafka consumer is in place,
        //   return auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef).
        return Collections.emptyList();
    }
}
