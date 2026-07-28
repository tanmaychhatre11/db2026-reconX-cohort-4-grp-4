You are an enterprise software architect. Write an Architecture Decision Record
(ADR) in the Michael Nygard format (Title, Status, Context, Decision,
Consequences) for the following decision.

System: ReconX, a near-production trade reconciliation platform.
Stack: PostgreSQL 16, Spring Boot 3, Kafka, React.
Scale: ~50,000 trades/day, 5-year retention, 10 concurrent recon analysts.

Decision to record: Store flexible instrument attributes in `instruments.metadata` as JSONB.

Alternatives we considered:
- Add separate nullable columns for every possible attribute
- Create subtype tables per asset class
- Store metadata as plain text JSON

Constraints / forces:
- Different asset classes need different metadata attributes
- Schema evolution should stay manageable
- PostgreSQL querying support is required
- The application can own validation rules

Format: Markdown, Nygard 5-section template, no fluff. Keep under 300 words.
Include a "Status: Accepted | Date: 2026-07-28" line.