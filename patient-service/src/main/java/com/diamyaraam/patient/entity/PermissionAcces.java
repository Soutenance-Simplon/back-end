package com.diamyaraam.patient.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "permission_acces", schema = "patient_schema")
public class PermissionAcces {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "dossier_medical_id", nullable = false)
    private UUID dossierMedicalId;

    @Column(name = "utilisateur_autorise_id", nullable = false)
    private UUID utilisateurAutoriseId;

    public enum TypeAcces {
        LECTURE_TOTALE, LECTURE_PARTIELLE, URGENCE_UNIQUEMENT, FAMILLE_LECTURE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_acces", length = 30, nullable = false)
    private TypeAcces typeAcces;

    public enum LienFamilial {
        CONJOINT, PARENT, ENFANT, FRERE_SOEUR, TUTEUR_LEGAL, AUTRE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "lien_familial", length = 20)
    private LienFamilial lienFamilial;

    @Column(name = "sections_autorisees", columnDefinition = "TEXT")
    private String sectionsAutorisees;

    @Column(name = "date_expiration")
    private LocalDateTime dateExpiration;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Column(length = 300)
    private String motif;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PermissionAcces() {}

    public boolean isValide() {
        if (!Boolean.TRUE.equals(this.actif)) return false;
        if (this.dateExpiration == null) return true;
        return LocalDateTime.now().isBefore(this.dateExpiration);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(UUID dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    public UUID getUtilisateurAutoriseId() { return utilisateurAutoriseId; }
    public void setUtilisateurAutoriseId(UUID utilisateurAutoriseId) { this.utilisateurAutoriseId = utilisateurAutoriseId; }

    public TypeAcces getTypeAcces() { return typeAcces; }
    public void setTypeAcces(TypeAcces typeAcces) { this.typeAcces = typeAcces; }

    public LienFamilial getLienFamilial() { return lienFamilial; }
    public void setLienFamilial(LienFamilial lienFamilial) { this.lienFamilial = lienFamilial; }

    public String getSectionsAutorisees() { return sectionsAutorisees; }
    public void setSectionsAutorisees(String sectionsAutorisees) { this.sectionsAutorisees = sectionsAutorisees; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
