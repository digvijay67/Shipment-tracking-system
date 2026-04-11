-- ============================================================
-- GoComet Shipment Tracking System - SAFE INIT SCRIPT
-- Docker + PostgreSQL 16 compatible
-- NO DO blocks for CREATE DATABASE
-- ============================================================

-- ⚠️ IMPORTANT:
-- Docker runs this as superuser, so direct CREATE DATABASE is allowed
-- BUT NOT inside DO $$ blocks

-- ============================================================
-- 1. CREATE DATABASES
-- ============================================================

CREATE DATABASE gocomet_users;
CREATE DATABASE gocomet_shipments;
CREATE DATABASE gocomet_tracking;

-- ============================================================
-- 2. USERS DATABASE
-- ============================================================

\c gocomet_users;

CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(20) NOT NULL DEFAULT 'USER',
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);

-- Seed data
INSERT INTO users (email, password_hash, first_name, last_name, role)
VALUES
('admin@gocomet.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsxq4.3mi',
 'Admin', 'GoComet', 'ADMIN'),
('demo@gocomet.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj6hsxq4.3mi',
 'Demo', 'User', 'USER')
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- 3. SHIPMENTS DATABASE
-- ============================================================

\c gocomet_shipments;

CREATE TABLE IF NOT EXISTS shipments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number VARCHAR(50) UNIQUE NOT NULL,
    user_id         UUID NOT NULL,
    origin          VARCHAR(255) NOT NULL,
    destination     VARCHAR(255) NOT NULL,
    sender_name     VARCHAR(255) NOT NULL,
    receiver_name   VARCHAR(255) NOT NULL,
    receiver_phone  VARCHAR(50),
    weight_kg       DOUBLE PRECISION NOT NULL,
    distance_km     DOUBLE PRECISION,
    carrier         VARCHAR(100),
    status          VARCHAR(50) DEFAULT 'PENDING',
    expected_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    notes           VARCHAR(1000),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shipments_user_id ON shipments(user_id);
CREATE INDEX IF NOT EXISTS idx_shipments_tracking ON shipments(tracking_number);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status);

-- ============================================================
-- 4. TRACKING DATABASE
-- ============================================================

\c gocomet_tracking;

CREATE TABLE IF NOT EXISTS shipment_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id     UUID NOT NULL,
    tracking_number VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    location        VARCHAR(255),
    description     VARCHAR(500),
    event_type      VARCHAR(50),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_events_shipment_id ON shipment_events(shipment_id);
CREATE INDEX IF NOT EXISTS idx_events_tracking ON shipment_events(tracking_number);
CREATE INDEX IF NOT EXISTS idx_events_status ON shipment_events(status);