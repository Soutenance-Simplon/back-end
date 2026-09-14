package com.diamyaraam.rdv.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rendez_vous", schema = "rdv_schema")
public class RendezVous {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // UUID references to other services
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "medecin_id", nullable = false)
    private UUID medecinId;

    @Column(name = "dossier_medical_id")
    private UUID dossierMedicalId;

    @Column(name = "creneau_id")
    private UUID creneauId;

    @Column(name = "date_heure_souhaitee", nullable = false)
    private LocalDateTime dateHeureSouhaitee;

    @Column(name = "date_heure_confirmee")
    private LocalDateTime dateHeureConfirmee;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    @Column(name = "est_urgent", nullable = false)
    private Boolean estUrgent = false;

    public enum TypeConsultation { PRESENTIELLE, TELECONSULTATION, DOMICILE }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation", length = 20, nullable = false)
    private TypeConsultation typeConsultation = TypeConsultation.TELECONSULTATION;


    public enum StatutRendezVous {
        EN_ATTENTE, ACCEPTE, CONFIRME, EN_COURS, TERMINE, ANNULE, ABSENT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;

    @Column(name = "tarif_applique", precision = 10, scale = 2)
    private BigDecimal tarifApplique;

    @Column(name = "paiement_valide", nullable = false)
    private Boolean paiementValide = false;

    @Column(name = "reference_paiement", length = 100)
    private String referencePaiement;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public RendezVous() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getMedecinId() { return medecinId; }
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    public UUID getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(UUID dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    public UUID getCreneauId() { return creneauId; }
    public void setCreneauId(UUID creneauId) { this.creneauId = creneauId; }

    public LocalDateTime getDateHeureSouhaitee() { return dateHeureSouhaitee; }
    public void setDateHeureSouhaitee(LocalDateTime dateHeureSouhaitee) { this.dateHeureSouhaitee = dateHeureSouhaitee; }

    public LocalDateTime getDateHeureConfirmee() { return dateHeureConfirmee; }
    public void setDateHeureConfirmee(LocalDateTime dateHeureConfirmee) { this.dateHeureConfirmee = dateHeureConfirmee; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Boolean getEstUrgent() { return estUrgent; }
    public void setEstUrgent(Boolean estUrgent) { this.estUrgent = estUrgent; }

    public TypeConsultation getTypeConsultation() { return typeConsultation; }
    public void setTypeConsultation(TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    public StatutRendezVous getStatut() { return statut; }
    public void setStatut(StatutRendezVous statut) { this.statut = statut; }

    public BigDecimal getTarifApplique() { return tarifApplique; }
    public void setTarifApplique(BigDecimal tarifApplique) { this.tarifApplique = tarifApplique; }

    public Boolean getPaiementValide() { return paiementValide; }
    public void setPaiementValide(Boolean paiementValide) { this.paiementValide = paiementValide; }

    public String getReferencePaiement() { return referencePaiement; }
    public void setReferencePaiement(String referencePaiement) { this.referencePaiement = referencePaiement; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
