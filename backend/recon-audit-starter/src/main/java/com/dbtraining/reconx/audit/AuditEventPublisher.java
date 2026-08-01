package com.dbtraining.reconx.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);
    private final AuditProperties properties;

    public AuditEventPublisher(AuditProperties properties) {
        this.properties = properties;
    }

    public void publish(String eventType, String detail) {
        log.info("AUDIT [{}] {} -> {}", properties.getTopic(), eventType, detail);
    }
}