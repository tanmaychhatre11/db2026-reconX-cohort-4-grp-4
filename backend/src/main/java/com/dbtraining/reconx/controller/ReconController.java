package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.exception.ReconBreakNotFoundException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.ReconJobRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import com.dbtraining.reconx.repository.entity.ReconJob;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dbtraining.reconx.dto.ResolveRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * TICKET-ADV068 — POST /api/v1/recon/run — returns 202 + jobId
 * TICKET-ADV069 — GET  /api/v1/recon/jobs/{jobId}/results
 * TICKET-ADV070 — PUT  /api/v1/recon/results/{id}/resolve
 */
@RestController
@RequestMapping("/v1/recon")
@Tag(name = "recon", description = "Reconciliation operations")
@SecurityRequirement(name = "bearerAuth")
public class ReconController {

    private final ReconBreakRepository reconBreakRepository;
    private final ReconJobRepository jobs;

    public ReconController(ReconBreakRepository breaks,
                        ReconJobRepository jobs) {
        this.reconBreakRepository = breaks;
        this.jobs = jobs;
    }

    @PostMapping("/run")
    @Operation(summary = "Trigger a reconciliation job (async)")
    public ResponseEntity<Map<String, String>> runRecon(
            @Valid @RequestBody ReconRunRequest req) {

        String jobId = UUID.randomUUID().toString();

        ReconJob job = new ReconJob();
        job.setJobId(jobId);
        job.setFromDate(req.from());
        job.setToDate(req.to());
        job.setStatus("QUEUED");

        jobs.save(job);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "jobId", jobId,
                        "status", "QUEUED"
                ));
    }

    @GetMapping("/jobs/{jobId}/results")
    @Operation(summary = "Get results for a recon job")
    public List<ReconBreak> results(@PathVariable String jobId) {
        return reconBreakRepository.findByJobId(jobId);
    }

    @PutMapping("/results/{id}/resolve")
    @Operation(summary = "Mark a recon break as RESOLVED with a note")
    @Transactional
    public ResponseEntity<ReconBreak> resolve(
        @PathVariable Long id,
        @Valid @RequestBody ResolveRequest request) {

        ReconBreak rb = reconBreakRepository.findById(id)
            .orElseThrow(() -> new ReconBreakNotFoundException(id));

        rb.resolve(request.getNote());

        reconBreakRepository.save(rb);

        return ResponseEntity.ok(rb);
    }
}
