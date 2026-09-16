package com.diamyaraam.medecin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Template de disponibilité hebdomadaire du médecin (RM095)
 */
@Entity
@Table(name = "disponibilite_medecin", schema = "medecin_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DisponibiliteMedecin {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    @JsonIgnore
    private Medecin medecin;

    public enum JourSemaine {
        LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", length = 10, nullable = false)
    private JourSemaine jourSemaine;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "duree_creneau_minutes", nullable = false)
    private Integer dureeCreneauMinutes = 30;

    public enum TypeConsultation {
        PRESENTIELLE, TELECONSULTATION, LES_DEUX
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation", length = 20, nullable = false)
    private TypeConsultation typeConsultation = TypeConsultation.LES_DEUX;

    @Column(name = "date_debut_validite")
    private LocalDate dateDebutValidite;

    @Column(name = "date_fin_validite")
    private LocalDate dateFinValidite;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DisponibiliteMedecin() {}

    // Getters / Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Medecin getMedecin() { return medecin; }
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    @JsonProperty("medecinId")
    public UUID getMedecinId() {
        return medecin != null ? medecin.getId() : null;
    }

    public JourSemaine getJourSemaine() { return jourSemaine; }
    public void setJourSemaine(JourSemaine jourSemaine) { this.jourSemaine = jourSemaine; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public Integer getDureeCreneauMinutes() { return dureeCreneauMinutes; }
    public void setDureeCreneauMinutes(Integer dureeCreneauMinutes) { this.dureeCreneauMinutes = dureeCreneauMinutes; }

    public TypeConsultation getTypeConsultation() { return typeConsultation; }
    public void setTypeConsultation(TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    public LocalDate getDateDebutValidite() { return dateDebutValidite; }
    public void setDateDebutValidite(LocalDate dateDebutValidite) { this.dateDebutValidite = dateDebutValidite; }

    public LocalDate getDateFinValidite() { return dateFinValidite; }
    public void setDateFinValidite(LocalDate dateFinValidite) { this.dateFinValidite = dateFinValidite; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
