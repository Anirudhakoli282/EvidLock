package com.evidlock.persistence.model;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.service.crypto.HashChainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.List;

/**
 * Security Proxy for Court / Judicial Officers (Public Prosecutors, Magistrates, Judges).
 * Independent judicial authority possessing sole authorization to approve or reject
 * evidence removal/disposal orders pursuant to court mandates.
 */
public class UserCourt extends User {

    public UserCourt() {
        super();
        this.role = "COURT";
    }

    @Override
    public ResponseEntity<List<Case>> getCases() {
        List<Case> cases = DBManager.getInstance().getCaseDao().getAll();
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Case> getCase(Long id) {
        Case c = DBManager.getInstance().getCaseDao().findById(id);
        if (c == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(c, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getEvidenceItems(String agencyCode, Long caseId) {
        if (caseId != null) {
            return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findByCase(caseId), HttpStatus.OK);
        }
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().getAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EvidenceItem> getEvidenceItem(Long id) {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(id);
        if (item == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CustodyEvent>> getCustodyEvents(Long evidenceItemId) {
        List<CustodyEvent> events = DBManager.getInstance().getCustodyEventDao().findByEvidenceItem(evidenceItemId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChainVerificationResult> verifyChain(Long evidenceItemId) {
        ChainVerificationResult result = HashChainService.getInstance().verifyChain(evidenceItemId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getPendingRemovals() {
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findPendingRemovals(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EvidenceItem> approveEvidenceRemoval(Long evidenceId, String legalRef, String password) {
        // 1. Password verification
        if (password == null || !Utility.checkPassword(password, this)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 2. Mandatory court order / legal reference
        if (legalRef == null || legalRef.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 3. Evidence existence and status check
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceId);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!"REMOVAL_REQUESTED".equalsIgnoreCase(item.getStatus())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // 4. Separation of Concerns & Four-Eyes Principle:
        // Rule: Approver must NEVER be the requester
        if (this.email.equalsIgnoreCase(item.getRemovalRequestedBy())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Rule: Approver must NEVER be the current custodian
        if (this.email.equalsIgnoreCase(item.getCurrentCustodianId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // Rule: Approver must NEVER be the investigator who owns the case or uploader
        Case c = DBManager.getInstance().getCaseDao().findById(item.getCaseId());
        if (c != null && this.email.equalsIgnoreCase(c.getCreatedBy())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (this.email.equalsIgnoreCase(item.getUploadedBy())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // 5. Apply Approved Removal & Tombstone Preservation
        item.setStatus("REMOVED");
        item.setRemovalApprovedBy(this.email);
        item.setLegalRef(legalRef.trim());
        item.setRemovedAt(new Timestamp(System.currentTimeMillis()));

        // Move physical file to sealed archive directory or preserve tombstone record
        if (item.getFilePath() != null && !item.getFilePath().isBlank()) {
            try {
                Path sourcePath = Paths.get(item.getFilePath());
                if (Files.exists(sourcePath)) {
                    Path archiveDir = Paths.get("backend/src/main/resources/files/sealed_archive");
                    if (!Files.exists(archiveDir)) {
                        Files.createDirectories(archiveDir);
                    }
                    Path targetPath = archiveDir.resolve("sealed_" + sourcePath.getFileName().toString());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    item.setFilePath(targetPath.toString().replace("\\", "/"));
                }
            } catch (Exception ex) {
                // Keep tombstone metadata intact
                item.setFilePath("SEALED_ARCHIVE_TOMBSTONE");
            }
        }

        boolean updated = DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);
        if (!updated) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 6. Append REMOVAL_APPROVED & EVIDENCE_REMOVED events to immutable Hash Chain Ledger
        HashChainService.getInstance().logEvent(
                item.getId(),
                this.email,
                "REMOVAL_APPROVED",
                item.getCurrentCustodianId(),
                item.getCurrentCustodianId(),
                "Judicial removal authorized by Court Officer " + this.name + " (" + this.badgeNumber + "). Court Order Ref: " + legalRef.trim()
        );

        HashChainService.getInstance().logEvent(
                item.getId(),
                this.email,
                "EVIDENCE_REMOVED",
                item.getCurrentCustodianId(),
                null,
                "Evidence payload sealed into court archive pursuant to Order " + legalRef.trim() + ". Master SHA-256 [" + item.getFileHash() + "] permanently preserved in tombstone registry."
        );

        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EvidenceItem> rejectEvidenceRemoval(Long evidenceId, String reason, String password) {
        // 1. Password verification
        if (password == null || !Utility.checkPassword(password, this)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // 2. Reason validation
        if (reason == null || reason.trim().length() < 10) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 3. Evidence existence and status check
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceId);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!"REMOVAL_REQUESTED".equalsIgnoreCase(item.getStatus())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // 4. Reset status back to In Custody / Active
        item.setStatus("In Custody");
        String rejectionNote = "Removal request rejected by Court Officer " + this.name + " (" + this.badgeNumber + "): " + reason.trim();
        item.setRemovalReason(rejectionNote);

        boolean updated = DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);
        if (!updated) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 5. Append REMOVAL_REJECTED block to ledger
        HashChainService.getInstance().logEvent(
                item.getId(),
                this.email,
                "REMOVAL_REJECTED",
                item.getCurrentCustodianId(),
                item.getCurrentCustodianId(),
                rejectionNote
        );

        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<java.util.Map<String, Object>> getDashboardMetrics() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("totalCases", DBManager.getInstance().getCaseDao().getAll().size());
        m.put("openCases", DBManager.getInstance().getCaseDao().countByStatus("Open"));
        m.put("closedCases", DBManager.getInstance().getCaseDao().countByStatus("Closed"));
        m.put("archivedCases", DBManager.getInstance().getCaseDao().countByStatus("Archived"));

        List<EvidenceItem> allEvidence = DBManager.getInstance().getEvidenceItemDao().getAll();
        m.put("totalEvidence", allEvidence.size());
        m.put("legalHoldCount", allEvidence.stream().filter(EvidenceItem::isLegalHold).count());
        m.put("compromisedCount", DBManager.getInstance().getEvidenceItemDao().findIntegrityCompromised().size());
        m.put("nearingRetentionCount", DBManager.getInstance().getEvidenceItemDao().findNearingRetention(30).size());
        m.put("disposalEligibleCount", DBManager.getInstance().getEvidenceItemDao().findEligibleForDisposal().size());
        m.put("pendingRemovalsCount", allEvidence.stream().filter(e -> "REMOVAL_REQUESTED".equalsIgnoreCase(e.getStatus())).count());
        m.put("removedCount", allEvidence.stream().filter(e -> "REMOVED".equalsIgnoreCase(e.getStatus())).count());
        m.put("recentEvents", DBManager.getInstance().getCustodyEventDao().findRecent(10));
        m.put("pendingTransfersCount", DBManager.getInstance().getCustodyTransferRequestDao().getAllPending().size());

        return new ResponseEntity<>(m, HttpStatus.OK);
    }
}
