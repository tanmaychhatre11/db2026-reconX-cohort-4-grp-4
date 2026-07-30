package com.dbtraining.reconx.dto;

import java.time.Instant;

public class AuditRevisionResponse {

    private Long revisionId;
    private Instant revisionTimestamp;
    private String revisionType;
    private String changedBy;
    private TradeResponse snapshot;

    public AuditRevisionResponse() {
    }

    public AuditRevisionResponse(Long revisionId,
                                 Instant revisionTimestamp,
                                 String revisionType,
                                 String changedBy,
                                 TradeResponse snapshot) {
        this.revisionId = revisionId;
        this.revisionTimestamp = revisionTimestamp;
        this.revisionType = revisionType;
        this.changedBy = changedBy;
        this.snapshot = snapshot;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public Instant getRevisionTimestamp() {
        return revisionTimestamp;
    }

    public String getRevisionType() {
        return revisionType;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public TradeResponse getSnapshot() {
        return snapshot;
    }
}