-- =====================================================
-- V4__create_market_tables.sql
-- =====================================================

-- =====================================================
-- Metals Master
-- =====================================================

CREATE TABLE IF NOT EXISTS metals (

    id BIGINT NOT NULL AUTO_INCREMENT,

    code VARCHAR(8) NOT NULL,

    name VARCHAR(64) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id),

    CONSTRAINT uq_metals_code
        UNIQUE (code)

);

CREATE INDEX idx_metals_code
ON metals(code);

-- =====================================================
-- Latest Snapshot
-- =====================================================

CREATE TABLE IF NOT EXISTS metal_latest (

    metal_id BIGINT NOT NULL,

    feed_ts_utc DATETIME(3) NOT NULL,

    captured_at_utc DATETIME(3) NOT NULL,

    ask DECIMAL(20,6),

    mid DECIMAL(20,6),

    bid DECIMAL(20,6),

    value DECIMAL(20,6),

    performance DECIMAL(20,6),

    source VARCHAR(64)
        NOT NULL DEFAULT 'goldbroker',

    PRIMARY KEY (metal_id),

    CONSTRAINT fk_latest_metal
        FOREIGN KEY (metal_id)
        REFERENCES metals(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_latest_feed
ON metal_latest(feed_ts_utc);

CREATE INDEX idx_latest_capture
ON metal_latest(captured_at_utc);

-- =====================================================
-- 5 Minute Spot Prices
-- =====================================================

CREATE TABLE IF NOT EXISTS spot_ticks_5m (

    id BIGINT NOT NULL AUTO_INCREMENT,

    metal_id BIGINT NOT NULL,

    ts_utc DATETIME(3) NOT NULL,

    ask DECIMAL(20,6),

    mid DECIMAL(20,6),

    bid DECIMAL(20,6),

    value DECIMAL(20,6),

    performance DECIMAL(20,6),

    source VARCHAR(64)
        NOT NULL DEFAULT 'goldbroker',

    created_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT uq_tick
        UNIQUE (metal_id, ts_utc),

    CONSTRAINT fk_tick_metal
        FOREIGN KEY (metal_id)
        REFERENCES metals(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_tick_metal_ts
ON spot_ticks_5m(metal_id, ts_utc DESC);

CREATE INDEX idx_tick_ts
ON spot_ticks_5m(ts_utc);

-- =====================================================
-- Daily History
-- =====================================================

CREATE TABLE IF NOT EXISTS metal_daily_history (

    id BIGINT NOT NULL AUTO_INCREMENT,

    metal_id BIGINT NOT NULL,

    day_utc DATE NOT NULL,

    open DECIMAL(20,6) NOT NULL,

    high DECIMAL(20,6) NOT NULL,

    low DECIMAL(20,6) NOT NULL,

    close DECIMAL(20,6) NOT NULL,

    ma50 DECIMAL(20,6),

    ma200 DECIMAL(20,6),

    source VARCHAR(64)
        NOT NULL DEFAULT 'goldbroker',

    created_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT uq_daily_history
        UNIQUE (metal_id, day_utc),

    CONSTRAINT fk_daily_history_metal
        FOREIGN KEY (metal_id)
        REFERENCES metals(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_daily_history
ON metal_daily_history(metal_id, day_utc DESC);

CREATE INDEX idx_daily_day
ON metal_daily_history(day_utc);