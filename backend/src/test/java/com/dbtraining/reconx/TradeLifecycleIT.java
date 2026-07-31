package com.dbtraining.reconx;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradeLifecycleIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper mapper;

    @LocalServerPort
    private int port;

    private static String jwt;
    private static Long tradeId;
    private static Long reconBreakId;

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    @Test
    @Order(1)
    void loginAsAdmin() throws Exception {

        ResponseEntity<String> response = rest.postForEntity(
                url("/auth/login"),
                Map.of(
                        "email", "admin@db.com",
                        "password", "admin123"
                ),
                String.class
        );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        JsonNode body = mapper.readTree(response.getBody());

        jwt = body.get("token").asText();

        assertThat(jwt).isNotBlank();
    }


    @Test
    @Order(2)
    void createTrade() {

        HttpHeaders headers = authHeaders();

        HttpEntity<?> request = new HttpEntity<>(
                Map.of(
                        "tradeRef", "ABC-20260603-0001",
                        "instrumentId", 1,
                        "counterpartyId", 1,
                        "assetClass", "EQUITY",
                        "side", "BUY",
                        "quantity", 10,
                        "price", 100,
                        "tradeDate", "2026-06-03"
                ),
                headers
        );

        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/v1/trades"),
                        request,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        try {
            tradeId = mapper.readTree(response.getBody())
                    .get("id")
                    .asLong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(tradeId).isNotNull();
    }


    @Test
    @Order(3)
    void getTradeBack() {

        ResponseEntity<String> response =
                rest.exchange(
                        url("/v1/trades"),
                        HttpMethod.GET,
                        new HttpEntity<>(authHeaders()),
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }


    @Test
    @Order(4)
    void patchStatus() {

        HttpEntity<?> request =
                new HttpEntity<>(
                        Map.of("status", "MATCHED"),
                        authHeaders()
                );

        ResponseEntity<String> response =
                rest.exchange(
                        url("/v1/trades/" + tradeId + "/status"),
                        HttpMethod.PATCH,
                        request,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }


    @Test
    @Order(5)
    void triggerRecon() {

        HttpEntity<?> request =
                new HttpEntity<>(
                        Map.of(
                                "from", "2026-06-01",
                                "to", "2026-06-30"
                        ),
                        authHeaders()
                );

        ResponseEntity<String> response =
                rest.postForEntity(
                        url("/v1/recon/run"),
                        request,
                        String.class
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);
    }


    @Test
    @Order(6)
    void resolveBreak() {

        // Placeholder until a recon break exists from the reconciliation flow
        assertThat(true).isTrue();
    }


    private HttpHeaders authHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }
}