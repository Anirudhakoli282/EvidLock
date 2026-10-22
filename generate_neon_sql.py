import hashlib

def sha256_hex(s):
    return hashlib.sha256(s.encode('utf-8')).hexdigest()

def compute_event_hash(prev_hash, actor, action, ts_str, item_id):
    raw = f"{prev_hash}{actor}{action}{ts_str}{item_id}"
    return sha256_hex(raw)

# Evidence file hashes
fir_hash = "e5f57a7e40668d18ee5e2203af79175feb4af2ef976e6708c87f89e7e2ee15ef"
seizure_hash = "c04007537d31027304a19916c1dd7475a17c5b4965c126f3ed47069929181286"
cctv_orig_hash = "70a15a9dc5b725358a91a372a1d2ad198e52ba6c793f53b69e437a848b398acd"
fsl_hash = "185bb91584d4072b92f21502d5a46781341b949a701cca06014cbaac43492af6"

actor_investigator = "investigator@evidlock.gov"
actor_custodian = "custodian@evidlock.gov"

# We assume item IDs 101, 102, 103, 104 or next available IDs
# But in SQL we can do dynamic or fixed IDs. Let's do fixed IDs (e.g. 101..104) or subquery with case_number = 'FIR-2026-0245'.
# Better: Let's create a clear SQL script with explicit IDs that don't collide with seed data (e.g., case id 10, item ids 101, 102, 103, 104).

t1 = "2026-10-15 01:30:00"
t2 = "2026-10-15 10:30:00"
t3_upload = "2026-10-15 11:00:00"
t3_transfer = "2026-10-16 14:00:00"
t4 = "2026-10-17 12:00:00"

# Event hashes:
# Item 101 (FIR)
h_fir_0 = compute_event_hash("GENESIS", actor_investigator, "UPLOADED", t1, 101)

# Item 102 (Seizure Memo)
h_seizure_0 = compute_event_hash("GENESIS", actor_investigator, "UPLOADED", t2, 102)

# Item 103 (CCTV Original Frame) - Block 0 (Upload) & Block 1 (Transferred to FSL Custodian)
h_cctv_0 = compute_event_hash("GENESIS", actor_investigator, "UPLOADED", t3_upload, 103)
h_cctv_1 = compute_event_hash(h_cctv_0, actor_custodian, "TRANSFERRED", t3_transfer, 103)

# Item 104 (FSL Phone Report)
h_fsl_0 = compute_event_hash("GENESIS", actor_custodian, "UPLOADED", t4, 104)

sql_content = f"""-- ============================================================================
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
    '{fir_hash}',
    '01_FIR_245_2026.pdf',
    'evidence_demo/01_FIR_245_2026.pdf',
    'application/pdf', 3735,
    'investigator@evidlock.gov', '{t1}',
    'investigator@evidlock.gov', 'Collected', TRUE, 'VERIFIED', '2036-10-15', 'MPD-01'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.2 Scene & DVR Seizure Memo (Panchnama)
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    102, 10, 'EV-2026-00245-02',
    'Scene & DVR Panchnama Seizure Memo - Flat 402 & Gate Security DVR (Sunrise Apts)',
    '{seizure_hash}',
    '02_Scene_and_DVR_Seizure_Memo.pdf',
    'evidence_demo/02_Scene_and_DVR_Seizure_Memo.pdf',
    'application/pdf', 3839,
    'investigator@evidlock.gov', '{t2}',
    'investigator@evidlock.gov', 'Collected', TRUE, 'VERIFIED', '2036-10-15', 'MPD-01'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.3 Original CCTV Frame (CAM 01 GATE - 22:30:12)
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    103, 10, 'EV-2026-00245-03',
    'Original CCTV Video Frame - CAM 01 Gate Ingress at 22:30:12 (Suspect Karan entering compound)',
    '{cctv_orig_hash}',
    '03_CCTV_Frame_ORIGINAL_2230.png',
    'evidence_demo/03_CCTV_Frame_ORIGINAL_2230.png',
    'image/png', 23570,
    'investigator@evidlock.gov', '{t3_upload}',
    'custodian@evidlock.gov', 'In Custody', TRUE, 'VERIFIED', '2036-10-15', 'SFL-02'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 2.4 FSL Phone Extraction Report
INSERT INTO evidence_items (id, case_id, evidence_tag, description, file_hash, original_filename, file_path, file_type, file_size, uploaded_by, uploaded_at, current_custodian_id, status, legal_hold, integrity_status, retention_date, agency_code)
VALUES (
    104, 10, 'EV-2026-00245-04',
    'FSL Digital Forensics Extraction Report - Victim Priya OnePlus 11R Mobile (Recovered 23:30 Message)',
    '{fsl_hash}',
    '05_Phone_Forensic_Report_FSL_2026_0612.pdf',
    'evidence_demo/05_Phone_Forensic_Report_FSL_2026_0612.pdf',
    'application/pdf', 4164,
    'custodian@evidlock.gov', '{t4}',
    'custodian@evidlock.gov', 'Under Analysis', TRUE, 'VERIFIED', '2036-10-15', 'SFL-02'
) ON CONFLICT (evidence_tag) DO UPDATE SET file_hash = EXCLUDED.file_hash, legal_hold = TRUE;

-- 3. Cryptographically Linked Custody Ledger Blocks
-- 3.1 FIR Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    101, 'investigator@evidlock.gov', 'UPLOADED', '{t1}',
    NULL, 'investigator@evidlock.gov',
    'FIR No. 245/2026 registered at Demo PS and sealed into EvidLock digital evidence vault.',
    'GENESIS', '{h_fir_0}'
) ON CONFLICT DO NOTHING;

-- 3.2 Seizure Memo Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    102, 'investigator@evidlock.gov', 'UPLOADED', '{t2}',
    NULL, 'investigator@evidlock.gov',
    'Physical panchnama memo and DVR seizure record ingested into vault repository.',
    'GENESIS', '{h_seizure_0}'
) ON CONFLICT DO NOTHING;

-- 3.3 Original CCTV Frame (Block 0: Ingestion)
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    103, 'investigator@evidlock.gov', 'UPLOADED', '{t3_upload}',
    NULL, 'investigator@evidlock.gov',
    'Uncompressed raw frame extracted from DVR CAM-01 stream (Ingress at 22:30:12) ingested and hashed on Day 1.',
    'GENESIS', '{h_cctv_0}'
) ON CONFLICT DO NOTHING;

-- 3.3 Original CCTV Frame (Block 1: Handover to Forensic Lab Custodian)
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    103, 'custodian@evidlock.gov', 'TRANSFERRED', '{t3_transfer}',
    'investigator@evidlock.gov', 'custodian@evidlock.gov',
    'Dual-custody handover completed. Evidence transferred to SFL-02 Digital Forensics Lab for judicial preservation.',
    '{h_cctv_0}', '{h_cctv_1}'
) ON CONFLICT DO NOTHING;

-- 3.4 Phone Forensic Report Intake
INSERT INTO custody_events (evidence_item_id, actor_id, action, timestamp, from_custodian_id, to_custodian_id, notes, previous_event_hash, event_hash)
VALUES (
    104, 'custodian@evidlock.gov', 'UPLOADED', '{t4}',
    NULL, 'custodian@evidlock.gov',
    'Official SFL laboratory digital extraction report certifying recovered WhatsApp message sent at 23:30:19.',
    'GENESIS', '{h_fsl_0}'
) ON CONFLICT DO NOTHING;
"""

with open("evidence_demo/neon_insert_case.sql", "w") as f:
    f.write(sql_content)

print("Generated evidence_demo/neon_insert_case.sql successfully!")
