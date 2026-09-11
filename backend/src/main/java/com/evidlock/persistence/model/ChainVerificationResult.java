package com.evidlock.persistence.model;

import java.util.List;

public class ChainVerificationResult {
    private Long evidenceItemId;
    private String evidenceTag;
    private boolean valid;
    private String statusMessage;
    private int totalEvents;
    private Long brokenAtEventId;
    private String brokenReason;
    private List<CustodyEvent> events;

    public ChainVerificationResult() {}

    public ChainVerificationResult(Long evidenceItemId, String evidenceTag, boolean valid, String statusMessage, int totalEvents) {
        this.evidenceItemId = evidenceItemId;
        this.evidenceTag = evidenceTag;
        this.valid = valid;
        this.statusMessage = statusMessage;
        this.totalEvents = totalEvents;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public Long getBrokenAtEventId() {
        return brokenAtEventId;
    }

    public void setBrokenAtEventId(Long brokenAtEventId) {
        this.brokenAtEventId = brokenAtEventId;
    }

    public String getBrokenReason() {
        return brokenReason;
    }

    public void setBrokenReason(String brokenReason) {
        this.brokenReason = brokenReason;
    }

    public List<CustodyEvent> getEvents() {
        return events;
    }

    public void setEvents(List<CustodyEvent> events) {
        this.events = events;
    }
}
