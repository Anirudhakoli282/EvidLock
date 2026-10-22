package com.evidlock.persistence.model;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.service.crypto.HashChainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCustodian extends User {

    public UserCustodian() {
        super();
        this.role = "CUSTODIAN";
    }

    public UserCustodian(User user) {
        super(user.getId(), user.getEmail(), user.getPassword(), user.getName(),
                user.getBadgeNumber(), "CUSTODIAN", user.getAgencyCode(), user.getPhone());
    }

    @Override
    public ResponseEntity<String> cambioPassword(String oldPw, String newPw) {
        User stored = DBManager.getInstance().getUserDao().findByEmail(this.email);
        if (stored == null || !Utility.checkPassword(oldPw, stored)) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        stored.setPassword(newPw);
        boolean updated = DBManager.getInstance().getUserDao().updatePassword(stored);
        return updated ? new ResponseEntity<>("Password updated", HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<Agency> getProfile() {
        Agency a = DBManager.getInstance().getAgencyDao().findByCode(this.agencyCode);
        return a != null ? new ResponseEntity<>(a, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<List<Case>> getCases() {
        return new ResponseEntity<>(DBManager.getInstance().getCaseDao().getAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Case> getCase(Long id) {
        Case c = DBManager.getInstance().getCaseDao().findById(id);
        return c != null ? new ResponseEntity<>(c, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Case> createCase(Case c) {
        // Custodians do not create investigation cases
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<Case> updateCase(Case c) {
        // Custodians do not modify case metadata
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getEvidenceItems(String agencyCode, Long caseId) {
        List<EvidenceItem> list;
        if (caseId != null) {
            list = DBManager.getInstance().getEvidenceItemDao().findByCase(caseId);
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
    public ResponseEntity<EvidenceItem> updateEvidence(EvidenceItem item) {
        // Custodians can update custody status e.g. In Custody, Under Analysis, Released
        if ("Disposed".equalsIgnoreCase(item.getStatus())) {
            // Cannot dispose if legal hold is true
            EvidenceItem existing = DBManager.getInstance().getEvidenceItemDao().findById(item.getId());
            if (existing != null && existing.isLegalHold()) {
                return new ResponseEntity<>(HttpStatus.LOCKED);
            }
        }
        boolean updated = DBManager.getInstance().getEvidenceItemDao().update(item);
        if (updated) {
            HashChainService.getInstance().logEvent(
                    item.getId(),
                    this.email,
                    "ANALYZED",
                    this.email,
                    this.email,
                    "Custody status updated to " + item.getStatus() + " by Custodian " + this.name
            );
            return new ResponseEntity<>(item, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<String> deleteEvidence(Long id) {
        // Custodians cannot delete evidence records
        return new ResponseEntity<>("Custodians cannot delete evidence", HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<EvidenceItem> setLegalHold(Long id, boolean hold) {
        // Only supervisors can place or lift legal holds
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(dto.getEvidenceItemId());
        if (item == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        CustodyTransferRequest req = new CustodyTransferRequest();
        req.setEvidenceItemId(item.getId());
        req.setFromCustodianId(this.email);
        req.setToCustodianId(dto.getToCustodianId());
        req.setReason(dto.getReason() != null ? dto.getReason() : "Custodian vault transfer");
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
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        CustodyTransferRequest req = DBManager.getInstance().getCustodyTransferRequestDao().findById(dto.getRequestId());
        if (req == null || !"PENDING".equals(req.getStatus())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!this.email.equalsIgnoreCase(req.getToCustodianId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        DBManager.getInstance().getCustodyTransferRequestDao().updateStatus(req.getId(), "ACCEPTED", now, true);
        DBManager.getInstance().getEvidenceItemDao().updateCustodian(req.getEvidenceItemId(), this.email, "In Custody");

        // Log TRANSFERRED event in immutable hash chain
        HashChainService.getInstance().logEvent(
                req.getEvidenceItemId(),
                this.email,
                "TRANSFERRED",
                req.getFromCustodianId(),
                this.email,
                "Custody accepted at vault by Custodian " + this.name + " (" + this.badgeNumber + ")"
        );

        req.setStatus("ACCEPTED");
        req.setAcceptedAt(now);
        return new ResponseEntity<>(req, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> rejectTransfer(Long requestId, String reason) {
        CustodyTransferRequest req = DBManager.getInstance().getCustodyTransferRequestDao().findById(requestId);
        if (req == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!this.email.equalsIgnoreCase(req.getToCustodianId()) && !this.email.equalsIgnoreCase(req.getFromCustodianId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        DBManager.getInstance().getCustodyTransferRequestDao().updateStatus(requestId, "REJECTED", null, false);
        return new ResponseEntity<>("Transfer rejected", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CustodyTransferRequest>> getPendingTransfers() {
        List<CustodyTransferRequest> list = DBManager.getInstance().getCustodyTransferRequestDao().findPendingForCustodian(this.email);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> m = new HashMap<>();
        List<EvidenceItem> all = DBManager.getInstance().getEvidenceItemDao().getAll();
        m.put("totalEvidence", all.size());
        m.put("inMyCustody", all.stream().filter(e -> this.email.equalsIgnoreCase(e.getCurrentCustodianId())).count());
        m.put("underAnalysis", all.stream().filter(e -> "Under Analysis".equalsIgnoreCase(e.getStatus())).count());
        m.put("legalHoldCount", all.stream().filter(EvidenceItem::isLegalHold).count());
        m.put("pendingTransfersCount", DBManager.getInstance().getCustodyTransferRequestDao().findPendingForCustodian(this.email).size());
        m.put("recentEvents", DBManager.getInstance().getCustodyEventDao().findRecent(10));
        return new ResponseEntity<>(m, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CertificationTraining>> getTrainings() {
        return new ResponseEntity<>(DBManager.getInstance().getCertificationTrainingDao().getAll(), HttpStatus.OK);
    }
}
