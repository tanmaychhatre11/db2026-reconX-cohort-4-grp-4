# C4 Level 2 — Container Diagram: ReconX

```mermaid
C4Container
    title C4 Container — ReconX

    Person(user, "User", "Ops Analyst / Ops Manager / Compliance Auditor")
    System_Ext(omsKafka, "Internal OMS", "Upstream trade source")
    System_Ext(sso, "Corporate SSO", "OIDC IdP")

    System_Boundary(reconxBoundary, "ReconX") {
        Container(reactSpa, "Recon UI", "React 19 + Vite", "Single-page app. Live trade feed via SSE; trades + breaks tables; admin views.")
        Container(api, "recon-service API", "Java 25 + Spring Boot 3", "REST API. JWT auth, RBAC, validation, exposes /actuator/prometheus.")
        Container(reconEngine, "Reconciliation Engine", "Spring + CompletableFuture", "Async batch + streaming match logic. Writes recon_breaks.")
        ContainerDb(postgres, "PostgreSQL 16", "Liquibase-managed", "Partitioned trades, recon_breaks, audit_log, mat. views.")
        ContainerQueue(kafka, "Apache Kafka", "3 topics + DLQs", "trade-events, recon-results, system-alerts. DLQ per topic.")
        Container(prom, "Prometheus", "TSDB", "Scrapes the API every 15s.")
        Container(graf, "Grafana", "Dashboard", "Pre-provisioned dashboards.")
    }

    Rel(user, reactSpa, "Uses", "HTTPS")
    Rel(reactSpa, api, "REST + SSE", "HTTPS / JSON")
    Rel(reactSpa, sso, "Login (OIDC)", "HTTPS")
    Rel(api, postgres, "Reads + writes", "JDBC")
    Rel(api, kafka, "Publishes trade-events", "Kafka protocol")
    Rel(reconEngine, kafka, "Consumes trade-events", "Kafka protocol")
    Rel(reconEngine, postgres, "Writes recon_breaks", "JDBC")
    Rel(omsKafka, kafka, "Streams trades", "Kafka MirrorMaker")
    Rel(prom, api, "Scrapes /actuator/prometheus", "HTTPS")
    Rel(graf, prom, "Queries", "HTTPS / PromQL")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

## Container Summary

| Type | Container | Technology | Responsibility |
|------|-----------|------------|----------------|
| **Frontend** | Recon UI | React 19 + Vite | Single-page app; live trade feed via SSE; trades, breaks, and admin views |
| **API** | recon-service API | Java 25 + Spring Boot 3 | REST API; JWT auth, RBAC, validation; exposes `/actuator/prometheus` |
| **Service** | Reconciliation Engine | Spring + CompletableFuture | Async batch and streaming match logic; writes `recon_breaks` |
| **Database** | PostgreSQL 16 | Liquibase-managed | Partitioned trades, recon_breaks, audit_log, materialised views |
| **Queue** | Apache Kafka | 3 topics + DLQs | `trade-events`, `recon-results`, `system-alerts`; DLQ per topic |
| **Monitoring** | Prometheus | TSDB | Scrapes the API every 15 s |
| **Monitoring** | Grafana | Dashboard | Pre-provisioned dashboards for platform health and KPIs |

## Design Notes

1. **SSE for live data** — The React SPA receives real-time trade updates from the API via Server-Sent Events, avoiding polling overhead.
2. **Event-driven reconciliation** — Kafka decouples trade ingestion from the reconciliation engine, enabling async batch and streaming processing.
3. **Dead-letter queues** — Each Kafka topic has a paired DLQ to capture unprocessable messages without blocking the main consumer.
4. **Observability** — Prometheus scrapes the Spring Actuator endpoint; Grafana provides pre-provisioned dashboards sourced from the `monitoring/` directory.
5. **External identity** — The SPA delegates authentication to the Corporate SSO (OIDC), keeping credential management outside the platform boundary.
