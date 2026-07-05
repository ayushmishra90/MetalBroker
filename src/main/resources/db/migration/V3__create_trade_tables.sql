-- =====================================================
-- V3__create_trade_tables.sql
-- =====================================================

CREATE TABLE IF NOT EXISTS trades (

    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    type VARCHAR(10) NOT NULL,

    metal VARCHAR(8) NOT NULL,

    quantity DECIMAL(20,8) NOT NULL,

    executed_price DECIMAL(20,6) NOT NULL,

    total_amount DECIMAL(20,6) NOT NULL,

    executed_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT fk_trade_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_trade_user
ON trades(user_id);

CREATE INDEX idx_trade_executed_at
ON trades(executed_at DESC);

CREATE INDEX idx_trade_user_time
ON trades(user_id, executed_at DESC);

CREATE INDEX idx_trade_metal
ON trades(metal);