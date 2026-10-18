-- ============================================================================
-- EvidLock Demo Seed Data (India-Oriented)
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. Organizations / Agencies (Indian Law Enforcement)
INSERT INTO agencies (agency_code, name, email, phone, address, badge_prefix, logo) VALUES
('MPD-01', 'Mumbai Police - Cyber & Economic Offences Wing', 'cybercrimes@mumbaipolice.gov.in', '+91-22-2262-1855', 'Crawford Market, D.N. Road, Mumbai, Maharashtra 400001', 'MPD', 'assets/img/agency-mpd.png'),
('SFL-02', 'Forensic Science Laboratory - Maharashtra', 'fsl.lab@maharashtra.gov.in', '+91-20-2612-4900', 'Plot No. 17, Aundh, Pune, Maharashtra 411007', 'SFL', 'assets/img/agency-sfl.png'),
('DAO-03', 'Central Bureau of Investigation - Mumbai Branch', 'cbi.mumbai@cbi.gov.in', '+91-22-2262-3535', 'Aayakar Bhavan, M.K. Road, Mumbai, Maharashtra 400020', 'DAO', 'assets/img/agency-dao.png'),
('OIG-04', 'National Investigation Agency - Evidence Audit Cell', 'nia.audit@nia.gov.in', '+91-11-2436-5729', 'CGO Complex, Lodhi Road, New Delhi 110003', 'OIG', 'assets/img/agency-oig.png')
ON CONFLICT (agency_code) DO NOTHING;

