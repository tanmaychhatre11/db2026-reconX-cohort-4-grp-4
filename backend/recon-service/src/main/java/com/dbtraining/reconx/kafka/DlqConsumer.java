package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DlqConsumer {
    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);

    private final DlqMessageRepository repository;
    private final ObjectMapper objectMapper;

    public DlqConsumer(DlqMessageRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicsConfig.TRADE_EVENTS_DLQ, groupId = "dlq-monitor")
    public void onDlqMessage(ConsumerRecord<String, TradeEvent> record,
                             @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String reason) {
        TradeEvent event = record.value();
        if (event == null) {
            log.error("DLQ message has no deserialized TradeEvent topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset());
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(event);
            repository.save(new DlqMessage(
                    event.eventId().toString(),
                    event.tradeRef(),
                    record.topic().replace("-dlq", ""),
                    record.partition(),
                    record.offset(),
                    payload,
                    reason,
                    Instant.now()));
            log.error("DLQ message recorded eventId={} tradeRef={} reason={}",
                    event.eventId(), event.tradeRef(), reason);
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("Unable to serialize DLQ event", error);
        }
    }
}
