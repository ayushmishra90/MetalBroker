-- Active: 1783184064881@@127.0.0.1@3306@metalbroker_dev
-- =====================================================
-- V1__create_users.sql
-- =====================================================

CREATE TABLE IF NOT EXISTS users (

    id BIGINT NOT NULL AUTO_INCREMENT,

    email VARCHAR(255) NOT NULL,

    password VARCHAR(255) NOT NULL,

    role VARCHAR(20) NOT NULL DEFAULT 'USER',

    current_refresh_token VARCHAR(1024) NULL,

    created_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    updated_at DATETIME(3)
        NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),

    CONSTRAINT uq_users_email
        UNIQUE (email)

);

CREATE INDEX idx_users_email
ON users(email);