-- 2. Users (BCrypt hash for 'Web2324.': $2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re)
INSERT INTO users (email, password, name, badge_number, role, agency_code, phone, piva) VALUES
('supervisor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'ACP Rajesh Patil', 'MPD-SUP-101', 'SUPERVISOR', 'MPD-01', '+91-98201-10001', '36710385611'),
('investigator@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'PI Priya Sharma', 'MPD-INV-402', 'INVESTIGATOR', 'MPD-01', '+91-98202-10002', '23267419900'),
('custodian@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Dr. Vikram Desai', 'SFL-CST-881', 'CUSTODIAN', 'SFL-02', '+91-98203-10003', '19833552187'),
('auditor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Ananya Mehta', 'OIG-AUD-909', 'AUDITOR', 'OIG-04', '+91-98204-10004', '98714353456'),

-- Compatibility with original demo accounts
('alpha@azienda.com', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Admin Supervisor Alpha', 'SUP-DEMO-01', 'SUPERVISOR', 'MPD-01', '+91-98201-11111', '36710385611'),
('beta@azienda.com', '$2a$10$VwO0Kv5vM1ovy.2AyTm2cOoqLiXslGREZV84hcljIkNRy2Lc9S7xW', 'Investigator Beta', 'INV-DEMO-02', 'INVESTIGATOR', 'MPD-01', '+91-98202-22222', '23267419900'),
('gamma@consulente.com', '$2a$10$hhEul7umyLhBn2DbJB.17eCYQAfzuIeSgK6C7/wB8siwf8QHnPmvS', 'Custodian Gamma', 'CST-DEMO-03', 'CUSTODIAN', 'SFL-02', '+91-98203-33333', '19833552187')
ON CONFLICT (email) DO NOTHING;

-- 3. Cases (India-oriented)
INSERT INTO cases (id, case_number, title, description, status, agency_code, created_by, created_at) VALUES
(1, 'CASE-2024-001', 'State v. Raghav Enterprises - Financial Fraud & Hawala', 'Investigation into unauthorized hawala transfers and falsification of digital financial records exceeding ₹14.2 Crore, linked to Mumbai Dharavi branch.', 'Open', 'MPD-01', 'investigator@evidlock.gov', '2024-01-10 09:30:00'),
(2, 'CASE-2024-002', 'Operation Chakravyuh - Narcotics & Encrypted Communications', 'Seizure of encrypted mobile devices and VoIP logs during high-priority interdiction near Nhava Sheva Port, Navi Mumbai.', 'Open', 'MPD-01', 'investigator@evidlock.gov', '2024-02-14 14:15:00'),
(3, 'CASE-2023-089', 'TechVista Cyber Intrusion & Ransomware - Pune IT Park', 'Forensic memory capture and incident response following ransomware deployment on enterprise IT systems at Rajiv Gandhi Infotech Park, Hinjewadi, Pune.', 'Closed', 'SFL-02', 'supervisor@evidlock.gov', '2023-11-05 18:00:00'),
(4, 'CASE-2023-042', 'Dharavi Warehouse Homicide Investigation', 'Physical and electronic evidence collected from scene at Warehouse No. 7, L.B.S. Marg, Kurla West, Mumbai 400070.', 'Archived', 'MPD-01', 'investigator@evidlock.gov', '2023-06-20 22:45:00')
ON CONFLICT (id) DO NOTHING;

SELECT setval('cases_id_seq', (SELECT MAX(id) FROM cases));

-- 4. Evidence Items
INSERT INTO evidence_items (
    id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size,
    uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code
) VALUES
(1, 1, 'EV-2024-00101', 'Seized Encrypted USB Flash Drive (Lexar 64GB - Black/Silver) - Recovered from suspect office, Nariman Point',
 '0a89e662f155dcfe17a8ee08c617bc2972f6271b1d63b71b2897f1c8ebba5aa0',
 'lexar_usb_dump.raw', 'backend/src/main/resources/files/lexar_usb_dump.raw',
 'application/octet-stream', 214, 'investigator@evidlock.gov', '2024-01-11 10:00:00',
 'custodian@evidlock.gov', 'In Custody', TRUE, 'VERIFIED', '2029-12-31', 'MPD-01'),

(2, 2, 'EV-2024-00201', 'Burner Mobile Phone (Samsung Galaxy A12) - Recovered during Nhava Sheva Port interdiction',
 '0b540389889aa5cda8e9b6a6a4beffee754600b65930b139ef769691883b399f',
 'cellbrite_physical_extraction.bin', 'backend/src/main/resources/files/cellbrite_physical_extraction.bin',
 'application/octet-stream', 106, 'investigator@evidlock.gov', '2024-02-15 11:30:00',
 'custodian@evidlock.gov', 'Under Analysis', FALSE, 'VERIFIED', CURRENT_DATE + INTERVAL '14 days', 'MPD-01'),

(3, 2, 'EV-2024-00202', 'Intercepted Encrypted Audio Recording - Court-Authorized Intercept Order No. CBI/2024/047',
 '13e79228b7d4052d169f130e938d4c31b05970bd20e57451687caa889c13254c',
 'wiretap_session_049.wav', 'backend/src/main/resources/files/wiretap_session_049.wav',
 'audio/wav', 112, 'investigator@evidlock.gov', '2024-02-16 16:45:00',
 'investigator@evidlock.gov', 'Collected', FALSE, 'VERIFIED', CURRENT_DATE + INTERVAL '45 days', 'MPD-01')
ON CONFLICT (id) DO NOTHING;

SELECT setval('evidence_items_id_seq', (SELECT MAX(id) FROM evidence_items));

-- 5. Cryptographic Custody Ledger for EV-2024-00101 (Item 1)
-- Genesis block: previous_event_hash = 'GENESIS'
-- Event 1: Uploaded by investigator
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    1, 1, 'investigator@evidlock.gov', 'UPLOADED', '2024-01-11 10:00:00',
    NULL, 'investigator@evidlock.gov', 'Initial forensic acquisition from suspect office, Nariman Point, Mumbai. Bit-stream raw image generated per FSL Maharashtra SOP.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-01-11 10:00:00' || '1', 'sha256'), 'hex')
);

-- Event 2: Transferred from Investigator to FSL Maharashtra Custodian
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    2, 1, 'custodian@evidlock.gov', 'TRANSFERRED', '2024-01-12 14:20:00',
    'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Transferred to FSL Maharashtra, Pune for cryptographic hash verification and secure vault storage.',
    (SELECT event_hash FROM custody_events WHERE id = 1),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 1) || 'custodian@evidlock.gov' || 'TRANSFERRED' || '2024-01-12 14:20:00' || '1', 'sha256'), 'hex')
);

-- Event 3: Viewed & Analyzed
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    3, 1, 'custodian@evidlock.gov', 'ANALYZED', '2024-01-15 09:00:00',
    'custodian@evidlock.gov', 'custodian@evidlock.gov', 'Cryptographic verification complete. Master SHA-256 matches chain of custody intake log. Cleared for court submission.',
    (SELECT event_hash FROM custody_events WHERE id = 2),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 2) || 'custodian@evidlock.gov' || 'ANALYZED' || '2024-01-15 09:00:00' || '1', 'sha256'), 'hex')
);

