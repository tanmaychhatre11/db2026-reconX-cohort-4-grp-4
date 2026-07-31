package com.dbtraining.reconx.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TICKET-ADV080 — Deprecated v0 trade endpoint.
 */
@RestController
@RequestMapping("/v0/trades")
@Hidden
public class TradeV0Controller {

    @Deprecated(since = "v1.4.0", forRemoval = true)
    @GetMapping
    public ResponseEntity<Void> deprecatedTrades(HttpServletResponse response) {

        response.setHeader("Deprecation", "true");
        response.setHeader("Sunset", "Sat, 01 Jul 2028 00:00:00 GMT");
        response.setHeader(
                "Link",
                "</api/v1/trades>; rel=\"successor-version\""
        );

        return ResponseEntity.status(HttpStatus.GONE).build();
    }
}