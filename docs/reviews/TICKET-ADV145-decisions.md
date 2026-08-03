# TICKET-ADV145 Kafka consumer config review

Prompt used: [TICKET-ADV145-prompt.md](./TICKET-ADV145-prompt.md)

| # | Area | Finding | Recommendation | Decision | Rationale |
|---|---|---|---|---|---|
| 1 | Backpressure | Consumer batches can grow too large for a slow reconciliation path and increase the chance of long poll gaps. | Set `spring.kafka.consumer.max-poll-records=100`. | Accept | A smaller batch keeps the poll loop responsive while we still process enough events for this training workload. |
| 2 | Error handling | `ExponentialBackOff` retries at fixed 1s/2s/4s intervals with no jitter, which can synchronize retries during an outage. | Add jitter to the retry backoff strategy. | Defer | The current retry path is correct for Day 9, but adding jitter needs a custom backoff implementation and a broader test pass. |
| 3 | Idempotence | Producer safety was implied by defaults rather than asserted in config, making duplicate-send protection less obvious to reviewers. | Set `spring.kafka.producer.properties.enable.idempotence=true` and `spring.kafka.producer.acks=all`. | Accept | These are low-cost safety settings and make the intent explicit in config instead of relying on transitive defaults. |
| 4 | Observability | Kafka consumers do not currently enforce per-listener application tags beyond the global Micrometer application tag. | Add extra Kafka client tags or listener naming if metrics cardinality becomes hard to separate. | Reject | `management.metrics.tags.application=${spring.application.name}` already prevents cross-service collisions, so we do not need more tag churn yet. |
| 5 | Security | The local Kafka bootstrap connection is PLAINTEXT and has no SASL/TLS settings. | Use `SASL_SSL` plus broker ACLs in production. | Reject | This repo intentionally runs local developer infrastructure, and production transport security is being handled as a later environment-specific hardening task. |

Accepted changes applied on this branch:

- `spring.kafka.consumer.max-poll-records=100`
- `spring.kafka.producer.acks=all`
- `spring.kafka.producer.properties.enable.idempotence=true`
