package com.dbtraining.reconx.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("uat")
class LiquibaseMigrationsIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("reconx")
                    .withUsername("test")
                    .withPassword("test");


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    void liquibaseRunsOnFreshDatabaseAndSeedDataExists() {

        Integer changesets = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class
        );

        Integer activeTrades = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trades WHERE deleted_at IS NULL",
                Integer.class
        );


        assertThat(changesets)
                .isGreaterThanOrEqualTo(13);

        assertThat(activeTrades)
                .isGreaterThanOrEqualTo(10);
    }
}