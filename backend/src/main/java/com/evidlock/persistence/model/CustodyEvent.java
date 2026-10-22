package com.evidlock.persistence.model;

import java.sql.Timestamp;

public class CustodyEvent {
    private Long id;
    private Long evidenceItemId;
    private String actorId;
    private String action; // 'UPLOADED', 'VIEWED', 'DOWNLOADED', 'TRANSFERRED', 'ANALYZED', 'RELEASED', 'DISPOSED', 'INTEGRITY_ALERT', 'REMOVAL_REQUESTED', 'REMOVAL_APPROVED', 'REMOVAL_REJECTED', 'EVIDENCE_REMOVED', 'REPORT_EXPORTED'
    private Timestamp timestamp;
    private String fromCustodianId;
    private String toCustodianId;
    private String notes;
    private String previousEventHash;
    private String eventHash;

    public CustodyEvent() {}

    public CustodyEvent(Long id, Long evidenceItemId, String actorId, String action, Timestamp timestamp,
                        String fromCustodianId, String toCustodianId, String notes,
                        String previousEventHash, String eventHash) {
        this.id = id;
        this.evidenceItemId = evidenceItemId;
        this.actorId = actorId;
        this.action = action;
        this.timestamp = timestamp;
        this.fromCustodianId = fromCustodianId;
        this.toCustodianId = toCustodianId;
        this.notes = notes;
        this.previousEventHash = previousEventHash;
        this.eventHash = eventHash;
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

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPreviousEventHash() {
        return previousEventHash;
    }

    public void setPreviousEventHash(String previousEventHash) {
        this.previousEventHash = previousEventHash;
    }

    public String getEventHash() {
        return eventHash;
    }

    public void setEventHash(String eventHash) {
        this.eventHash = eventHash;
    }
}
