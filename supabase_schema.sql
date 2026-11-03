-- =============================================================================
-- ZapPay 2.0 Production Supabase Schema
-- Includes PostGIS, UUIDv7 / UUID generation, Double-Entry Ledger,
-- Forecourt AI Meter OCR, Fleet Controls, and 360 Vehicle Identity
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- 1. USERS & IDENTITY
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'PUMP_OPERATOR', 'PUMP_OWNER', 'FLEET_ADMIN', 'SUPER_ADMIN');
CREATE TYPE kyc_status AS ENUM ('PENDING', 'MIN_KYC', 'FULL_KYC', 'SUSPENDED');

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'customer',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    device_fingerprint_hash VARCHAR(255),
    totp_secret VARCHAR(64),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    blood_group VARCHAR(8) DEFAULT 'O+',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. B2B FLEET ORGANIZATIONS
CREATE TABLE IF NOT EXISTS fleet_organizations (
    id SERIAL PRIMARY KEY,
    owner_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(200) NOT NULL,
    gstin VARCHAR(15) UNIQUE,
    pan_number VARCHAR(10),
    subscription_plan VARCHAR(50) NOT NULL DEFAULT 'PRO_PER_VEHICLE',
    master_wallet_balance NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    credit_limit NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. VEHICLES & COMPLIANCE VAULT
CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    owner_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fleet_org_id INTEGER REFERENCES fleet_organizations(id) ON DELETE SET NULL,
    vrn_number VARCHAR(20) UNIQUE NOT NULL,
    vahan_verified BOOLEAN NOT NULL DEFAULT TRUE,
    make_model VARCHAR(120) DEFAULT 'Tata Nexon / Maruti Swift',
    vehicle_class VARCHAR(50) NOT NULL DEFAULT 'FOUR_WHEELER',
    fuel_type VARCHAR(30) NOT NULL DEFAULT 'PETROL',
    tank_capacity_litres NUMERIC(6,2) DEFAULT 45.0,
    insurance_expiry_date VARCHAR(30) DEFAULT '2027-04-15',
    puc_expiry_date VARCHAR(30) DEFAULT '2026-12-10',
    last_odometer_km INTEGER DEFAULT 12450,
    avg_mileage_kmpl NUMERIC(5,2) DEFAULT 16.8,
    parking_contact_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    blood_group VARCHAR(10) DEFAULT 'O+',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. TAMPER-PROOF QR TAGS & FLEET RULES
CREATE TABLE IF NOT EXISTS qr_tags (
    id SERIAL PRIMARY KEY,
    qr_public_token VARCHAR(64) UNIQUE NOT NULL,
    hmac_secret_key VARCHAR(128) NOT NULL DEFAULT 'zp_hmac_secret_key_prod_v2',
    vehicle_id INTEGER UNIQUE REFERENCES vehicles(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    batch_code VARCHAR(50) NOT NULL DEFAULT 'BATCH_CG_01',
    max_per_txn NUMERIC(10,2) NOT NULL DEFAULT 2500.00,
    daily_limit NUMERIC(10,2) NOT NULL DEFAULT 5000.00,
    monthly_limit NUMERIC(10,2) NOT NULL DEFAULT 25000.00,
    require_geofence_match BOOLEAN NOT NULL DEFAULT FALSE,
    allowed_fuel_type VARCHAR(30),
    activated_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. PETROL PUMP STATIONS
CREATE TABLE IF NOT EXISTS petrol_pumps (
    id SERIAL PRIMARY KEY,
    pump_name VARCHAR(255) NOT NULL,
    owner_name VARCHAR(255) NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Chhattisgarh',
    pincode VARCHAR(10) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    phone_number VARCHAR(15) NOT NULL,
    email VARCHAR(255),
    daily_fuel_capacity NUMERIC(10,2),
    fuel_types VARCHAR(255) DEFAULT 'Petrol, Diesel, CNG',
    commission_rate NUMERIC(5,4) DEFAULT 0.02,
    mdr_fee_bps INTEGER DEFAULT 0,
    verified_petrol_density NUMERIC(6,2) DEFAULT 742.0,
    verified_diesel_density NUMERIC(6,2) DEFAULT 835.0,
    trust_score NUMERIC(3,2) DEFAULT 5.0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. PAYMENT MANDATES (UPI Autopay, UPI Circle, RuPay Credit)
CREATE TABLE IF NOT EXISTS payment_mandates (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id INTEGER REFERENCES vehicles(id) ON DELETE SET NULL,
    rail_type VARCHAR(50) NOT NULL DEFAULT 'UPI_AUTOPAY',
    ummn_reference VARCHAR(100) UNIQUE,
    payer_vpa VARCHAR(120) DEFAULT 'user@okaxis',
    max_limit NUMERIC(10,2) NOT NULL DEFAULT 2500.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    delegated_to_name VARCHAR(100),
    delegated_to_phone VARCHAR(15),
    valid_until VARCHAR(30) DEFAULT '2030-12-31',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. TRANSACTIONS & AI METER OCR AUDIT
CREATE TABLE IF NOT EXISTS transactions (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    user_id INTEGER NOT NULL REFERENCES users(id),
    wallet_id INTEGER REFERENCES wallets(id),
    qr_code_id INTEGER,
    pump_id INTEGER REFERENCES petrol_pumps(id),
    operator_id INTEGER,
    transaction_type VARCHAR(50) NOT NULL,
    fuel_dispensed VARCHAR(50),
    fuel_quantity NUMERIC(10,2),
    fuel_rate NUMERIC(10,2),
    amount NUMERIC(10,2) NOT NULL,
    platform_fee NUMERIC(10,2) DEFAULT 0.00,
    net_dealer_amount NUMERIC(10,2) DEFAULT 0.00,
    ocr_verified BOOLEAN DEFAULT FALSE,
    ocr_confidence NUMERIC(4,3) DEFAULT 1.0,
    soundbox_payload VARCHAR(255),
    status VARCHAR(30) DEFAULT 'COMPLETED',
    payment_gateway_response TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. DISPENSER METER AUDITS
CREATE TABLE IF NOT EXISTS dispenser_meter_audits (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(100),
    station_id INTEGER REFERENCES petrol_pumps(id),
    attendant_id INTEGER REFERENCES users(id),
    volume_litres NUMERIC(8,2) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    density NUMERIC(6,2) DEFAULT 742.0,
    fuel_rate NUMERIC(8,2) NOT NULL,
    math_verified BOOLEAN DEFAULT TRUE,
    confidence_score NUMERIC(4,3) DEFAULT 0.99,
    manual_override BOOLEAN DEFAULT FALSE,
    meter_image_path VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. DOUBLE-ENTRY FINANCIAL LEDGER
CREATE TABLE IF NOT EXISTS ledger_entries (
    id SERIAL PRIMARY KEY,
    transaction_id VARCHAR(100),
    account_type VARCHAR(50) NOT NULL,
    account_owner_id INTEGER NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    running_balance NUMERIC(12,2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 10. STATION PURITY & MILEAGE LOGS
CREATE TABLE IF NOT EXISTS station_purity_reviews (
    id SERIAL PRIMARY KEY,
    station_id INTEGER NOT NULL REFERENCES petrol_pumps(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    vehicle_id INTEGER REFERENCES vehicles(id),
    odometer_km INTEGER NOT NULL,
    litres_filled NUMERIC(8,2) NOT NULL,
    computed_kmpl NUMERIC(5,2) DEFAULT 15.0,
    purity_score NUMERIC(3,2) DEFAULT 4.8,
    feedback TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

