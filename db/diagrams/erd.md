# TICKET-ADV006 - ER Model (8 Entities)

```mermaid
erDiagram
    COUNTERPARTIES ||--o{ TRADES : "executes"
    INSTRUMENTS ||--o{ TRADES : "covers"
    TRADES ||--o{ SETTLEMENTS : "settles via"
    TRADES ||--o{ RECON_BREAKS : "may produce"
    RECON_JOBS ||--o{ RECON_BREAKS : "detected by"
    USERS ||--o{ RECON_JOBS : "triggers"
    USERS ||--o{ AUDIT_LOG : "actor"

    COUNTERPARTIES {
        bigint id PK
        varchar name
        varchar lei_code UK
        varchar region
        timestamp created_at
    }

    INSTRUMENTS {
        bigint id PK
        varchar symbol UK
        varchar name
        varchar asset_class
        varchar currency
        varchar isin UK
        jsonb metadata "TICKET-ADV009"
    }

    TRADES {
        bigint id PK
        varchar trade_ref UK
        bigint instrument_id FK
        bigint counterparty_id FK
        varchar asset_class
        varchar side
        numeric quantity
        numeric price
        date trade_date "PARTITION KEY (TICKET-ADV007)"
        varchar status
        timestamp deleted_at
        timestamp created_at
        timestamp modified_at
    }

    SETTLEMENTS {
        bigint id PK
        bigint trade_id FK
        date settlement_date
        numeric amount
        varchar status
    }

    RECON_BREAKS {
        bigint id PK
        bigint trade_id FK
        bigint job_id FK
        varchar discrepancy_type
        varchar status
        timestamp detected_at
        timestamp resolved_at
        varchar resolution_note
    }

    RECON_JOBS {
        bigint id PK
        varchar job_id UK
        bigint triggered_by FK
        date from_date
        date to_date
        varchar status
        timestamp started_at
        timestamp finished_at
        int trades_processed
        int breaks_detected
    }

    AUDIT_LOG {
        bigint id PK
        varchar event_id UK
        varchar trade_ref
        varchar event_type
        timestamp event_timestamp
        varchar changed_by "NO FK - audit outlives users"
        text before_state
        text after_state
    }

    USERS {
        bigint id PK
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
        timestamp created_at
    }
```

## Entity Summary

| Entity | Purpose | Partition Key |
|--------|---------|---------------|
| **counterparties** | Trading counterparties with LEI codes | - |
| **instruments** | Financial instruments (equities, bonds, etc.) | - |
| **trades** | Core trade records with lifecycle status | `trade_date` |
| **settlements** | Settlement instructions per trade | - |
| **recon_breaks** | Detected discrepancies during reconciliation | - |
| **recon_jobs** | Async reconciliation job tracking | - |
| **audit_log** | Append-only event log for trade changes | - |
| **users** | Application users for JWT-based RBAC | - |

## Foreign Key Relationships

| Child Table | FK Column | Parent Table | Constraint |
|-------------|-----------|--------------|------------|
| trades | instrument_id | instruments | fk_trades_instrument |
| trades | counterparty_id | counterparties | fk_trades_counterparty |
| settlements | trade_id | trades | fk_settlements_trade |
| recon_breaks | trade_id | trades | fk_recon_breaks_trade |
| recon_breaks | recon_job_id | recon_jobs | fk_recon_breaks_job |
| recon_jobs | triggered_by | users | fk_recon_jobs_user |


## Design Notes

- `audit_log.changed_by` deliberately has **NO database FK** - audit records must outlive the users and trades they reference
- `trades.trade_date` is the partition key for range partitioning (TICKET-ADV007)
- `instruments.metadata` stores flexible JSONB attributes (TICKET-ADV009)
