-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT
    t.trade_ref,
    t.instrument_id,
    t.trade_date,
    t.quantity,
    t.price,
    t.quantity * t.price AS notional,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
ORDER BY t.trade_date DESC, t.instrument_id, t.trade_ref;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup
-- (execution → confirmation → settlement → recon_break → resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (

    -- Anchor: every trade starts at stage 1 (EXECUTION)
    SELECT
        t.id            AS trade_id,
        t.trade_ref,
        1               AS stage,
        'EXECUTION'     AS stage_name,
        t.created_at    AS event_at,
        t.status        AS event_status
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- Recursive step: advance to the next lifecycle stage
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        ne.stage_name,
        ne.event_at,
        ne.event_status
    FROM trade_lifecycle tl
    JOIN LATERAL (

        -- stage 1 → 2: CONFIRMATION (trade acknowledged by counterparty)
        SELECT 'CONFIRMATION'               AS stage_name,
               t.modified_at               AS event_at,
               t.status                    AS event_status
        FROM   trades t
        WHERE  t.id = tl.trade_id
          AND  tl.stage = 1

        UNION ALL

        -- stage 2 → 3: SETTLEMENT (custodian settlement record exists)
        SELECT 'SETTLEMENT',
               s.settlement_date::timestamp,
               s.status
        FROM   settlements s
        WHERE  s.trade_id = tl.trade_id
          AND  tl.stage = 2

        UNION ALL

        -- stage 3 → 4: RECON_BREAK (discrepancy detected for this trade)
        SELECT 'RECON_BREAK',
               rb.detected_at,
               rb.status
        FROM   recon_breaks rb
        WHERE  rb.trade_id = tl.trade_id
          AND  tl.stage = 3

        UNION ALL

        -- stage 4 → 5: RESOLUTION (break has been closed/resolved)
        SELECT 'RESOLUTION',
               rb.resolved_at,
               rb.status
        FROM   recon_breaks rb
        WHERE  rb.trade_id = tl.trade_id
          AND  tl.stage = 4
          AND  rb.resolved_at IS NOT NULL

    ) AS ne ON TRUE
    WHERE tl.stage < 5   -- termination guard: cap at 5 stages, prevents runaway recursion
)
SELECT
    trade_id,
    trade_ref,
    stage,
    stage_name,
    event_at,
    event_status
FROM  trade_lifecycle
ORDER BY trade_id, stage;


-- ============================================================================
-- ADV008 — REFRESH the daily-summary materialised view (concurrent so it can
--         run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB queries against instruments.metadata (GIN jsonb_path_ops)
-- ============================================================================

-- Containment (@>) — uses GIN index: instruments in the Banking sector
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;

-- Containment (@>) — Technology sector instruments
SELECT id, symbol, metadata->>'sector' AS sector
FROM instruments
WHERE metadata @> '{"sector":"Technology"}';

-- Path extraction (->>) — issuer country for every instrument that has one
SELECT symbol, metadata->'issuer'->>'country' AS country
FROM instruments
WHERE metadata @> '{"issuer": {}}'::jsonb;

-- Key existence — instruments that carry a credit rating
SELECT symbol, metadata->'rating'->>'sp' AS sp_rating
FROM instruments
WHERE metadata @> '{"rating": {}}'::jsonb;

-- Array membership — instruments tagged as safe-haven or benchmark
SELECT symbol, metadata->>'sector' AS sector
FROM instruments
WHERE metadata @> '{"tags": ["safe-haven"]}'::jsonb
   OR metadata @> '{"tags": ["benchmark"]}'::jsonb;

-- EXPLAIN ANALYZE to confirm GIN index scan (not a Seq Scan)
EXPLAIN ANALYZE
SELECT * FROM instruments WHERE metadata @> '{"sector":"Technology"}';
