package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.security.JwtTokenProvider;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("uat")
@WebMvcTest(TradeController.class)
class TradeControllerWebMvcTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TradeService tradeService;

        @MockBean
        private TradeMapper tradeMapper;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        private TradeRequest validRequest() {
                return new TradeRequest(
                                "ABC-20260730-0001",
                                1L,
                                1L,
                                "EQUITY",
                                "BUY",
                                new BigDecimal("10"),
                                new BigDecimal("100"),
                                LocalDate.of(2026, 7, 30));
        }

        @Test
        @WithMockUser(roles = "TRADER")
        void testCreateTrade_authenticated_returns201() throws Exception {

                Trade trade = Mockito.mock(Trade.class);
                Mockito.when(trade.getId()).thenReturn(42L);

                TradeResponse response = new TradeResponse(
                                42L,
                                "ABC-20260730-0001",
                                1L,
                                "AAPL",
                                1L,
                                "Counterparty",
                                "EQUITY",
                                "BUY",
                                new BigDecimal("10"),
                                new BigDecimal("100"),
                                LocalDate.of(2026, 7, 30),
                                "PENDING",
                                null,
                                null);

                Mockito.when(tradeService.create(any(TradeRequest.class), anyString()))
                                .thenReturn(trade);

                Mockito.when(tradeMapper.toResponse(any(Trade.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/v1/trades")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest())))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location",
                                                org.hamcrest.Matchers.containsString("42")))
                                .andExpect(jsonPath("$.id").value(42))
                                .andExpect(jsonPath("$.tradeRef")
                                                .value("ABC-20260730-0001"));
        }
}