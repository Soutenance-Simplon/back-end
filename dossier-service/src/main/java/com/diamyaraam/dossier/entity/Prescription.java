package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescription", schema = "dossier_schema")
public class Prescription {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    @Column(name = "medecin_id", nullable = false)
    private UUID medecinId;

    @Column(name = "numero_prescription", unique = true, length = 50)
    private String numeroPrescription;

    public enum StatutPrescription { BROUILLON, ACTIVE, ANNULEE, REMPLACEE, EXPIREE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutPrescription statut = StatutPrescription.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String detailsMedicaments;

    @Column(name = "rapport_ia", columnDefinition = "TEXT")
    private String rapportIa;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Prescription() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DossierMedical getDossierMedical() { return dossierMedical; }
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    public UUID getMedecinId() { return medecinId; }
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    public String getNumeroPrescription() { return numeroPrescription; }
    public void setNumeroPrescription(String numeroPrescription) { this.numeroPrescription = numeroPrescription; }

    public StatutPrescription getStatut() { return statut; }
    public void setStatut(StatutPrescription statut) { this.statut = statut; }

    public String getDetailsMedicaments() { return detailsMedicaments; }
    public void setDetailsMedicaments(String detailsMedicaments) { this.detailsMedicaments = detailsMedicaments; }

    public String getRapportIa() { return rapportIa; }
    public void setRapportIa(String rapportIa) { this.rapportIa = rapportIa; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
