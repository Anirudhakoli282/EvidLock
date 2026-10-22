-- ============================================================================
-- EVIDLOCK DEMO CASE DATA: "The 10:30 PM Gate Camera"
-- Case FIR No. 245/2026 (Pune City Police & Forensic Science Lab Maharashtra)
-- ============================================================================
-- Copy and paste this into Neon SQL Console to inject the demo case & evidence!

-- 1. Insert Case Record
INSERT INTO cases (id, case_number, title, description, status, agency_code, created_by, created_at)
VALUES (
    10,
    'FIR-2026-0245',
    'State of Maharashtra vs. Karan Demo (The 10:30 PM Gate Camera Case)',
    'Homicide inquiry into the suspicious death of Priya Demo at Flat 402, Sunrise Apartments, Pune. Digital video surveillance from perimeter gate camera CAM-01 and victim mobile telephone extracted under chain of custody.',
    'Open',
    'MPD-01',
    'investigator@evidlock.gov',
    '2026-10-15 01:15:00'
) ON CONFLICT (case_number) DO UPDATE 
SET title = EXCLUDED.title, description = EXCLUDED.description;

-- 2. Insert Evidence Items (with verified SHA-256 Checksums)
-- 2.1 FIR Document
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    101, 10, 'EV-2026-00245-01',
    'First Information Report (FIR No. 245/2026) - Demo Police Station, Pune',
    'e5f57a7e40668d18ee5e2203af79175feb4af2ef976e6708c87f89e7e2ee15ef',
    '01_FIR_245_2026.pdf',
    'evidence_demo/01_FIR_245_2026.pdf',
    'application/pdf', 3735,
    'investigator@evidlock.gov', '2026-10-15 01:30:00',
    'investigator@evidlock.gov', 'Collected', TRUE, 'VERIFIED', '2036-10-15', 'MPD-01'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.2 Scene & DVR Seizure Memo (Panchnama)
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    102, 10, 'EV-2026-00245-02',
    'Scene & DVR Panchnama Seizure Memo - Flat 402 & Gate Security DVR (Sunrise Apts)',
    'c04007537d31027304a19916c1dd7475a17c5b4965c126f3ed47069929181286',
    '02_Scene_and_DVR_Seizure_Memo.pdf',
    'evidence_demo/02_Scene_and_DVR_Seizure_Memo.pdf',
    'application/pdf', 3839,
    'investigator@evidlock.gov', '2026-10-15 10:30:00',
    'investigator@evidlock.gov', 'Collected', TRUE, 'VERIFIED', '2036-10-15', 'MPD-01'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.3 Original CCTV Frame (CAM 01 GATE - 22:30:12)
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    103, 10, 'EV-2026-00245-03',
    'Original CCTV Video Frame - CAM 01 Gate Ingress at 22:30:12 (Suspect Karan entering compound)',
    '70a15a9dc5b725358a91a372a1d2ad198e52ba6c793f53b69e437a848b398acd',
    '03_CCTV_Frame_ORIGINAL_2230.png',
    'evidence_demo/03_CCTV_Frame_ORIGINAL_2230.png',
    'image/png', 23570,
    'investigator@evidlock.gov', '2026-10-15 11:00:00',
    'custodian@evidlock.gov', 'In Custody', TRUE, 'VERIFIED', '2036-10-15', 'SFL-02'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.4 FSL Phone Extraction Report
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    104, 10, 'EV-2026-00245-04',
    'FSL Digital Forensics Extraction Report - Victim Priya OnePlus 11R Mobile (Recovered 23:30 Message)',
    '185bb91584d4072b92f21502d5a46781341b949a701cca06014cbaac43492af6',
    '05_Phone_Forensic_Report_FSL_2026_0612.pdf',
    'evidence_demo/05_Phone_Forensic_Report_FSL_2026_0612.pdf',
    'application/pdf', 4164,
    'custodian@evidlock.gov', '2026-10-17 12:00:00',
    'custodian@evidlock.gov', 'Under Analysis', TRUE, 'VERIFIED', '2036-10-15', 'SFL-02'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 3. Cryptographically Linked Custody Ledger Blocks
-- 3.1 FIR Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    101, 'investigator@evidlock.gov', 'UPLOADED', '2026-10-15 01:30:00',
    NULL, 'investigator@evidlock.gov',
    'FIR No. 245/2026 registered at Demo PS and sealed into EvidLock digital evidence vault.',
    'GENESIS', '594e54d79df7ea08e87214cb3de92d9a416f661fe6c28a0040fd67862b1e3c1b'
) ON CONFLICT DO NOTHING;

-- 3.2 Seizure Memo Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    102, 'investigator@evidlock.gov', 'UPLOADED', '2026-10-15 10:30:00',
    NULL, 'investigator@evidlock.gov',
    'Physical panchnama memo and DVR seizure record ingested into vault repository.',
    'GENESIS', '81b4bd5d9096e100dd84fde42198f3c81ae2632e16275c40d41b13c125eb317d'
) ON CONFLICT DO NOTHING;

-- 3.3 Original CCTV Frame (Block 0: Ingestion)
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    103, 'investigator@evidlock.gov', 'UPLOADED', '2026-10-15 11:00:00',
    NULL, 'investigator@evidlock.gov',
    'Uncompressed raw frame extracted from DVR CAM-01 stream (Ingress at 22:30:12) ingested and hashed on Day 1.',
    'GENESIS', '6813999e9369d948c7cfd5ded675c8e856be5ad790d1605ae363f9448d05a536'
) ON CONFLICT DO NOTHING;

-- 3.3 Original CCTV Frame (Block 1: Handover to Forensic Lab Custodian)
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    103, 'custodian@evidlock.gov', 'TRANSFERRED', '2026-10-16 14:00:00',
    'investigator@evidlock.gov', 'custodian@evidlock.gov',
    'Dual-custody handover completed. Evidence transferred to SFL-02 Digital Forensics Lab for judicial preservation.',
    '6813999e9369d948c7cfd5ded675c8e856be5ad790d1605ae363f9448d05a536', '1685cd6c1ac3eb5b5bc5e73bbbe7d9f6a65c03a2e7fb65faeb1d73f67e8c0784'
) ON CONFLICT DO NOTHING;

-- 3.4 Phone Forensic Report Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    104, 'custodian@evidlock.gov', 'UPLOADED', '2026-10-17 12:00:00',
    NULL, 'custodian@evidlock.gov',
    'Official SFL laboratory digital extraction report certifying recovered WhatsApp message sent at 23:30:19.',
    'GENESIS', '3fe2e6807769f1f0e325235472d9a9bb8006936fe7254469ab28864a7b16e20b'
) ON CONFLICT DO NOTHING;
