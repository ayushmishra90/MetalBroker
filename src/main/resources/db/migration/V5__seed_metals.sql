-- =====================================================
-- V5__seed_metals.sql
-- Seed master metal records
-- =====================================================

INSERT INTO metals (code, name, is_active)
VALUES
    ('XAU', 'Gold', TRUE),
    ('XAG', 'Silver', TRUE),
    ('XPD', 'Palladium', TRUE),
    ('XPT', 'Platinum', TRUE)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    is_active = VALUES(is_active);