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

public class UserInvestigator extends User {

    public UserInvestigator() {
        super();
        this.role = "INVESTIGATOR";
    }

    public UserInvestigator(User user) {
        super(user.getId(), user.getEmail(), user.getPassword(), user.getName(),
                user.getBadgeNumber(), "INVESTIGATOR", user.getAgencyCode(), user.getPhone());
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
        // Investigators view cases for their agency
        List<Case> cases = DBManager.getInstance().getCaseDao().findByAgency(this.agencyCode);
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
        c.setAgencyCode(this.agencyCode);
        c.setStatus("Open");
        Long id = DBManager.getInstance().getCaseDao().insert(c);
        c.setId(id);
        return new ResponseEntity<>(c, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Case> updateCase(Case c) {
        // Investigators cannot close or archive cases; only supervisors can
        if ("Closed".equalsIgnoreCase(c.getStatus()) || "Archived".equalsIgnoreCase(c.getStatus())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        boolean updated = DBManager.getInstance().getCaseDao().update(c);
        return updated ? new ResponseEntity<>(c, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<List<EvidenceItem>> getEvidenceItems(String agencyCode, Long caseId) {
        List<EvidenceItem> list;
        if (caseId != null) {
            list = DBManager.getInstance().getEvidenceItemDao().findByCase(caseId);
        } else {
            list = DBManager.getInstance().getEvidenceItemDao().findByAgency(this.agencyCode);
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
            item.setStatus("Collected");
            item.setLegalHold(false);
            item.setIntegrityStatus("VERIFIED");
            item.setRetentionDate(retentionDate != null ? retentionDate : new Date(System.currentTimeMillis() + 31536000000L));
            item.setAgencyCode(this.agencyCode);

            Long id = DBManager.getInstance().getEvidenceItemDao().insert(item);
            item.setId(id);

            // Log UPLOADED block in hash chain
            HashChainService.getInstance().logEvent(
                    id,
                    this.email,
                    "UPLOADED",
                    null,
                    this.email,
                    "Evidence collected and uploaded by Investigator " + this.name + " (" + this.badgeNumber + ")"
            );

            return new ResponseEntity<>(item, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<EvidenceItem> updateEvidence(EvidenceItem item) {
        // Investigators cannot change status to Disposed
        if ("Disposed".equalsIgnoreCase(item.getStatus())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        boolean updated = DBManager.getInstance().getEvidenceItemDao().update(item);
        return updated ? new ResponseEntity<>(item, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<String> deleteEvidence(Long id) {
        // Investigators are strictly prohibited from deleting evidence
        return new ResponseEntity<>("Investigators cannot delete evidence items", HttpStatus.FORBIDDEN);
    }

    @Override
    public ResponseEntity<EvidenceItem> setLegalHold(Long id, boolean hold) {
        // Investigators cannot place or lift legal holds; only supervisors can
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

        // Investigator can only transfer evidence currently in their custody
        if (!this.email.equalsIgnoreCase(item.getCurrentCustodianId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        CustodyTransferRequest req = new CustodyTransferRequest();
        req.setEvidenceItemId(item.getId());
        req.setFromCustodianId(this.email);
        req.setToCustodianId(dto.getToCustodianId());
        req.setReason(dto.getReason() != null ? dto.getReason() : "Evidence submitted for laboratory analysis/vault custody");
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

        // Only the designated recipient can accept
        if (!this.email.equalsIgnoreCase(req.getToCustodianId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        DBManager.getInstance().getCustodyTransferRequestDao().updateStatus(req.getId(), "ACCEPTED", now, true);
        DBManager.getInstance().getEvidenceItemDao().updateCustodian(req.getEvidenceItemId(), this.email, "In Custody");

        HashChainService.getInstance().logEvent(
                req.getEvidenceItemId(),
                this.email,
                "TRANSFERRED",
                req.getFromCustodianId(),
                this.email,
                "Custody transfer accepted by Investigator " + this.name + " (" + this.badgeNumber + ")"
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
        List<Case> cases = DBManager.getInstance().getCaseDao().findByAgency(this.agencyCode);
        m.put("totalCases", cases.size());
        m.put("openCases", cases.stream().filter(c -> "Open".equalsIgnoreCase(c.getStatus())).count());
        m.put("closedCases", cases.stream().filter(c -> "Closed".equalsIgnoreCase(c.getStatus())).count());

        List<EvidenceItem> evidence = DBManager.getInstance().getEvidenceItemDao().findByAgency(this.agencyCode);
        m.put("totalEvidence", evidence.size());
        m.put("myCustodyCount", evidence.stream().filter(e -> this.email.equalsIgnoreCase(e.getCurrentCustodianId())).count());
        m.put("legalHoldCount", evidence.stream().filter(EvidenceItem::isLegalHold).count());
        m.put("recentEvents", DBManager.getInstance().getCustodyEventDao().findRecent(10));
        m.put("pendingTransfersCount", DBManager.getInstance().getCustodyTransferRequestDao().findPendingForCustodian(this.email).size());

        return new ResponseEntity<>(m, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CertificationTraining>> getTrainings() {
        return new ResponseEntity<>(DBManager.getInstance().getCertificationTrainingDao().getAll(), HttpStatus.OK);
    }
}
