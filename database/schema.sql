-- ============================================================================
-- EvidLock: Digital Evidence Chain-of-Custody Management System Schema
-- ============================================================================

DROP TRIGGER IF EXISTS trg_custody_events_append_only ON custody_events;
DROP TRIGGER IF EXISTS trg_evidence_items_legal_hold ON evidence_items;
DROP FUNCTION IF EXISTS prevent_custody_event_modification();
DROP FUNCTION IF EXISTS check_evidence_legal_hold();

DROP TABLE IF EXISTS user_certifications CASCADE;
DROP TABLE IF EXISTS certification_trainings CASCADE;
DROP TABLE IF EXISTS custody_transfer_requests CASCADE;
DROP TABLE IF EXISTS custody_events CASCADE;
DROP TABLE IF EXISTS evidence_items CASCADE;
DROP TABLE IF EXISTS cases CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS agencies CASCADE;

-- Legacy tables cleanup if present
DROP TABLE IF EXISTS corsi_dipendenti CASCADE;
DROP TABLE IF EXISTS corsi_aziende CASCADE;
DROP TABLE IF EXISTS corsi CASCADE;
DROP TABLE IF EXISTS documenti CASCADE;
DROP TABLE IF EXISTS dipendenti CASCADE;
DROP TABLE IF EXISTS consulenti CASCADE;
DROP TABLE IF EXISTS aziende CASCADE;

-- 1. Organizations / Law Enforcement & Forensics Agencies
CREATE TABLE agencies (
    agency_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    phone VARCHAR(30),
    address VARCHAR(255) NOT NULL,
    badge_prefix VARCHAR(10) NOT NULL,
    logo VARCHAR(255)
);

-- 2. Users (Investigators, Evidence Custodians, Supervisors, Auditors)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    badge_number VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL, -- 'SUPERVISOR', 'INVESTIGATOR', 'CUSTODIAN', 'AUDITOR'
    agency_code VARCHAR(20) NOT NULL REFERENCES agencies(agency_code) ON DELETE RESTRICT,
    phone VARCHAR(30),
    piva VARCHAR(20)
);

-- 3. Investigation & Forensic Cases
CREATE TABLE cases (
    id SERIAL PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'Open', -- 'Open', 'Closed', 'Archived'
    agency_code VARCHAR(20) NOT NULL REFERENCES agencies(agency_code) ON DELETE RESTRICT,
    created_by VARCHAR(320) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Evidence Items
CREATE TABLE evidence_items (
    id SERIAL PRIMARY KEY,
    case_id INTEGER NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    evidence_tag VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    file_hash VARCHAR(64) NOT NULL, -- SHA-256 hex string
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT DEFAULT 0,
    uploaded_by VARCHAR(320) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_custodian_id VARCHAR(320) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Collected', -- 'Collected', 'In Custody', 'Under Analysis', 'Released', 'Disposed'
    legal_hold BOOLEAN NOT NULL DEFAULT FALSE,
    integrity_status VARCHAR(20) NOT NULL DEFAULT 'VERIFIED', -- 'VERIFIED', 'COMPROMISED'
    retention_date DATE,
    agency_code VARCHAR(20) NOT NULL REFERENCES agencies(agency_code) ON DELETE RESTRICT
);

-- 5. Append-Only Cryptographic Custody Ledger
CREATE TABLE custody_events (
    id BIGSERIAL PRIMARY KEY,
    evidence_item_id INTEGER NOT NULL REFERENCES evidence_items(id) ON DELETE RESTRICT,
    actor_id VARCHAR(320) NOT NULL,
    action VARCHAR(50) NOT NULL, -- 'UPLOADED', 'VIEWED', 'DOWNLOADED', 'TRANSFERRED', 'ANALYZED', 'RELEASED', 'DISPOSED', 'INTEGRITY_ALERT'
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    from_custodian_id VARCHAR(320),
    to_custodian_id VARCHAR(320),
    notes TEXT,
    previous_event_hash VARCHAR(64) NOT NULL,
    event_hash VARCHAR(64) NOT NULL
);

-- 6. Dual-Confirmation Custody Transfer Requests
CREATE TABLE custody_transfer_requests (
    id SERIAL PRIMARY KEY,
    evidence_item_id INTEGER NOT NULL REFERENCES evidence_items(id) ON DELETE CASCADE,
    from_custodian_id VARCHAR(320) NOT NULL,
    to_custodian_id VARCHAR(320) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'
    released_at TIMESTAMP,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Certification & Chain of Custody Compliance Trainings
CREATE TABLE certification_trainings (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_hours INTEGER NOT NULL,
    category VARCHAR(100) NOT NULL,
    required_role VARCHAR(50),
    agency_code VARCHAR(20) NOT NULL REFERENCES agencies(agency_code) ON DELETE CASCADE
);

CREATE TABLE user_certifications (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    training_id INTEGER NOT NULL REFERENCES certification_trainings(id) ON DELETE CASCADE,
    certified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- ============================================================================
-- DATABASE TRIGGERS & CONSTRAINTS
-- ============================================================================

-- Rule 1: Custody Events are APPEND-ONLY (No updates or deletions allowed)
CREATE OR REPLACE FUNCTION prevent_custody_event_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Custody events are append-only. Modification or deletion is strictly prohibited by law and system policy.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_custody_events_append_only
BEFORE UPDATE OR DELETE ON custody_events
FOR EACH ROW EXECUTE FUNCTION prevent_custody_event_modification();

-- Rule 2: Evidence under Legal Hold cannot be deleted, disposed, or replaced
CREATE OR REPLACE FUNCTION check_evidence_legal_hold()
RETURNS TRIGGER AS $$
BEGIN
    IF (OLD.legal_hold = TRUE) THEN
        IF (TG_OP = 'DELETE') THEN
            RAISE EXCEPTION 'Action blocked: Evidence item % is under active Legal Hold and cannot be deleted.', OLD.evidence_tag;
        ELSIF (TG_OP = 'UPDATE' AND (NEW.status = 'Disposed' OR NEW.file_path <> OLD.file_path)) THEN
            RAISE EXCEPTION 'Action blocked: Evidence item % is under active Legal Hold and cannot be disposed or modified.', OLD.evidence_tag;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_evidence_items_legal_hold
BEFORE UPDATE OR DELETE ON evidence_items
FOR EACH ROW EXECUTE FUNCTION check_evidence_legal_hold();
