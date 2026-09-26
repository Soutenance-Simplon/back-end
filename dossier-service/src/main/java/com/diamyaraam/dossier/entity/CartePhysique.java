package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "carte_physique", schema = "dossier_schema")
public class CartePhysique {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // Le numéro de série lisible par un humain imprimé sur la carte (ex: DY-123-456)
    @Column(name = "numero_serie", nullable = false, unique = true, length = 50)
    private String numeroSerie;

    // Le code caché dans le QR Code (le "jeton sécurisé").
    // C'est ce que l'agent scanne pour lier la carte.
    @Column(name = "qr_token_securise", nullable = false, unique = true, length = 255)
    private String qrTokenSecurise;

    // L'ID du patient auquel la carte est liée.
    // Il est NULL au début quand la carte est juste imprimée et stockée en agence.
    @Column(name = "patient_id")
    private UUID patientId;

    public enum StatutCarte {
        EN_STOCK,     // Imprimée mais pas encore vendue
        ACTIVE,       // Vendue et liée à un patient
        PERDUE,       // Le patient a déclaré la perte de la carte
        DESACTIVEE    // La carte a été remplacée
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_carte", length = 20, nullable = false)
    private StatutCarte statut = StatutCarte.EN_STOCK;

    // La date à laquelle la carte a été liée à un patient
    @Column(name = "date_activation")
    private LocalDateTime dateActivation;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CartePhysique() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getQrTokenSecurise() { return qrTokenSecurise; }
    public void setQrTokenSecurise(String qrTokenSecurise) { this.qrTokenSecurise = qrTokenSecurise; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public StatutCarte getStatut() { return statut; }
    public void setStatut(StatutCarte statut) { this.statut = statut; }

    public LocalDateTime getDateActivation() { return dateActivation; }
    public void setDateActivation(LocalDateTime dateActivation) { this.dateActivation = dateActivation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
