You are an enterprise software architect. Write an Architecture Decision Record
(ADR) in the Michael Nygard format (Title, Status, Context, Decision,
Consequences) for the following decision.

System: ReconX, a near-production trade reconciliation platform.
Stack: PostgreSQL 16, Spring Boot 3, Kafka, React.
Scale: ~50,000 trades/day, 5-year retention, 10 concurrent recon analysts.

Decision to record: Use a GIN index with `jsonb_path_ops` on `instruments.metadata` rather than a btree index.

Alternatives we considered:
- No index on metadata
- Btree index on extracted JSON fields
- GIN index with default operator class

Constraints / forces:
- Queries need containment search on JSONB
- Read performance matters for analyst lookups
- Index size and maintenance cost must stay reasonable
- PostgreSQL-native indexing support is preferred

Format: Markdown, Nygard 5-section template, no fluff. Keep under 300 words.
Include a "Status: Accepted | Date: 2026-07-28" line.