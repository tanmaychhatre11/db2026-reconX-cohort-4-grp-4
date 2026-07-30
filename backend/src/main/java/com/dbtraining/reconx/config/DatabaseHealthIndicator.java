package com.dbtraining.reconx.config;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Component("reconxDatabase")
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private static final String QUERY = "SELECT 1";

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        long start = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(QUERY)) {

            statement.setQueryTimeout(2);

            try (ResultSet resultSet = statement.executeQuery()) {
                long elapsedMs = System.currentTimeMillis() - start;

                builder.up()
                        .withDetail("query", QUERY)
                        .withDetail("elapsedMs", elapsedMs);
            }

        } catch (Exception e) {
            builder.down(e)
                    .withDetail("query", QUERY);
        }
    }
}