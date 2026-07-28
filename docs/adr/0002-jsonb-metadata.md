# ADR-0002 — Store flexible instrument attributes in JSONB metadata

- Status: Accepted
- Date: 2026-07-28
- Deciders: ReconX team

## Context

ReconX supports multiple asset classes, and each type of instrument can have
different attributes. Adding many nullable database columns would make the
schema bloated, while subtype tables would add more joins and complexity.

## Decision

Store flexible instrument-specific attributes in a PostgreSQL JSONB column named
`metadata` on the `instruments` table. Keep core fields such as symbol, name,
currency, and asset class as regular relational columns.

## Consequences

Positive:
- New metadata attributes can be added without frequent schema changes.
- PostgreSQL JSONB operators allow targeted lookups.
- The main schema stays cleaner for common fields.

Negative:
- Some validation moves from the database into the application.
- JSONB queries are more specialized than normal relational queries.
- Poor governance could lead to overuse of flexible metadata.