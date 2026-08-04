package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV129 — TradeEventProducer
 *
 * WHAT:    Publishes TradeEvent messages to the `trade-events` Kafka topic.
 * HOW:     {@literal KafkaTemplate<String, TradeEvent>}. Key = tradeRef so that all
 *          events for the same trade hash to the same partition and
 *          preserve ordering.
 * WHY:     Out-of-order events for the same trade would make event sourcing
 *          impossible (you'd "apply" CREATE after UPDATE).
 * OBSERVE: Kafdrop -> `trade-events` shows one message per published event,
 *          partitioned by tradeRef.
 * ============================================================================
 */
@Component
public class TradeEventProducer {

    private static final Logger log = LoggerFactory.getLogger(TradeEventProducer.class);
    private final KafkaTemplate<String, TradeEvent> template;

    public TradeEventProducer(KafkaTemplate<String, TradeEvent> template) {
        this.template = template;
    }

    public void publish(TradeEvent event) {
        log.debug("Publishing TradeEvent eventId={} ref={} type={}",
                event.eventId(), event.tradeRef(), event.eventType());

        template.send(KafkaTopicsConfig.TRADE_EVENTS, event.tradeRef(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish TradeEvent eventId={} ref={}",
                                event.eventId(), event.tradeRef(), error);
                        return;
                    }

                    var metadata = result.getRecordMetadata();
                    log.debug("Published TradeEvent eventId={} partition={} offset={}",
                            event.eventId(), metadata.partition(), metadata.offset());
                });
    }
}
