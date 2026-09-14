package com.diamyaraam.rdv.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "salle_teleconsultation", schema = "rdv_schema")
public class SalleTeleconsultation {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id", unique = true, nullable = false)
    private RendezVous rendezVous;

    @Column(name = "token_patient", unique = true, nullable = false, length = 100)
    private String tokenPatient;

    @Column(name = "token_medecin", unique = true, nullable = false, length = 100)
    private String tokenMedecin;

    public enum StatutSalle { EN_ATTENTE, ACTIVE, TERMINEE, EXPIREE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutSalle statut = StatutSalle.EN_ATTENTE;

    @Column(name = "patient_connecte", nullable = false)
    private Boolean patientConnecte = false;

    @Column(name = "medecin_connecte", nullable = false)
    private Boolean medecinConnecte = false;

    @Column(name = "url_salle", length = 500)
    private String urlSalle;

    @Column(name = "tokens_expire_at", nullable = false)
    private LocalDateTime tokensExpireAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public SalleTeleconsultation() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RendezVous getRendezVous() { return rendezVous; }
    public void setRendezVous(RendezVous rendezVous) { this.rendezVous = rendezVous; }

    public String getTokenPatient() { return tokenPatient; }
    public void setTokenPatient(String tokenPatient) { this.tokenPatient = tokenPatient; }

    public String getTokenMedecin() { return tokenMedecin; }
    public void setTokenMedecin(String tokenMedecin) { this.tokenMedecin = tokenMedecin; }

    public StatutSalle getStatut() { return statut; }
    public void setStatut(StatutSalle statut) { this.statut = statut; }

    public Boolean getPatientConnecte() { return patientConnecte; }
    public void setPatientConnecte(Boolean patientConnecte) { this.patientConnecte = patientConnecte; }

    public Boolean getMedecinConnecte() { return medecinConnecte; }
    public void setMedecinConnecte(Boolean medecinConnecte) { this.medecinConnecte = medecinConnecte; }

    public String getUrlSalle() { return urlSalle; }
    public void setUrlSalle(String urlSalle) { this.urlSalle = urlSalle; }

    public LocalDateTime getTokensExpireAt() { return tokensExpireAt; }
    public void setTokensExpireAt(LocalDateTime tokensExpireAt) { this.tokensExpireAt = tokensExpireAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
