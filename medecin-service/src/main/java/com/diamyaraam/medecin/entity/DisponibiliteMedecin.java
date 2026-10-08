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
// Import de la classe `UpdateTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UpdateTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `LocalTime` (paquet java.time)
import java.time.LocalTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * Template de disponibilité hebdomadaire du médecin (RM095)
 */
@Entity
// Table SQL associée à l'entité : table « disponibilite_medecin », schéma « medecin_schema »
@Table(name = "disponibilite_medecin", schema = "medecin_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `DisponibiliteMedecin`
public class DisponibiliteMedecin {

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
    // Colonne de clé étrangère de la relation : colonne « medecin_id »
    @JoinColumn(name = "medecin_id", nullable = false)
    // Exclut ce champ de la sérialisation JSON
    @JsonIgnore
    // Attribut `medecin` de type Medecin [privée]
    private Medecin medecin;

    // Déclaration de l'énumération `JourSemaine`
    public enum JourSemaine {
        // Constante(s) de l'énumération : LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE
        LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI, SAMEDI, DIMANCHE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « jour_semaine », obligatoire (NOT NULL), longueur max 10
    @Column(name = "jour_semaine", length = 10, nullable = false)
    // Attribut `jourSemaine` de type JourSemaine [privée]
    private JourSemaine jourSemaine;

    // Colonne SQL associée à l'attribut : nom « heure_debut », obligatoire (NOT NULL)
    @Column(name = "heure_debut", nullable = false)
    // Attribut `heureDebut` de type heure [privée]
    private LocalTime heureDebut;

    // Colonne SQL associée à l'attribut : nom « heure_fin », obligatoire (NOT NULL)
    @Column(name = "heure_fin", nullable = false)
    // Attribut `heureFin` de type heure [privée]
    private LocalTime heureFin;

    // Colonne SQL associée à l'attribut : nom « duree_creneau_minutes », obligatoire (NOT NULL)
    @Column(name = "duree_creneau_minutes", nullable = false)
    // Attribut `dureeCreneauMinutes` de type entier [privée] ; valeur initiale : la valeur numérique 30
    private Integer dureeCreneauMinutes = 30;

    // Déclaration de l'énumération `TypeConsultation`
    public enum TypeConsultation {
        // Constante(s) de l'énumération : PRESENTIELLE, TELECONSULTATION, LES_DEUX
        PRESENTIELLE, TELECONSULTATION, LES_DEUX
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_consultation », obligatoire (NOT NULL), longueur max 20
    @Column(name = "type_consultation", length = 20, nullable = false)
    // Attribut `typeConsultation` de type TypeConsultation [privée] ; valeur initiale : la valeur de TypeConsultation.LES_DEUX
    private TypeConsultation typeConsultation = TypeConsultation.LES_DEUX;

    // Colonne SQL associée à l'attribut : nom « date_debut_validite »
    @Column(name = "date_debut_validite")
    // Attribut `dateDebutValidite` de type date [privée]
    private LocalDate dateDebutValidite;

    // Colonne SQL associée à l'attribut : nom « date_fin_validite »
    @Column(name = "date_fin_validite")
    // Attribut `dateFinValidite` de type date [privée]
    private LocalDate dateFinValidite;

    // Colonne SQL associée à l'attribut : nom « actif », obligatoire (NOT NULL)
    @Column(name = "actif", nullable = false)
    // Attribut `actif` de type booléen [privée] ; valeur initiale : le booléen vrai
    private Boolean actif = true;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Horodatage mis à jour automatiquement à chaque modification
    @UpdateTimestamp
    // Colonne SQL associée à l'attribut : nom « updated_at »
    @Column(name = "updated_at")
    // Attribut `updatedAt` (date de dernière modification) de type date-heure [privée]
    private LocalDateTime updatedAt;

    // Constructeur de `DisponibiliteMedecin` sans paramètre
    public DisponibiliteMedecin() {}

    // Getters / Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

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

    // Accesseur (getter) : renvoie la valeur de l'attribut `jourSemaine`
    public JourSemaine getJourSemaine() { return jourSemaine; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `jourSemaine`
    public void setJourSemaine(JourSemaine jourSemaine) { this.jourSemaine = jourSemaine; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `heureDebut`
    public LocalTime getHeureDebut() { return heureDebut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `heureDebut`
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `heureFin`
    public LocalTime getHeureFin() { return heureFin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `heureFin`
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dureeCreneauMinutes`
    public Integer getDureeCreneauMinutes() { return dureeCreneauMinutes; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dureeCreneauMinutes`
    public void setDureeCreneauMinutes(Integer dureeCreneauMinutes) { this.dureeCreneauMinutes = dureeCreneauMinutes; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeConsultation`
    public TypeConsultation getTypeConsultation() { return typeConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeConsultation`
    public void setTypeConsultation(TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateDebutValidite`
    public LocalDate getDateDebutValidite() { return dateDebutValidite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateDebutValidite`
    public void setDateDebutValidite(LocalDate dateDebutValidite) { this.dateDebutValidite = dateDebutValidite; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateFinValidite`
    public LocalDate getDateFinValidite() { return dateFinValidite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateFinValidite`
    public void setDateFinValidite(LocalDate dateFinValidite) { this.dateFinValidite = dateFinValidite; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `actif`
    public Boolean getActif() { return actif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `actif`
    public void setActif(Boolean actif) { this.actif = actif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
