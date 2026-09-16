package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "antecedent_medical", schema = "dossier_schema")
public class AntecedentMedical {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    public enum TypeAntecedent {
        MALADIE_ANCIENNE, CHIRURGIE, HOSPITALISATION, TRAUMATISME, TRAITEMENT_PASSE, ANTECEDENT_FAMILIAL, AUTRE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_antecedent", length = 30, nullable = false)
    private TypeAntecedent typeAntecedent;

    @Column(nullable = false, length = 300)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_evenement")
    private LocalDate dateEvenement;

    public enum StatutInfo { DECLARE_PATIENT, VALIDE_MEDECIN, MODIFIE_MEDECIN, ARCHIVE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutInfo statut = StatutInfo.DECLARE_PATIENT;

    @Column(name = "medecin_auteur_id")
    private UUID medecinAuteurId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AntecedentMedical() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DossierMedical getDossierMedical() { return dossierMedical; }
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    public TypeAntecedent getTypeAntecedent() { return typeAntecedent; }
    public void setTypeAntecedent(TypeAntecedent typeAntecedent) { this.typeAntecedent = typeAntecedent; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    public StatutInfo getStatut() { return statut; }
    public void setStatut(StatutInfo statut) { this.statut = statut; }

    public UUID getMedecinAuteurId() { return medecinAuteurId; }
    public void setMedecinAuteurId(UUID medecinAuteurId) { this.medecinAuteurId = medecinAuteurId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
