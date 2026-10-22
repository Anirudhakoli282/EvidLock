package com.evidlock.persistence.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public class User {
    protected Long id;
    protected String email;
    protected String password;
    protected String name;
    protected String badgeNumber;
    protected String role; // 'SUPERVISOR', 'INVESTIGATOR', 'CUSTODIAN', 'AUDITOR'
    protected String agencyCode;
    protected String phone;
    protected String pIva;

    public User() {}

    public User(Long id, String email, String password, String name, String badgeNumber, String role, String agencyCode, String phone) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.badgeNumber = badgeNumber;
        this.role = role;
        this.agencyCode = agencyCode;
        this.phone = phone;
        this.pIva = agencyCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
        this.pIva = agencyCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPIva() {
        return agencyCode != null ? agencyCode : pIva;
    }

    public void setPIva(String pIva) {
        this.pIva = pIva;
        if (this.agencyCode == null) this.agencyCode = pIva;
    }

    // =========================================================================
    // Security Proxy RBAC Interface: Default implementations deny access
    // =========================================================================

    public ResponseEntity<String> cambioPassword(String oldPw, String newPw) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<Agency> getProfile() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Case Operations
    public ResponseEntity<List<Case>> getCases() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<Case> getCase(Long id) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<Case> createCase(Case c) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<Case> updateCase(Case c) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Evidence Operations
    public ResponseEntity<List<EvidenceItem>> getEvidenceItems(String agencyCode, Long caseId) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> getEvidenceItem(Long id) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> uploadEvidence(MultipartFile file, Long caseId, String tag, String description, Date retentionDate) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> updateEvidence(EvidenceItem item) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<String> deleteEvidence(Long id) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> setLegalHold(Long id, boolean hold) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Custody & Verification Operations
    public ResponseEntity<List<CustodyEvent>> getCustodyEvents(Long evidenceItemId) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<ChainVerificationResult> verifyChain(Long evidenceItemId) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Custody Transfer Operations
    public ResponseEntity<CustodyTransferRequest> initiateTransfer(TransferStepUpDto dto) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<CustodyTransferRequest> acceptTransfer(TransferStepUpDto dto) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<String> rejectTransfer(Long requestId, String reason) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<List<CustodyTransferRequest>> getPendingTransfers() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Dashboard & Metrics
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Certification Trainings
    public ResponseEntity<List<CertificationTraining>> getTrainings() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Controlled Removal Governance Operations (Default FORBIDDEN)
    public ResponseEntity<String> requestEvidenceRemoval(Long evidenceId, String reason, String password, String legalRef) {
        return new ResponseEntity<>("Unauthorized: Your role is not permitted to request evidence removal", HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> approveEvidenceRemoval(Long evidenceId, String legalRef, String password) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<EvidenceItem> rejectEvidenceRemoval(Long evidenceId, String reason, String password) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    public ResponseEntity<List<EvidenceItem>> getPendingRemovals() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
