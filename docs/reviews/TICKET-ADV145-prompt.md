Review the following Spring Kafka consumer and producer configuration for production
readiness. Flag any missing or risky settings in these areas:
  (1) backpressure & poll tuning,
  (2) error handling, retry & DLQ,
  (3) idempotence and exactly-once semantics,
  (4) observability — metrics, logging, traces,
  (5) security — TLS, SASL, ACLs.

For each finding, give the concrete config key, the recommended value, and a
one-line justification. Do NOT rewrite the whole file — just list findings.

Application context: trade reconciliation service, ~500 events/sec, strict
audit requirements.

=== application.yml (Kafka section) ===
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP:localhost:9092}
    producer:
      acks: all
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      properties:
        enable.idempotence: true
        spring.json.add.type.headers: false
    consumer:
      group-id: reconx-service
      auto-offset-reset: earliest
      max-poll-records: 100
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: com.dbtraining.reconx.dto
        spring.json.use.type.headers: false
        spring.json.value.default.type: com.dbtraining.reconx.dto.TradeEvent

=== KafkaErrorHandlerConfig.java ===
package com.dbtraining.reconx.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (ConsumerRecord<?, ?> record, Exception exception) ->
                        new TopicPartition(record.topic() + "-dlq", record.partition()));

        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxAttempts(3);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}
