// Déclaration du package Java : `com.diamyaraam.medecin.entity`
package com.diamyaraam.medecin.entity;

// Import de la classe `JsonIgnore` (paquet com.fasterxml.jackson.annotation)
import com.fasterxml.jackson.annotation.JsonIgnore;
// Import de la classe `JsonIgnoreProperties` (paquet com.fasterxml.jackson.annotation)
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// Import de la classe `JsonProperty` (paquet com.fasterxml.jackson.annotation)
import com.fasterxml.jackson.annotation.JsonProperty;
// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * Créneaux concrets de consultation (RM096)
 */
@Entity
// Table SQL associée à l'entité : table « creneau_disponible », schéma « medecin_schema »
@Table(name = "creneau_disponible", schema = "medecin_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `CreneauDisponible`
public class CreneauDisponible {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Relation plusieurs-à-un avec une autre entité
    @ManyToOne(fetch = FetchType.LAZY)
    // Colonne de clé étrangère de la relation : colonne « disponibilite_id »
    @JoinColumn(name = "disponibilite_id", nullable = true)
    // Exclut ce champ de la sérialisation JSON
    @JsonIgnore
    // Attribut `disponibilite` de type DisponibiliteMedecin [privée]
    private DisponibiliteMedecin disponibilite;

    // Relation plusieurs-à-un avec une autre entité
    @ManyToOne(fetch = FetchType.LAZY)
    // Colonne de clé étrangère de la relation : colonne « medecin_id »
    @JoinColumn(name = "medecin_id", nullable = false)
    // Exclut ce champ de la sérialisation JSON
    @JsonIgnore
    // Attribut `medecin` de type Medecin [privée]
    private Medecin medecin;

    // Colonne SQL associée à l'attribut : nom « date_heure_debut », obligatoire (NOT NULL)
    @Column(name = "date_heure_debut", nullable = false)
    // Attribut `dateHeureDebut` de type date-heure [privée]
    private LocalDateTime dateHeureDebut;

    // Colonne SQL associée à l'attribut : nom « date_heure_fin », obligatoire (NOT NULL)
    @Column(name = "date_heure_fin", nullable = false)
    // Attribut `dateHeureFin` de type date-heure [privée]
    private LocalDateTime dateHeureFin;

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_consultation », obligatoire (NOT NULL), longueur max 20
    @Column(name = "type_consultation", length = 20, nullable = false)
    // Attribut `typeConsultation` de type DisponibiliteMedecin.TypeConsultation [privée]
    private DisponibiliteMedecin.TypeConsultation typeConsultation;

    // Déclaration de l'énumération `StatutCreneau`
    public enum StatutCreneau {
        // Constante(s) de l'énumération : DISPONIBLE, RESERVE, BLOQUE, PASSE
        DISPONIBLE, RESERVE, BLOQUE, PASSE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutCreneau [privée] ; valeur initiale : la valeur de StatutCreneau.DISPONIBLE
    private StatutCreneau statut = StatutCreneau.DISPONIBLE;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `CreneauDisponible` sans paramètre
    public CreneauDisponible() {}

    // Getters / Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `disponibilite`
    public DisponibiliteMedecin getDisponibilite() { return disponibilite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `disponibilite`
    public void setDisponibilite(DisponibiliteMedecin disponibilite) { this.disponibilite = disponibilite; }

    // Nom de la propriété dans le JSON
    @JsonProperty("disponibiliteId")
    // Méthode `getDisponibiliteId` (publique) — sans paramètre ; retourne : identifiant UUID ; intention : récupère (get disponibilite id)
    public UUID getDisponibiliteId() {
        // Retourne `disponibilite != null ? disponibilite.getId() : null`
        return disponibilite != null ? disponibilite.getId() : null;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecin`
    public Medecin getMedecin() { return medecin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecin`
    public void setMedecin(Medecin medecin) { this.medecin = medecin; }

    // Nom de la propriété dans le JSON
    @JsonProperty("medecinId")
    // Méthode `getMedecinId` (publique) — sans paramètre ; retourne : identifiant UUID ; intention : récupère (get medecin id)
    public UUID getMedecinId() {
        // Retourne `medecin != null ? medecin.getId() : null`
        return medecin != null ? medecin.getId() : null;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateHeureDebut`
    public LocalDateTime getDateHeureDebut() { return dateHeureDebut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateHeureDebut`
    public void setDateHeureDebut(LocalDateTime dateHeureDebut) { this.dateHeureDebut = dateHeureDebut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateHeureFin`
    public LocalDateTime getDateHeureFin() { return dateHeureFin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateHeureFin`
    public void setDateHeureFin(LocalDateTime dateHeureFin) { this.dateHeureFin = dateHeureFin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeConsultation`
    public DisponibiliteMedecin.TypeConsultation getTypeConsultation() { return typeConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeConsultation`
    public void setTypeConsultation(DisponibiliteMedecin.TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutCreneau getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutCreneau statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
