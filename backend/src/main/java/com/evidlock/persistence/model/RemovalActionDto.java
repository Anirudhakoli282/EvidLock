package com.evidlock.persistence.model;

public class RemovalActionDto {
    private String reason;
    private String password;
    private String legalRef;

    public RemovalActionDto() {}

    public RemovalActionDto(String reason, String password, String legalRef) {
        this.reason = reason;
        this.password = password;
        this.legalRef = legalRef;
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

    public String getLegalRef() {
        return legalRef;
    }

    public void setLegalRef(String legalRef) {
        this.legalRef = legalRef;
    }
}
