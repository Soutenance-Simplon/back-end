// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « dossier_vaccination », schéma « dossier_schema »
@Table(name = "dossier_vaccination", schema = "dossier_schema")
// Déclaration de la classe `Vaccination`
public class Vaccination {

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
    // Colonne de clé étrangère de la relation : colonne « dossier_medical_id »
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    // Attribut `dossierMedical` de type DossierMedical [privée]
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierMedical dossierMedical;

    // Colonne SQL associée à l'attribut : nom « nom_vaccin », obligatoire (NOT NULL), longueur max 150
    @Column(name = "nom_vaccin", nullable = false, length = 150)
    // Attribut `nomVaccin` de type chaîne de caractères [privée]
    private String nomVaccin;

    // Colonne SQL associée à l'attribut : nom « maladie_cible », longueur max 150
    @Column(name = "maladie_cible", length = 150)
    // Attribut `maladieCible` de type chaîne de caractères [privée]
    private String maladieCible;

    // Colonne SQL associée à l'attribut : nom « date_injection », obligatoire (NOT NULL)
    @Column(name = "date_injection", nullable = false)
    // Attribut `dateInjection` de type date [privée]
    private LocalDate dateInjection;

    // Colonne SQL associée à l'attribut : nom « date_rappel »
    @Column(name = "date_rappel")
    // Attribut `dateRappel` de type date [privée]
    private LocalDate dateRappel;

    // Colonne SQL associée à l'attribut : nom « numero_lot », longueur max 50
    @Column(name = "numero_lot", length = 50)
    // Attribut `numeroLot` de type chaîne de caractères [privée]
    private String numeroLot;

    // Colonne SQL associée à l'attribut : nom « centre_vaccination », longueur max 200
    @Column(name = "centre_vaccination", length = 200)
    // Attribut `centreVaccination` de type chaîne de caractères [privée]
    private String centreVaccination;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `Vaccination` sans paramètre
    public Vaccination() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nomVaccin`
    public String getNomVaccin() { return nomVaccin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nomVaccin`
    public void setNomVaccin(String nomVaccin) { this.nomVaccin = nomVaccin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `maladieCible`
    public String getMaladieCible() { return maladieCible; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `maladieCible`
    public void setMaladieCible(String maladieCible) { this.maladieCible = maladieCible; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateInjection`
    public LocalDate getDateInjection() { return dateInjection; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateInjection`
    public void setDateInjection(LocalDate dateInjection) { this.dateInjection = dateInjection; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateRappel`
    public LocalDate getDateRappel() { return dateRappel; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateRappel`
    public void setDateRappel(LocalDate dateRappel) { this.dateRappel = dateRappel; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `numeroLot`
    public String getNumeroLot() { return numeroLot; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `numeroLot`
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `centreVaccination`
    public String getCentreVaccination() { return centreVaccination; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `centreVaccination`
    public void setCentreVaccination(String centreVaccination) { this.centreVaccination = centreVaccination; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
