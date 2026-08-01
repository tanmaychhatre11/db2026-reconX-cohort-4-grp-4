package com.dbtraining.reconx.service;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * TICKET-ADV044 — Testcontainers PostgreSQL wiring.
 */
@Testcontainers
class ReconciliationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @Test
    void containerIsRunning() {
        org.junit.jupiter.api.Assertions.assertTrue(postgres.isRunning());
    }
}
