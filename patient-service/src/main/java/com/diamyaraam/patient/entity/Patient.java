package com.diamyaraam.patient.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patients_patient", schema = "patient_schema")
public class Patient {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // UUID reference to User in auth-service
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "contact_urgence_nom", length = 200)
    private String contactUrgenceNom;

    @Column(name = "contact_urgence_telephone", length = 20)
    private String contactUrgenceTelephone;

    @Column(name = "contact_urgence_lien", length = 100)
    private String contactUrgenceLien;

    @Column(length = 500)
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String region;

    @Column(name = "consent_analyse_ia", nullable = false)
    private Boolean consentAnalyseIa = false;

    @Column(name = "consent_partage_famille", nullable = false)
    private Boolean consentPartageFamille = false;

    @Column(name = "nfc_id", length = 100)
    private String nfcId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Patient() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getContactUrgenceNom() { return contactUrgenceNom; }
    public void setContactUrgenceNom(String contactUrgenceNom) { this.contactUrgenceNom = contactUrgenceNom; }

    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) { this.contactUrgenceTelephone = contactUrgenceTelephone; }

    public String getContactUrgenceLien() { return contactUrgenceLien; }
    public void setContactUrgenceLien(String contactUrgenceLien) { this.contactUrgenceLien = contactUrgenceLien; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Boolean getConsentAnalyseIa() { return consentAnalyseIa; }
    public void setConsentAnalyseIa(Boolean consentAnalyseIa) { this.consentAnalyseIa = consentAnalyseIa; }

    public Boolean getConsentPartageFamille() { return consentPartageFamille; }
    public void setConsentPartageFamille(Boolean consentPartageFamille) { this.consentPartageFamille = consentPartageFamille; }

    public String getNfcId() { return nfcId; }
    public void setNfcId(String nfcId) { this.nfcId = nfcId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
