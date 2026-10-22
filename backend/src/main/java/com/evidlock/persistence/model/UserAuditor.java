package com.evidlock.persistence.model;

import com.evidlock.controller.Utility;
import com.evidlock.persistence.DBManager;
import com.evidlock.service.crypto.HashChainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Security Proxy for AUDITOR Role.
 * Provides comprehensive, strictly READ-ONLY access across all cases, evidence,
 * cryptographic chains, and audit logs.
 * All write, modification, deletion, transfer, and hold operations are strictly FORBIDDEN.
 */
public class UserAuditor extends User {

    public UserAuditor() {
        super();
        this.role = "AUDITOR";
    }

    public UserAuditor(User user) {
        super(user.getId(), user.getEmail(), user.getPassword(), user.getName(),
                user.getBadgeNumber(), "AUDITOR", user.getAgencyCode(), user.getPhone());
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

    // =========================================================================
    // READ-ONLY ACCESS: Full visibility into Cases and Evidence
    // =========================================================================

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
    public ResponseEntity<List<CustodyTransferRequest>> getPendingTransfers() {
        return new ResponseEntity<>(DBManager.getInstance().getCustodyTransferRequestDao().getAll(), HttpStatus.OK);
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
        m.put("pendingRemovalsCount", DBManager.getInstance().getEvidenceItemDao().findPendingRemovals().size());
        m.put("removedCount", DBManager.getInstance().getEvidenceItemDao().findRemoved().size());
        m.put("recentEvents", DBManager.getInstance().getCustodyEventDao().findRecent(20));
        m.put("pendingTransfersCount", DBManager.getInstance().getCustodyTransferRequestDao().getAllPending().size());

        return new ResponseEntity<>(m, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CertificationTraining>> getTrainings() {
        return new ResponseEntity<>(DBManager.getInstance().getCertificationTrainingDao().getAll(), HttpStatus.OK);
    }

    // =========================================================================
    // STRICT FORBIDDEN ENFORCEMENT: All write/mutation methods denied
    // =========================================================================

    @Override
    public ResponseEntity<Case> createCase(Case c) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<Case> updateCase(Case c) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<EvidenceItem> uploadEvidence(MultipartFile file, Long caseId, String tag, String description, Date retentionDate) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<EvidenceItem> updateEvidence(EvidenceItem item) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<String> deleteEvidence(Long id) {
        return new ResponseEntity<>("Auditors have read-only access and cannot delete evidence", HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<EvidenceItem> setLegalHold(Long id, boolean hold) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<CustodyTransferRequest> initiateTransfer(TransferStepUpDto dto) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<CustodyTransferRequest> acceptTransfer(TransferStepUpDto dto) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<String> rejectTransfer(Long requestId, String reason) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