-- Custody Events for Item 2
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    4, 2, 'investigator@evidlock.gov', 'UPLOADED', '2024-02-15 11:30:00',
    NULL, 'investigator@evidlock.gov', 'Physical recovery from vehicle search, Nhava Sheva Port, Navi Mumbai. Faraday bag used during transit per SOP.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-02-15 11:30:00' || '2', 'sha256'), 'hex')
);

INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    5, 2, 'custodian@evidlock.gov', 'TRANSFERRED', '2024-02-16 10:00:00',
    'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Custody accepted at FSL Maharashtra digital intake station, Aundh, Pune.',
    (SELECT event_hash FROM custody_events WHERE id = 4),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 4) || 'custodian@evidlock.gov' || 'TRANSFERRED' || '2024-02-16 10:00:00' || '2', 'sha256'), 'hex')
);

-- Custody Events for Item 3
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    6, 3, 'investigator@evidlock.gov', 'UPLOADED', '2024-02-16 16:45:00',
    NULL, 'investigator@evidlock.gov', 'Court-authorized intercept under IT Act Section 69, Order No. CBI/2024/047. Digital segment uploaded to evidence vault.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-02-16 16:45:00' || '3', 'sha256'), 'hex')
);

SELECT setval('custody_events_id_seq', (SELECT MAX(id) FROM custody_events));

-- 6. Custody Transfer Requests
INSERT INTO custody_transfer_requests (
    id, evidence_item_id, from_custodian_id, to_custodian_id, reason, status, released_at, accepted_at, created_at
) VALUES
(1, 3, 'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Transfer audio recording to FSL Maharashtra acoustic forensics specialist for speech filtering & frequency enhancement.', 'PENDING', '2024-02-17 08:00:00', NULL, '2024-02-17 08:00:00');

SELECT setval('custody_transfer_requests_id_seq', (SELECT MAX(id) FROM custody_transfer_requests));

-- 7. Certification Trainings
INSERT INTO certification_trainings (id, title, description, duration_hours, category, required_role, agency_code) VALUES
(1, 'IT Act & Digital Evidence Standards - FSL Maharashtra Protocols', 'Comprehensive training on forensic integrity under Indian Evidence Act, anti-tampering hashing protocols, and court presentation standards.', 16, 'Forensic Standards', 'INVESTIGATOR', 'MPD-01'),
(2, 'Cryptographic Ledger & Evidence Vault Management - CBI Guidelines', 'Techniques for securing physical and digital evidence, handling dual-custody confirmations, and audit logging per CBI evidence manual.', 24, 'Evidence Handling', 'CUSTODIAN', 'SFL-02'),
(3, 'Legal Hold Enforcement & Evidence Retention - CrPC Compliance', 'Statutory retention periods under CrPC Section 451, judicial hold management, and disposal authorization workflows per Indian court directives.', 8, 'Legal Compliance', 'SUPERVISOR', 'DAO-03');

SELECT setval('certification_trainings_id_seq', (SELECT MAX(id) FROM certification_trainings));


