package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TICKET-ADV039 — TradeLookupService: Optional-chained lookup, no isPresent()/get().
 */
@ExtendWith(MockitoExtension.class)
class TradeLookupServiceTest {

    @Mock
    private TradeRepository tradeRepo;

    @Test
    void resolvableTradeRef_returnsCounterparty() {
        // given
        String ref = "EQU-20260603-0001";
        Counterparty counterparty = mock(Counterparty.class);
        Trade trade = mock(Trade.class);
        when(trade.getCounterparty()).thenReturn(counterparty);
        when(tradeRepo.findByTradeRef(ref)).thenReturn(Optional.of(trade));

        TradeLookupService service = new TradeLookupService(tradeRepo);

        // when
        Counterparty result = service.counterpartyForTradeRef(ref);

        // then
        assertThat(result).isEqualTo(counterparty);
    }

    @Test
    void missingTradeRef_throwsNoSuchElementExceptionWithRefInMessage() {
        // given
        String ref = "EQU-MISSING-0001";
        when(tradeRepo.findByTradeRef(ref)).thenReturn(Optional.empty());

        TradeLookupService service = new TradeLookupService(tradeRepo);

        // when / then
        assertThatThrownBy(() -> service.counterpartyForTradeRef(ref))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(ref);
    }
}