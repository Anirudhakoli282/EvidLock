package com.evidlock.persistence.model;

import java.sql.Timestamp;

public class CustodyTransferRequest {
    private Long id;
    private Long evidenceItemId;
    private String evidenceTag;
    private String evidenceDescription;
    private String fromCustodianId;
    private String toCustodianId;
    private String reason;
    private String status; // 'PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED'
    private Timestamp releasedAt;
    private Timestamp acceptedAt;
    private Timestamp createdAt;

    public CustodyTransferRequest() {
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvidenceItemId() {
        return evidenceItemId;
    }

    public void setEvidenceItemId(Long evidenceItemId) {
        this.evidenceItemId = evidenceItemId;
    }

    public String getEvidenceTag() {
        return evidenceTag;
    }

    public void setEvidenceTag(String evidenceTag) {
        this.evidenceTag = evidenceTag;
    }

    public String getEvidenceDescription() {
        return evidenceDescription;
    }

    public void setEvidenceDescription(String evidenceDescription) {
        this.evidenceDescription = evidenceDescription;
    }

    public String getFromCustodianId() {
        return fromCustodianId;
    }

    public void setFromCustodianId(String fromCustodianId) {
        this.fromCustodianId = fromCustodianId;
    }

    public String getToCustodianId() {
        return toCustodianId;
    }

    public void setToCustodianId(String toCustodianId) {
        this.toCustodianId = toCustodianId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Timestamp releasedAt) {
        this.releasedAt = releasedAt;
    }

    public Timestamp getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Timestamp acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
