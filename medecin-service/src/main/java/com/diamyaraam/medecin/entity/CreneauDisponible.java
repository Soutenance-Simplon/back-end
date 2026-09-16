package com.diamyaraam.medecin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Créneaux concrets de consultation (RM096)
 */
@Entity
@Table(name = "creneau_disponible", schema = "medecin_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CreneauDisponible {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disponibilite_id", nullable = true)
    @JsonIgnore
    private DisponibiliteMedecin disponibilite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    @JsonIgnore
    private Medecin medecin;

    @Column(name = "date_heure_debut", nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(name = "date_heure_fin", nullable = false)
    private LocalDateTime dateHeureFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation", length = 20, nullable = false)
    private DisponibiliteMedecin.TypeConsultation typeConsultation;

    public enum StatutCreneau {
        DISPONIBLE, RESERVE, BLOQUE, PASSE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutCreneau statut = StatutCreneau.DISPONIBLE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public CreneauDisponible() {}

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DisponibiliteMedecin getDisponibilite() { return disponibilite; }
    public void setDisponibilite(DisponibiliteMedecin disponibilite) { this.disponibilite = disponibilite; }

    @JsonProperty("disponibiliteId")
    public UUID getDisponibiliteId() {
        return disponibilite != null ? disponibilite.getId() : null;
    }

    public Medecin getMedecin() { return medecin; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    @JsonProperty("medecinId")
    public UUID getMedecinId() {
        return medecin != null ? medecin.getId() : null;
    }

    public LocalDateTime getDateHeureDebut() { return dateHeureDebut; }
    public void setDateHeureDebut(LocalDateTime dateHeureDebut) { this.dateHeureDebut = dateHeureDebut; }

    public LocalDateTime getDateHeureFin() { return dateHeureFin; }
    public void setDateHeureFin(LocalDateTime dateHeureFin) { this.dateHeureFin = dateHeureFin; }

    public DisponibiliteMedecin.TypeConsultation getTypeConsultation() { return typeConsultation; }
    public void setTypeConsultation(DisponibiliteMedecin.TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    public StatutCreneau getStatut() { return statut; }
    public void setStatut(StatutCreneau statut) { this.statut = statut; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
