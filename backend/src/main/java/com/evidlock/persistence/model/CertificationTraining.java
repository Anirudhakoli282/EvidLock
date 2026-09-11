package com.evidlock.persistence.model;

public class CertificationTraining {
    private Long id;
    private String title;
    private String description;
    private int durationHours;
    private String category;
    private String requiredRole;
    private String agencyCode;

    public CertificationTraining() {}

    public CertificationTraining(Long id, String title, String description, int durationHours, String category, String requiredRole, String agencyCode) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationHours = durationHours;
        this.category = category;
        this.requiredRole = requiredRole;
        this.agencyCode = agencyCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public String getAgencyCode() {
        return agencyCode;
    }

    public void setAgencyCode(String agencyCode) {
        this.agencyCode = agencyCode;
    }

    // Legacy compatibility getters/setters for Corso
    public String getNome() { return title; }
    public void setNome(String nome) { this.title = nome; }
    public int getDurata() { return durationHours; }
    public void setDurata(int durata) { this.durationHours = durata; }
    public String getCategoria() { return category; }
    public void setCategoria(String categoria) { this.category = categoria; }
    public String getAzienda() { return agencyCode; }
    public void setAzienda(String azienda) { this.agencyCode = azienda; }
    public double getPrezzo() { return 0.0; }
    public void setPrezzo(double p) {}
    public int getPosti() { return 50; }
    public void setPosti(int p) {}
    public int getPostiDisponibili() { return 50; }
    public void setPostiDisponibili(int p) {}
    public boolean isEsameFinale() { return true; }
    public void setEsameFinale(boolean e) {}
}
