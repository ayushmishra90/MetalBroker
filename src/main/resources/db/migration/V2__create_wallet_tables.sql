-- =====================================================
-- V2__create_wallet_tables.sql
-- =====================================================

CREATE TABLE IF NOT EXISTS wallets (

    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    inr_balance DECIMAL(20,2)
        NOT NULL DEFAULT 0.00,

    created_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT uq_wallet_user
        UNIQUE (user_id),

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_wallet_user
ON wallets(user_id);

-- =====================================================
-- Wallet Metal Balances
-- =====================================================

CREATE TABLE IF NOT EXISTS wallet_balances (

    id BIGINT NOT NULL AUTO_INCREMENT,

    wallet_id BIGINT NOT NULL,

    metal VARCHAR(8) NOT NULL,

    quantity DECIMAL(20,8)
        NOT NULL DEFAULT 0.00000000,

    created_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT uq_wallet_balance
        UNIQUE (wallet_id, metal),

    CONSTRAINT fk_wallet_balance_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

);

CREATE INDEX idx_wallet_balance_wallet
ON wallet_balances(wallet_id);

CREATE INDEX idx_wallet_balance_metal
ON wallet_balances(metal);