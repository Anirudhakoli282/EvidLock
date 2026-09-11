package com.evidlock.persistence.model;

public class TransferStepUpDto {
    private Long requestId;
    private Long evidenceItemId;
    private String toCustodianId;
    private String reason;
    private String password; // Step-up confirmation password
    private String token; // Optional short-lived step-up token

    public TransferStepUpDto() {}

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getEvidenceItemId() {
        return evidenceItemId;
    }

    public void setEvidenceItemId(Long evidenceItemId) {
        this.evidenceItemId = evidenceItemId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
