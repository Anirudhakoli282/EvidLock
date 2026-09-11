package com.evidlock.persistence.model;

import java.sql.Date;
import java.sql.Timestamp;

public class EvidenceItem {
    private Long id;
    private Long caseId;
    private String caseNumber; // convenience field for display
    private String evidenceTag;
    private String description;
    private String fileHash; // SHA-256 hex
    private String originalFilename;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String uploadedBy;
    private Timestamp uploadedAt;
    private String currentCustodianId;
    private String status; // 'Collected', 'In Custody', 'Under Analysis', 'Released', 'Disposed'
    private boolean legalHold;
    private String integrityStatus; // 'VERIFIED', 'COMPROMISED'
    private Date retentionDate;
    private String agencyCode;

    public EvidenceItem() {
        this.status = "Collected";
        this.legalHold = false;
        this.integrityStatus = "VERIFIED";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getEvidenceTag() {
        return evidenceTag;
    }

    public void setEvidenceTag(String evidenceTag) {
        this.evidenceTag = evidenceTag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getCurrentCustodianId() {
        return currentCustodianId;
    }

    public void setCurrentCustodianId(String currentCustodianId) {
        this.currentCustodianId = currentCustodianId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLegalHold() {
        return legalHold;
    }

    public void setLegalHold(boolean legalHold) {
        this.legalHold = legalHold;
    }

    public String getIntegrityStatus() {
        return integrityStatus;
    }

    public void setIntegrityStatus(String integrityStatus) {
        this.integrityStatus = integrityStatus;
    }

    public Date getRetentionDate() {
        return retentionDate;
    }

    public void setRetentionDate(Date retentionDate) {
        this.retentionDate = retentionDate;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    // Compatibility getters/setters for legacy UI
    public String getNome() { return evidenceTag + " - " + (originalFilename != null ? originalFilename : description); }
    public String getFile() { return filePath; }
    public void setFile(String file) { this.filePath = file; }
    public String getStato() { return status; }
    public void setStato(String stato) { this.status = stato; }
    public String getFormato() { return fileType; }
    public void setFormato(String formato) { this.fileType = formato; }
    public Date getDataRilascio() { return uploadedAt != null ? new Date(uploadedAt.getTime()) : null; }
    public Date getDataScadenza() { return retentionDate; }
    public void setDataScadenza(Date d) { this.retentionDate = d; }
}
