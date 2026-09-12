package com.diamyaraam.medecin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITÉ : Medecin — medecin-service
 *
 * Lien par userId (UUID) vers le compte dans auth-service.
 */
@Entity
@Table(name = "medecin", schema = "medecin_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Medecin {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // Lien par UUID vers auth-service (pas de FK cross-service)
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // Lien vers la base officielle ONMS
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onms_reference_id", unique = true, nullable = false)
    private OnmsReference onmsReference;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "photo_professionnelle")
    private String photoProfessionnelle;

    @Column(columnDefinition = "TEXT")
    private String biographie;

    @Column(length = 200)
    private String langues;

    @Column(name = "teleconsultation_active", nullable = false)
    private Boolean teleconsultationActive = true;

    @Column(name = "tarif_consultation", precision = 10, scale = 2)
    private BigDecimal tarifConsultation;

    @Column(name = "duree_consultation_minutes", nullable = false)
    private Integer dureeConsultationMinutes = 30;

    public enum StatutMedecin { ACTIF, SUSPENDU, EN_ATTENTE_VERIFICATION }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_medecin", length = 30, nullable = false)
    private StatutMedecin statutMedecin = StatutMedecin.EN_ATTENTE_VERIFICATION;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Medecin() {}

    public boolean peutExercer() {
        return StatutMedecin.ACTIF.equals(this.statutMedecin) && Boolean.TRUE.equals(this.isVerified);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public OnmsReference getOnmsReference() { return onmsReference; }
    public void setOnmsReference(OnmsReference onmsReference) { this.onmsReference = onmsReference; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getPhotoProfessionnelle() { return photoProfessionnelle; }
    public void setPhotoProfessionnelle(String photoProfessionnelle) { this.photoProfessionnelle = photoProfessionnelle; }

    public String getBiographie() { return biographie; }
    public void setBiographie(String biographie) { this.biographie = biographie; }

    public String getLangues() { return langues; }
    public void setLangues(String langues) { this.langues = langues; }

    public Boolean getTeleconsultationActive() { return teleconsultationActive; }
    public void setTeleconsultationActive(Boolean teleconsultationActive) { this.teleconsultationActive = teleconsultationActive; }

    public BigDecimal getTarifConsultation() { return tarifConsultation; }
    public void setTarifConsultation(BigDecimal tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    public Integer getDureeConsultationMinutes() { return dureeConsultationMinutes; }
    public void setDureeConsultationMinutes(Integer dureeConsultationMinutes) { this.dureeConsultationMinutes = dureeConsultationMinutes; }

    public StatutMedecin getStatutMedecin() { return statutMedecin; }
    public void setStatutMedecin(StatutMedecin statutMedecin) { this.statutMedecin = statutMedecin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
