# ADR-0003 — Use GIN `jsonb_path_ops` for JSONB metadata lookups

- Status: Accepted
- Date: 2026-07-28
- Deciders: ReconX team

## Context

ReconX needs to query `instruments.metadata` for fields such as sector and other
asset-specific attributes. A plain btree index is not a good fit for general
JSONB containment queries, and no index would make lookups slower as the dataset
grows.

## Decision

Use a PostgreSQL GIN index with `jsonb_path_ops` on `instruments.metadata` to
support containment-style JSONB queries efficiently.

## Consequences

Positive:
- JSONB containment queries become much faster.
- The index matches PostgreSQL JSONB search patterns better than btree.
- Flexible metadata remains queryable at scale.

Negative:
- GIN indexes are more specialized and less familiar to new developers.
- Index maintenance has a cost.
- Query patterns need to stay aligned with the chosen operator class.