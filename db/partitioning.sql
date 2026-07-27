-- ============================================================================
-- TICKET-ADV007 — Convert trades to monthly range-partitioned table (Postgres)
--
-- WHAT:    Create trades as a RANGE-partitioned parent table on trade_date,
--          with child partitions for April-July 2026 plus a DEFAULT catch-all.
-- WHY:     Partitioning by trade_date means EOD reconciliation queries and
--          matview refreshes (ADV008) scan one month, not the whole table.
--          The default partition prevents Day-9 Kafka inserts from failing
--          if they arrive outside the defined window.
-- OBSERVE: \d+ trades shows "Partition key: RANGE (trade_date)" and lists
--          each child partition with its boundary.
--
-- WARNING: Destructive. Run in a maintenance window — copies the entire
--          trades table into a new partitioned trades, then renames.
-- ============================================================================

-- 1. Drop foreign key from settlements (required before renaming trades)
ALTER TABLE settlements DROP CONSTRAINT IF EXISTS fk_settlements_trade;

-- 2. Rename existing trades table to legacy
ALTER TABLE trades RENAME TO trades_legacy;

-- 3. Create partitioned parent table (same columns, composite PK required)
CREATE TABLE trades (
    id              BIGSERIAL,
    trade_ref       VARCHAR(30)   NOT NULL,
    instrument_id   BIGINT        NOT NULL REFERENCES instruments(id),
    counterparty_id BIGINT        NOT NULL REFERENCES counterparties(id),
    asset_class     VARCHAR(20)   NOT NULL,
    side            VARCHAR(4)    NOT NULL,
    quantity        NUMERIC(18,4) NOT NULL,
    price           NUMERIC(18,4) NOT NULL,
    trade_date      DATE          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    modified_at     TIMESTAMPTZ,
    PRIMARY KEY (id, trade_date)
) PARTITION BY RANGE (trade_date);

-- 4. Per-month partitions (April - July 2026 rolling window)
CREATE TABLE trades_y2026m04 PARTITION OF trades
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE trades_y2026m05 PARTITION OF trades
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE trades_y2026m06 PARTITION OF trades
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE trades_y2026m07 PARTITION OF trades
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

-- 5. Default partition (catch-all for dates outside the defined ranges)
--    Prevents insert failures for trades outside the April-July window
CREATE TABLE trades_default PARTITION OF trades DEFAULT;

-- 6. Note: Unique index on just id is NOT possible with partitioned tables.
--    PostgreSQL requires unique constraints to include all partition columns.
--    The PK (id, trade_date) already ensures uniqueness within partitions.

-- 7. Recreate indexes on partitioned table
CREATE INDEX idx_trades_trade_date ON trades (trade_date);
CREATE INDEX idx_trades_status ON trades (status);
CREATE INDEX idx_trades_trade_ref ON trades (trade_ref);

-- 8. Migrate data from legacy table
INSERT INTO trades (id, trade_ref, instrument_id, counterparty_id, asset_class,
                   side, quantity, price, trade_date, status, deleted_at,
                   created_at, modified_at)
SELECT id, trade_ref, instrument_id, counterparty_id, asset_class,
       side, quantity, price, trade_date, status, deleted_at,
       created_at, modified_at
FROM trades_legacy;

-- 9. Note: FK from settlements to trades(id) is NOT possible with partitioned tables.
--    PostgreSQL requires unique constraints on referenced columns, but unique
--    constraints on partitioned tables must include all partition columns.
--    Data integrity is maintained at the application level.

-- 10. Drop legacy table after verification (uncomment when ready)
-- DROP TABLE trades_legacy;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify partition structure:
-- \d+ trades

-- Verify partition pruning works (should only scan trades_2026_06):
-- EXPLAIN ANALYZE SELECT * FROM trades WHERE trade_date = '2026-06-15';

-- List all partitions:
-- SELECT
--     parent.relname AS parent_table,
--     child.relname AS partition_name,
--     pg_get_expr(child.relpartbound, child.oid) AS partition_expression
-- FROM pg_inherits
-- JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
-- JOIN pg_class child ON pg_inherits.inhrelid = child.oid
-- WHERE parent.relname = 'trades'
-- ORDER BY child.relname;
