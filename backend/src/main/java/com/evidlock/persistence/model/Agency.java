package com.evidlock.persistence.model;

public class Agency {
    private String agencyCode;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String badgePrefix;
    private String logo;

    public Agency() {}

    public Agency(String agencyCode, String name, String email, String phone, String address, String badgePrefix, String logo) {
        this.agencyCode = agencyCode;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.badgePrefix = badgePrefix;
        this.logo = logo;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBadgePrefix() {
        return badgePrefix;
    }

    public void setBadgePrefix(String badgePrefix) {
        this.badgePrefix = badgePrefix;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    // Compatibility aliases for legacy frontend if needed
    public String getPIva() { return agencyCode; }
    public void setPIva(String pIva) { this.agencyCode = pIva; }
    public String getRagioneSociale() { return name; }
    public void setRagioneSociale(String name) { this.name = name; }
    public String getTelefono() { return phone; }
    public void setTelefono(String phone) { this.phone = phone; }
    public String getIndirizzo() { return address; }
    public void setIndirizzo(String address) { this.address = address; }
    public String getImmagine() { return logo; }
    public void setImmagine(String logo) { this.logo = logo; }
}
