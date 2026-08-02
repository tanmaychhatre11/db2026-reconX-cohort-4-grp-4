package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.service.TradeStreamService;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.dbtraining.reconx.dto.TradeStatusRequest;

/**
 * ============================================================================
 * TICKET-ADV063-ADV067 — TradeController (full CRUD + filterable list)
 * TICKET-ADV080 — API versioning: every endpoint under /v1/
 *
 * Combined with the /api context-path from application.yml, full URLs are
 * /api/v1/trades, /api/v1/trades/{id} etc.
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/trades")
@Tag(name = "trades", description = "Trade CRUD and search")
@SecurityRequirement(name = "bearerAuth")
public class TradeController {

    private final TradeService service;
    private final TradeMapper mapper;
    private final TradeStreamService tradeStreamService;

    public TradeController(TradeService service, TradeMapper mapper, TradeStreamService tradeStreamService) {
        this.service = service;
        this.mapper = mapper;
        this.tradeStreamService = tradeStreamService;
    }

    @GetMapping
    public PagedResponse<TradeResponse> list(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long counterpartyId,
            @PageableDefault(size = 20, sort = "tradeDate",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<Trade> page = service.list(from, to, status,counterpartyId, pageable);
        return PagedResponse.from(page, mapper::toResponse);
    }

    @PostMapping
    @Operation(summary = "Create a trade")
    public ResponseEntity<TradeResponse> create(@Valid @RequestBody TradeRequest req,
                                                @AuthenticationPrincipal Object principal) {

        String actor = String.valueOf(principal);

        Trade saved = service.create(req, actor);

        return ResponseEntity
                .created(URI.create("/api/v1/trades/" + saved.getId()))
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of a trade")
    public TradeResponse update(@PathVariable Long id,
                                @Valid @RequestBody TradeRequest req,
                                @AuthenticationPrincipal Object principal) {

        String actor = String.valueOf(principal);

        Trade updated = service.update(id, req, actor);

        return mapper.toResponse(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update only the status field")
    public TradeResponse updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody TradeStatusRequest request,
        @AuthenticationPrincipal Object principal) {

        Trade updated = service.updateStatus(
            id,
            request.getStatus(),
            principal != null ? principal.toString() : "system"
        );

        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets deleted_at)")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @AuthenticationPrincipal Object principal) {

        service.softDelete(
            id,
            principal != null ? principal.toString() : "system"
        );

        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = {"http://127.0.0.1:5500","http://localhost:5500"})
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTrades() {
        return tradeStreamService.subscribe();
    }
}
