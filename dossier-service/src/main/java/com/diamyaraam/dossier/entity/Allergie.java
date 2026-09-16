package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "allergie", schema = "dossier_schema")
public class Allergie {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    public enum TypeAllergie { MEDICAMENTEUSE, ALIMENTAIRE, ENVIRONNEMENTALE, AUTRE }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_allergie", length = 30, nullable = false)
    private TypeAllergie typeAllergie;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    public enum SourceAllergie { PATIENT, MEDECIN, EXAMEN_MEDICAL }

    @Enumerated(EnumType.STRING)
    @Column(name = "source_allergie", length = 20, nullable = false)
    private SourceAllergie sourceAllergie;

    public enum Severite { LEGERE, MODEREE, SEVERE }

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Severite severite = Severite.MODEREE;

    @Column(name = "est_critique", nullable = false)
    private Boolean estCritique = false;

    public enum StatutInfo { DECLARE_PATIENT, VALIDE_MEDECIN, MODIFIE_MEDECIN, ARCHIVE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutInfo statut = StatutInfo.DECLARE_PATIENT;

    // UUID reference to Medecin in medecin-service
    @Column(name = "medecin_validateur_id")
    private UUID medecinValidateurId;

    @Column(name = "date_validation")
    private LocalDate dateValidation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Allergie() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DossierMedical getDossierMedical() { return dossierMedical; }
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    public TypeAllergie getTypeAllergie() { return typeAllergie; }
    public void setTypeAllergie(TypeAllergie typeAllergie) { this.typeAllergie = typeAllergie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SourceAllergie getSourceAllergie() { return sourceAllergie; }
    public void setSourceAllergie(SourceAllergie sourceAllergie) { this.sourceAllergie = sourceAllergie; }

    public Severite getSeverite() { return severite; }
    public void setSeverite(Severite severite) { this.severite = severite; }

    public Boolean getEstCritique() { return estCritique; }
    public void setEstCritique(Boolean estCritique) { this.estCritique = estCritique; }

    public StatutInfo getStatut() { return statut; }
    public void setStatut(StatutInfo statut) { this.statut = statut; }

    public UUID getMedecinValidateurId() { return medecinValidateurId; }
    public void setMedecinValidateurId(UUID medecinValidateurId) { this.medecinValidateurId = medecinValidateurId; }

    public LocalDate getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDate dateValidation) { this.dateValidation = dateValidation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