-- 2. Users (BCrypt hash for 'Web2324.': $2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re)
INSERT INTO users (email, password, name, badge_number, role, agency_code, phone, piva) VALUES
('supervisor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Capt. Marcus Vance', 'MPD-SUP-101', 'SUPERVISOR', 'MPD-01', '+1-555-101-0001', '36710385611'),
('investigator@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Det. Sarah Jenkins', 'MPD-INV-402', 'INVESTIGATOR', 'MPD-01', '+1-555-402-0002', '23267419900'),
('custodian@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Dr. Robert Chen', 'SFL-CST-881', 'CUSTODIAN', 'SFL-02', '+1-555-881-0003', '19833552187'),
('auditor@evidlock.gov', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Elena Rostova', 'OIG-AUD-909', 'AUDITOR', 'OIG-04', '+1-555-909-0004', '98714353456'),

-- Compatibility with original demo accounts
('alpha@azienda.com', '$2a$10$tbkzB9DuvsrZp6/PpUkbN.mOUpaua5LIppshiL.FRGHMoXfx1e2re', 'Admin Supervisor Alpha', 'SUP-DEMO-01', 'SUPERVISOR', 'MPD-01', '+1-555-001-1111', '36710385611'),
('beta@azienda.com', '$2a$10$VwO0Kv5vM1ovy.2AyTm2cOoqLiXslGREZV84hcljIkNRy2Lc9S7xW', 'Investigator Beta', 'INV-DEMO-02', 'INVESTIGATOR', 'MPD-01', '+1-555-002-2222', '23267419900'),
('gamma@consulente.com', '$2a$10$hhEul7umyLhBn2DbJB.17eCYQAfzuIeSgK6C7/wB8siwf8QHnPmvS', 'Custodian Gamma', 'CST-DEMO-03', 'CUSTODIAN', 'SFL-02', '+1-555-003-3333', '19833552187')
ON CONFLICT (email) DO NOTHING;

-- 3. Cases
INSERT INTO cases (id, case_number, title, description, status, agency_code, created_by, created_at) VALUES
(1, 'CASE-2024-001', 'State v. Sterling Financial Fraud & Embezzlement', 'Investigation into unauthorized offshore wire transfers and falsification of digital financial ledgers exceeding $14.2M.', 'Open', 'MPD-01', 'investigator@evidlock.gov', '2024-01-10 09:30:00'),
(2, 'CASE-2024-002', 'Operation Nightfall Narcotics & Digital Communications', 'Seizure of burner telecommunications hardware and encrypted VoIP chat logs during high-priority interdiction.', 'Open', 'MPD-01', 'investigator@evidlock.gov', '2024-02-14 14:15:00'),
(3, 'CASE-2023-089', 'Apex Logistics Cyber Intrusion & Ransomware', 'Forensic memory capture and incident response following LockBit ransomware variant deployment on enterprise logistics network.', 'Closed', 'SFL-02', 'supervisor@evidlock.gov', '2023-11-05 18:00:00'),
(4, 'CASE-2023-042', 'Harbor District Homicide Investigation', 'Physical and electronic evidence collected from scene at Pier 47 warehouse.', 'Archived', 'MPD-01', 'investigator@evidlock.gov', '2023-06-20 22:45:00')
ON CONFLICT (id) DO NOTHING;

SELECT setval('cases_id_seq', (SELECT MAX(id) FROM cases));

-- 4. Evidence Items
INSERT INTO evidence_items (
    id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size,
    uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code
) VALUES
(1, 1, 'EV-2024-00101', 'Seized Encrypted USB Flash Drive (Lexar 64GB - Black/Silver)',
 '0a89e662f155dcfe17a8ee08c617bc2972f6271b1d63b71b2897f1c8ebba5aa0',
 'lexar_usb_dump.raw', 'backend/src/main/resources/files/lexar_usb_dump.raw',
 'application/octet-stream', 214, 'investigator@evidlock.gov', '2024-01-11 10:00:00',
 'custodian@evidlock.gov', 'In Custody', TRUE, 'VERIFIED', '2029-12-31', 'MPD-01'),

(2, 2, 'EV-2024-00201', 'Burner Mobile Phone (Samsung Galaxy A12 - Blue)',
 '0b540389889aa5cda8e9b6a6a4beffee754600b65930b139ef769691883b399f',
 'cellbrite_physical_extraction.bin', 'backend/src/main/resources/files/cellbrite_physical_extraction.bin',
 'application/octet-stream', 106, 'investigator@evidlock.gov', '2024-02-15 11:30:00',
 'custodian@evidlock.gov', 'Under Analysis', FALSE, 'VERIFIED', CURRENT_DATE + INTERVAL '14 days', 'MPD-01'),

(3, 2, 'EV-2024-00202', 'Intercepted Encrypted Audio Wiretap Recording (Target 881)',
 '13e79228b7d4052d169f130e938d4c31b05970bd20e57451687caa889c13254c',
 'wiretap_session_049.wav', 'backend/src/main/resources/files/wiretap_session_049.wav',
 'audio/wav', 112, 'investigator@evidlock.gov', '2024-02-16 16:45:00',
 'investigator@evidlock.gov', 'Collected', FALSE, 'VERIFIED', CURRENT_DATE + INTERVAL '45 days', 'MPD-01')
ON CONFLICT (id) DO NOTHING;

SELECT setval('evidence_items_id_seq', (SELECT MAX(id) FROM evidence_items));

-- 5. Cryptographic Custody Ledger for EV-2024-00101 (Item 1)
-- Genesis block: previous_event_hash = 'GENESIS'
-- Event 1: Uploaded by investigator
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    1, 1, 'investigator@evidlock.gov', 'UPLOADED', '2024-01-11 10:00:00',
    NULL, 'investigator@evidlock.gov', 'Initial forensic acquisition from suspect office desk. Bit-stream raw image generated.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-01-11 10:00:00' || '1', 'sha256'), 'hex')
);

