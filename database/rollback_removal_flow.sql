-- ============================================================================
-- EvidLock Migration Rollback: Revert Controlled Evidence Removal Feature
-- ============================================================================
-- Run this in your Neon PostgreSQL SQL Console to roll back the database changes cleanly.

-- 1. Drop the blocking hard-delete trigger and function
DROP TRIGGER IF EXISTS trg_evidence_items_prevent_hard_delete ON evidence_items;
DROP FUNCTION IF EXISTS prevent_evidence_item_hard_delete();

-- 2. Restore court@evidlock.gov role to AUDITOR
UPDATE users SET role = 'AUDITOR' WHERE email = 'court@evidlock.gov';

-- 3. Reset any evidence rows that were put into removal statuses back to 'In Custody'
UPDATE evidence_items 
SET status = 'In Custody' 
WHERE status IN ('REMOVED', 'REMOVAL_REQUESTED');

-- 4. Drop the 6 removal columns added by migration_removal_flow.sql
ALTER TABLE evidence_items DROP COLUMN IF EXISTS removal_requested_by;
ALTER TABLE evidence_items DROP COLUMN IF EXISTS removal_approved_by;
ALTER TABLE evidence_items DROP COLUMN IF EXISTS removal_reason;
ALTER TABLE evidence_items DROP COLUMN IF EXISTS legal_ref;
ALTER TABLE evidence_items DROP COLUMN IF EXISTS removal_requested_at;
ALTER TABLE evidence_items DROP COLUMN IF EXISTS removed_at;
