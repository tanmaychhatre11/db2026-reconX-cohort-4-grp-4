package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "trade_ref", nullable = false, length = 30)
    private String tradeRef;

    @Column(name = "original_topic", nullable = false, length = 100)
    private String originalTopic;

    @Column(nullable = false)
    private Integer partition;

    @Column(name = "record_offset", nullable = false)
    private Long offset;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    protected DlqMessage() {}

    public DlqMessage(String eventId, String tradeRef, String originalTopic,
                      Integer partition, Long offset, String payload,
                      String reason, Instant firstSeen) {
        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.originalTopic = originalTopic;
        this.partition = partition;
        this.offset = offset;
        this.payload = payload;
        this.reason = reason;
        this.firstSeen = firstSeen;
    }

    public Long getId() { return id; }
    public String getEventId() { return eventId; }
    public String getTradeRef() { return tradeRef; }
    public String getOriginalTopic() { return originalTopic; }
    public Integer getPartition() { return partition; }
    public Long getOffset() { return offset; }
    public String getPayload() { return payload; }
    public String getReason() { return reason; }
    public Instant getFirstSeen() { return firstSeen; }
}
