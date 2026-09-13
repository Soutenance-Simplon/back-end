package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dossier_medical", schema = "dossier_schema")
public class DossierMedical {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // UUID reference to Patient in patient-service
    @Column(name = "patient_id", nullable = false, unique = true)
    private UUID patientId;

    public enum GroupeSanguin {
        A_PLUS("A+"), A_MOINS("A-"),
        B_PLUS("B+"), B_MOINS("B-"),
        AB_PLUS("AB+"), AB_MOINS("AB-"),
        O_PLUS("O+"), O_MOINS("O-");

        private final String label;
        GroupeSanguin(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "groupe_sanguin", length = 10)
    private GroupeSanguin groupeSanguin;

    @Column(precision = 5)
    private Double taille;

    @Column(precision = 5)
    private Double poids;

    @Column(name = "groupe_sanguin_valide", nullable = false)
    private Boolean groupeSanguinValide = false;

    public enum StatutDossier { ACTIF, ARCHIVE, SUSPENDU }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_dossier", length = 15, nullable = false)
    private StatutDossier statutDossier = StatutDossier.ACTIF;

    @Column(name = "code_qr_securise", unique = true, length = 100)
    private String codeQrSecurise;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DossierMedical() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public GroupeSanguin getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(GroupeSanguin groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    public Double getTaille() { return taille; }
    public void setTaille(Double taille) { this.taille = taille; }

    public Double getPoids() { return poids; }
    public void setPoids(Double poids) { this.poids = poids; }

    public Boolean getGroupeSanguinValide() { return groupeSanguinValide; }
    public void setGroupeSanguinValide(Boolean groupeSanguinValide) { this.groupeSanguinValide = groupeSanguinValide; }

    public StatutDossier getStatutDossier() { return statutDossier; }
    public void setStatutDossier(StatutDossier statutDossier) { this.statutDossier = statutDossier; }

    public String getCodeQrSecurise() { return codeQrSecurise; }
    public void setCodeQrSecurise(String codeQrSecurise) { this.codeQrSecurise = codeQrSecurise; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