-- Event 2: Transferred from Investigator to State Forensic Lab Custodian
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    2, 1, 'custodian@evidlock.gov', 'TRANSFERRED', '2024-01-12 14:20:00',
    'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Transferred to State Forensic Laboratory vault for cryptographic hash verification and secure storage.',
    (SELECT event_hash FROM custody_events WHERE id = 1),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 1) || 'custodian@evidlock.gov' || 'TRANSFERRED' || '2024-01-12 14:20:00' || '1', 'sha256'), 'hex')
);

-- Event 3: Viewed & Analyzed
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    3, 1, 'custodian@evidlock.gov', 'ANALYZED', '2024-01-15 09:00:00',
    'custodian@evidlock.gov', 'custodian@evidlock.gov', 'Cryptographic verification complete. Master SHA-256 matches chain of custody intake log.',
    (SELECT event_hash FROM custody_events WHERE id = 2),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 2) || 'custodian@evidlock.gov' || 'ANALYZED' || '2024-01-15 09:00:00' || '1', 'sha256'), 'hex')
);

-- Custody Events for Item 2
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    4, 2, 'investigator@evidlock.gov', 'UPLOADED', '2024-02-15 11:30:00',
    NULL, 'investigator@evidlock.gov', 'Physical recovery from vehicle search. Faraday bag utilized during transit.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-02-15 11:30:00' || '2', 'sha256'), 'hex')
);

INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    5, 2, 'custodian@evidlock.gov', 'TRANSFERRED', '2024-02-16 10:00:00',
    'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Custody accepted at forensic digital intake station.',
    (SELECT event_hash FROM custody_events WHERE id = 4),
    encode(digest((SELECT event_hash FROM custody_events WHERE id = 4) || 'custodian@evidlock.gov' || 'TRANSFERRED' || '2024-02-16 10:00:00' || '2', 'sha256'), 'hex')
);

-- Custody Events for Item 3
INSERT INTO custody_events (id, evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    6, 3, 'investigator@evidlock.gov', 'UPLOADED', '2024-02-16 16:45:00',
    NULL, 'investigator@evidlock.gov', 'Court-authorized Title III wiretap digital intercept segment uploaded to evidence vault.',
    'GENESIS',
    encode(digest('GENESIS' || 'investigator@evidlock.gov' || 'UPLOADED' || '2024-02-16 16:45:00' || '3', 'sha256'), 'hex')
);

SELECT setval('custody_events_id_seq', (SELECT MAX(id) FROM custody_events));

-- 6. Custody Transfer Requests
INSERT INTO custody_transfer_requests (
    id, evidence_item_id, from_custodian_id, to_custodian_id, reason, status, released_at, accepted_at, created_at
) VALUES
(1, 3, 'investigator@evidlock.gov', 'custodian@evidlock.gov', 'Transfer audio recording to forensic acoustics specialist for speech filtering & frequency enhancement.', 'PENDING', '2024-02-17 08:00:00', NULL, '2024-02-17 08:00:00');

SELECT setval('custody_transfer_requests_id_seq', (SELECT MAX(id) FROM custody_transfer_requests));

-- 7. Certification Trainings
INSERT INTO certification_trainings (id, title, description, duration_hours, category, required_role, agency_code) VALUES
(1, 'CJIS Chain-of-Custody Standards & Digital Forensics ISO/IEC 27037', 'Comprehensive training on forensic integrity, anti-tampering hashing protocols, and evidentiary court presentation.', 16, 'Forensic Standards', 'INVESTIGATOR', 'MPD-01'),
(2, 'Cryptographic Ledger & Evidence Vault Management', 'Techniques for securing physical and digital evidence, handling dual-custody confirmations, and audit logging.', 24, 'Evidence Handling', 'CUSTODIAN', 'SFL-02'),
(3, 'Legal Hold Enforcement & Evidence Retention Compliance', 'Statutory retention periods, judicial hold management, and disposal authorization workflows.', 8, 'Legal Compliance', 'SUPERVISOR', 'DAO-03');

SELECT setval('certification_trainings_id_seq', (SELECT MAX(id) FROM certification_trainings));
