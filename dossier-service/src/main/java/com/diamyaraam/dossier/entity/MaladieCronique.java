package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "maladie_cronique", schema = "dossier_schema")
public class MaladieCronique {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    @Column(name = "nom_maladie", length = 200, nullable = false)
    private String nomMaladie;

    @Column(name = "code_cim10", length = 10)
    private String codeCim10;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_diagnostic")
    private LocalDate dateDiagnostic;

    public enum StatutDiagnostic { A_CONFIRMER, VALIDE_MEDICALEMENT, INFIRME, EN_REMISSION, ARCHIVE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_diagnostic", length = 25, nullable = false)
    private StatutDiagnostic statutDiagnostic = StatutDiagnostic.A_CONFIRMER;

    @Column(name = "medecin_diagnostiqueur_id")
    private UUID medecinDiagnostiqueurId;

    @Column(name = "archive", nullable = false)
    private Boolean archive = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public MaladieCronique() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DossierMedical getDossierMedical() { return dossierMedical; }
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    public String getNomMaladie() { return nomMaladie; }
    public void setNomMaladie(String nomMaladie) { this.nomMaladie = nomMaladie; }

    public String getCodeCim10() { return codeCim10; }
    public void setCodeCim10(String codeCim10) { this.codeCim10 = codeCim10; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateDiagnostic() { return dateDiagnostic; }
    public void setDateDiagnostic(LocalDate dateDiagnostic) { this.dateDiagnostic = dateDiagnostic; }

    public StatutDiagnostic getStatutDiagnostic() { return statutDiagnostic; }
    public void setStatutDiagnostic(StatutDiagnostic statutDiagnostic) { this.statutDiagnostic = statutDiagnostic; }

    public UUID getMedecinDiagnostiqueurId() { return medecinDiagnostiqueurId; }
    public void setMedecinDiagnostiqueurId(UUID medecinDiagnostiqueurId) { this.medecinDiagnostiqueurId = medecinDiagnostiqueurId; }

    public Boolean getArchive() { return archive; }
    public void setArchive(Boolean archive) { this.archive = archive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
