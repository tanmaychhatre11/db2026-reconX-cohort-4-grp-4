# ADR-0001 — Partition the `trades` table by `trade_date`

- Status: Accepted
- Date: 2026-07-28
- Deciders: ReconX team

## Context

`trades` is the highest-volume table in ReconX. At about 50,000 trades per day
and 5-year retention, the platform will hold roughly 91 million rows at steady
state. Most reconciliation and analyst queries filter by day or month, so an
unpartitioned table would become harder to query, maintain, and archive.

## Decision

Partition the `trades` table by RANGE on `trade_date` using one partition per
calendar month. Keep a default partition so that unexpected out-of-range rows do
not fail inserts.

## Consequences

Positive:
- Date-based queries benefit from partition pruning.
- Old data is easier to archive or drop by partition.
- Per-partition indexes are smaller and cheaper to maintain.

Negative:
- Partition management must be maintained over time.
- Key and uniqueness design becomes more complex in PostgreSQL.
- ORM mappings can be harder than with a single table.