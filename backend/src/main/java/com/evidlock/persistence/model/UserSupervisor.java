package com.evidlock.persistence.model;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.service.crypto.FileIntegrityService;
import com.evidlock.service.crypto.HashChainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSupervisor extends User {

    public UserSupervisor() {
        super();
        this.role = "SUPERVISOR";
    }

    public UserSupervisor(User user) {
        super(user.getId(), user.getEmail(), user.getPassword(), user.getName(),
                user.getBadgeNumber(), "SUPERVISOR", user.getAgencyCode(), user.getPhone());
    }

    @Override
    public ResponseEntity<String> cambioPassword(String oldPw, String newPw) {
        User stored = DBManager.getInstance().getUserDao().findByEmail(this.email);
        if (stored == null || !Utility.checkPassword(oldPw, stored)) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        stored.setPassword(newPw);
        boolean updated = DBManager.getInstance().getUserDao().updatePassword(stored);
        return updated ? new ResponseEntity<>("Password updated successfully", HttpStatus.OK)
                : new ResponseEntity<>("Failed to update password", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Agency> getProfile() {
        Agency a = DBManager.getInstance().getAgencyDao().findByCode(this.agencyCode);
        return a != null ? new ResponseEntity<>(a, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<List<Case>> getCases() {
        List<Case> cases = DBManager.getInstance().getCaseDao().getAll();
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Case> getCase(Long id) {
        Case c = DBManager.getInstance().getCaseDao().findById(id);
        return c != null ? new ResponseEntity<>(c, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Case> createCase(Case c) {
        if (c.getCaseNumber() == null || c.getTitle() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        c.setCreatedBy(this.email);
        if (c.getAgencyCode() == null) {
            c.setAgencyCode(this.agencyCode);
        }
        Long id = DBManager.getInstance().getCaseDao().insert(c);
        c.setId(id);
        return new ResponseEntity<>(c, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Case> updateCase(Case c) {
        boolean updated = DBManager.getInstance().getCaseDao().update(c);
        return updated ? new ResponseEntity<>(c, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getEvidenceItems(String agencyCode, Long caseId) {
        List<EvidenceItem> list;
        if (caseId != null) {
            list = DBManager.getInstance().getEvidenceItemDao().findByCase(caseId);
        } else if (agencyCode != null && !agencyCode.isBlank()) {
            list = DBManager.getInstance().getEvidenceItemDao().findByAgency(agencyCode);
        } else {
            list = DBManager.getInstance().getEvidenceItemDao().getAll();
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EvidenceItem> getEvidenceItem(Long id) {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(id);
        return item != null ? new ResponseEntity<>(item, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<EvidenceItem> uploadEvidence(MultipartFile file, Long caseId, String tag, String description, Date retentionDate) {
        if (file == null || file.isEmpty() || caseId == null || tag == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            byte[] bytes = file.getBytes();
            String sha256 = FileIntegrityService.getInstance().computeSha256(bytes);
            String savedPath = Utility.uploadFile(tag, file);

            EvidenceItem item = new EvidenceItem();
            item.setCaseId(caseId);
            item.setEvidenceTag(tag);
            item.setDescription(description != null ? description : file.getOriginalFilename());
            item.setFileHash(sha256);
            item.setOriginalFilename(file.getOriginalFilename());
            item.setFilePath(savedPath);
            item.setFileType(file.getContentType());
            item.setFileSize(file.getSize());
            item.setUploadedBy(this.email);
            item.setCurrentCustodianId(this.email);
            item.setStatus("In Custody");
            item.setLegalHold(false);
            item.setIntegrityStatus("VERIFIED");
            item.setRetentionDate(retentionDate != null ? retentionDate : new Date(System.currentTimeMillis() + 31536000000L));
            item.setAgencyCode(this.agencyCode);

            Long id = DBManager.getInstance().getEvidenceItemDao().insert(item);
            item.setId(id);

            // Append initial immutable UPLOADED block to cryptographic ledger
            HashChainService.getInstance().logEvent(
                    id,
                    this.email,
                    "UPLOADED",
                    null,
                    this.email,
                    "Initial forensic evidence upload by Supervisor " + this.name + " (" + this.badgeNumber + ")"
            );

            return new ResponseEntity<>(item, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EvidenceItem> updateEvidence(EvidenceItem item) {
        try {
            boolean updated = DBManager.getInstance().getEvidenceItemDao().update(item);
            if (updated) {
                HashChainService.getInstance().logEvent(
                        item.getId(),
                        this.email,
                        "ANALYZED",
                        item.getCurrentCustodianId(),
                        item.getCurrentCustodianId(),
                        "Evidence metadata updated by Supervisor " + this.name
                );
                return new ResponseEntity<>(item, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.LOCKED); // Legal hold blocked
        }
    }

    @Override
    public ResponseEntity<String> deleteEvidence(Long id) {
        return new ResponseEntity<>("Hard deletion is prohibited by system policy and ISO 27037 standards. Removal must proceed via the supervised removal approval workflow.", HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<String> requestEvidenceRemoval(Long evidenceId, String reason, String password, String legalRef) {
        // 1. Password verification
        if (password == null || !Utility.checkPassword(password, this)) {
            return new ResponseEntity<>("Authentication failure: Invalid supervisor password for removal authorization", HttpStatus.UNAUTHORIZED);
        }

        // 2. Reason validation (min 20 characters)
        if (reason == null || reason.trim().length() < 20) {
            return new ResponseEntity<>("Mandatory removal justification requires at least 20 characters", HttpStatus.BAD_REQUEST);
        }

        // 3. Evidence existence
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceId);
        if (item == null) {
            return new ResponseEntity<>("Evidence item not found", HttpStatus.NOT_FOUND);
        }

        // 4. Rule check: Legal Hold LOCKED
        if (item.isLegalHold()) {
            return new ResponseEntity<>("Operation blocked: Evidence item is under active Legal Hold and cannot be requested for removal", HttpStatus.LOCKED);
        }

        // 5. Rule check: In-transfer evidence
        List<CustodyTransferRequest> transfers = DBManager.getInstance().getCustodyTransferRequestDao().findByEvidenceItem(evidenceId);
        if (transfers != null && transfers.stream().anyMatch(t -> "PENDING".equalsIgnoreCase(t.getStatus()))) {
            return new ResponseEntity<>("Operation blocked: Evidence item has a pending custody transfer in progress", HttpStatus.CONFLICT);
        }

        // 6. Rule check: Integrity status must be VERIFIED / SECURE (tampered evidence must be preserved as proof)
        if (!"VERIFIED".equalsIgnoreCase(item.getIntegrityStatus())) {
            return new ResponseEntity<>("Operation blocked: Evidence integrity status is " + item.getIntegrityStatus() + ". Compromised evidence must be preserved as criminal proof.", HttpStatus.CONFLICT);
        }

        // 7. Rule check: Retention date passed OR court order / legal ref provided
        boolean retentionPassed = (item.getRetentionDate() != null && item.getRetentionDate().getTime() <= System.currentTimeMillis());
        boolean hasLegalRef = (legalRef != null && !legalRef.trim().isEmpty());
        if (!retentionPassed && !hasLegalRef) {
            return new ResponseEntity<>("Removal request requires either an expired retention date or a valid court order / case-closure legal reference", HttpStatus.BAD_REQUEST);
        }

        // 8. Update status to REMOVAL_REQUESTED
        item.setStatus("REMOVAL_REQUESTED");
        item.setRemovalRequestedBy(this.email);
        item.setRemovalReason(reason.trim());
        item.setLegalRef(hasLegalRef ? legalRef.trim() : null);
        item.setRemovalRequestedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        boolean updated = DBManager.getInstance().getEvidenceItemDao().updateRemovalStatus(item);
        if (!updated) {
            return new ResponseEntity<>("Failed to update evidence removal status in database", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 9. Append REMOVAL_REQUESTED block to hash chain ledger
        HashChainService.getInstance().logEvent(
                item.getId(),
                this.email,
                "REMOVAL_REQUESTED",
                item.getCurrentCustodianId(),
                item.getCurrentCustodianId(),
                "Removal requested by Supervisor " + this.name + " (" + this.badgeNumber + "). Reason: " + reason.trim() +
                        (hasLegalRef ? " | Legal Ref: " + legalRef.trim() : "")
        );

        return new ResponseEntity<>("Evidence removal request submitted successfully. Awaiting independent Court / Judicial approval.", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getPendingRemovals() {
        return new ResponseEntity<>(DBManager.getInstance().getEvidenceItemDao().findPendingRemovals(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<EvidenceItem> setLegalHold(Long id, boolean hold) {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(id);
        if (item == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        boolean updated = DBManager.getInstance().getEvidenceItemDao().updateLegalHold(id, hold);
        if (updated) {
            item.setLegalHold(hold);
            String action = hold ? "LEGAL_HOLD_PLACED" : "LEGAL_HOLD_LIFTED";
            String note = hold ? "Legal hold placed by Supervisor " + this.name + " (" + this.badgeNumber + "). Disposal and modifications locked."
                               : "Legal hold lifted by Supervisor " + this.name + " (" + this.badgeNumber + ").";
            HashChainService.getInstance().logEvent(
                    id,
                    this.email,
                    action,
                    item.getCurrentCustodianId(),
                    item.getCurrentCustodianId(),
                    note
            );
            return new ResponseEntity<>(item, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<CustodyEvent>> getCustodyEvents(Long evidenceItemId) {
        List<CustodyEvent> events = DBManager.getInstance().getCustodyEventDao().findByEvidenceItem(evidenceItemId);
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChainVerificationResult> verifyChain(Long evidenceItemId) {
        ChainVerificationResult result = HashChainService.getInstance().verifyChain(evidenceItemId);
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceItemId);
        if (item != null) {
            result.setEvidenceTag(item.getEvidenceTag());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CustodyTransferRequest> initiateTransfer(TransferStepUpDto dto) {
        // Verify Step-Up Password
        User stored = DBManager.getInstance().getUserDao().findByEmail(this.email);
        if (dto.getPassword() == null || !Utility.checkPassword(dto.getPassword(), stored)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Step-up auth failed
        }

        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(dto.getEvidenceItemId());
        if (item == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if ("REMOVED".equalsIgnoreCase(item.getStatus()) || "REMOVAL_REQUESTED".equalsIgnoreCase(item.getStatus())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        CustodyTransferRequest req = new CustodyTransferRequest();
        req.setEvidenceItemId(item.getId());
        req.setFromCustodianId(this.email);
        req.setToCustodianId(dto.getToCustodianId());
        req.setReason(dto.getReason() != null ? dto.getReason() : "Supervisor transfer authorization");
        req.setStatus("PENDING");
        req.setReleasedAt(new Timestamp(System.currentTimeMillis()));

        Long id = DBManager.getInstance().getCustodyTransferRequestDao().insert(req);
        req.setId(id);
        req.setEvidenceTag(item.getEvidenceTag());

        return new ResponseEntity<>(req, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CustodyTransferRequest> acceptTransfer(TransferStepUpDto dto) {
        // Verify Step-Up Password
        User stored = DBManager.getInstance().getUserDao().findByEmail(this.email);
        if (dto.getPassword() == null || !Utility.checkPassword(dto.getPassword(), stored)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Step-up auth failed
        }

        CustodyTransferRequest req = DBManager.getInstance().getCustodyTransferRequestDao().findById(dto.getRequestId());
        if (req == null || !"PENDING".equals(req.getStatus())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        DBManager.getInstance().getCustodyTransferRequestDao().updateStatus(req.getId(), "ACCEPTED", now, true);

        // Update evidence item custodian
        DBManager.getInstance().getEvidenceItemDao().updateCustodian(req.getEvidenceItemId(), this.email, "In Custody");

        // Append TRANSFERRED block to hash chain
        HashChainService.getInstance().logEvent(
                req.getEvidenceItemId(),
                this.email,
                "TRANSFERRED",
                req.getFromCustodianId(),
                this.email,
                "Dual-confirmation transfer completed. Accepted by " + this.name + " (" + this.badgeNumber + "). Reason: " + req.getReason()
        );

        req.setStatus("ACCEPTED");
        req.setAcceptedAt(now);
        return new ResponseEntity<>(req, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> rejectTransfer(Long requestId, String reason) {
        CustodyTransferRequest req = DBManager.getInstance().getCustodyTransferRequestDao().findById(requestId);
        if (req == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        DBManager.getInstance().getCustodyTransferRequestDao().updateStatus(requestId, "REJECTED", null, false);
        return new ResponseEntity<>("Transfer rejected", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CustodyTransferRequest>> getPendingTransfers() {
        List<CustodyTransferRequest> list = DBManager.getInstance().getCustodyTransferRequestDao().getAllPending();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> m = new HashMap<>();
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

    @Override
    public ResponseEntity<List<CertificationTraining>> getTrainings() {
        return new ResponseEntity<>(DBManager.getInstance().getCertificationTrainingDao().getAll(), HttpStatus.OK);
    }
}
