You are an enterprise software architect. Write an Architecture Decision Record
(ADR) in the Michael Nygard format (Title, Status, Context, Decision,
Consequences) for the following decision.

System: ReconX, a near-production trade reconciliation platform.
Stack: PostgreSQL 16, Spring Boot 3, Kafka, React.
Scale: ~50,000 trades/day, 5-year retention, 10 concurrent recon analysts.

Decision to record: Partition the `trades` table by RANGE on `trade_date` using monthly partitions.

Alternatives we considered:
- Single unpartitioned `trades` table
- Partition by counterparty
- Partition by asset class

Constraints / forces:
- Most queries are date-bounded
- Retention period is 5 years
- High insert volume requires manageable index size
- Archival should be operationally simple

Format: Markdown, Nygard 5-section template, no fluff. Keep under 300 words.
Include a "Status: Accepted | Date: 2026-07-28" line.