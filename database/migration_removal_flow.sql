-- ============================================================================
-- EvidLock Migration: Controlled Evidence Removal & Disposal Governance
-- ============================================================================

-- 1. Add Controlled Removal Governance columns to evidence_items
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS removal_requested_by VARCHAR(320);
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS removal_approved_by VARCHAR(320);
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS removal_reason TEXT;
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS legal_ref VARCHAR(255);
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS removal_requested_at TIMESTAMP;
ALTER TABLE evidence_items ADD COLUMN IF NOT EXISTS removed_at TIMESTAMP;

-- 2. Update users: Promote court@evidlock.gov to dedicated 'COURT' role
UPDATE users SET role = 'COURT' WHERE email = 'court@evidlock.gov';

-- 3. Prevent hard DELETE on evidence_items table
CREATE OR REPLACE FUNCTION prevent_evidence_item_hard_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Hard deletion of evidence items is prohibited by system policy. Removal must proceed via the supervised removal approval workflow.';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_evidence_items_prevent_hard_delete ON evidence_items;

CREATE TRIGGER trg_evidence_items_prevent_hard_delete
BEFORE DELETE ON evidence_items
FOR EACH ROW EXECUTE FUNCTION prevent_evidence_item_hard_delete();
