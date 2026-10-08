// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

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

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « dossier_consultation », schéma « dossier_schema »
@Table(name = "dossier_consultation", schema = "dossier_schema")
// Déclaration de la classe `Consultation`
public class Consultation {

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

    // Colonne SQL associée à l'attribut : nom « medecin_id », obligatoire (NOT NULL)
    @Column(name = "medecin_id", nullable = false)
    // Attribut `medecinId` (identifiant du médecin) de type identifiant UUID [privée]
    private UUID medecinId;

    // Colonne SQL associée à l'attribut : nom « date_consultation », obligatoire (NOT NULL)
    @Column(name = "date_consultation", nullable = false)
    // Attribut `dateConsultation` de type date-heure [privée] ; valeur initiale : la date et l'heure courantes (LocalDateTime.now())
    private LocalDateTime dateConsultation = LocalDateTime.now();

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 200
    @Column(nullable = false, length = 200)
    // Attribut `motif` (motif) de type chaîne de caractères [privée]
    private String motif;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `diagnostic` de type chaîne de caractères [privée]
    private String diagnostic;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `examenClinique` de type chaîne de caractères [privée]
    private String examenClinique;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `traitementPropose` de type chaîne de caractères [privée]
    private String traitementPropose;

    // Colonne SQL associée à l'attribut : nom « teleconsultation », obligatoire (NOT NULL)
    @Column(name = "teleconsultation", nullable = false)
    // Attribut `teleconsultation` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean teleconsultation = false;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `Consultation` sans paramètre
    public Consultation() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinId`
    public UUID getMedecinId() { return medecinId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinId`
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateConsultation`
    public LocalDateTime getDateConsultation() { return dateConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateConsultation`
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `motif`
    public String getMotif() { return motif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `motif`
    public void setMotif(String motif) { this.motif = motif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `diagnostic`
    public String getDiagnostic() { return diagnostic; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `diagnostic`
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `examenClinique`
    public String getExamenClinique() { return examenClinique; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `examenClinique`
    public void setExamenClinique(String examenClinique) { this.examenClinique = examenClinique; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `traitementPropose`
    public String getTraitementPropose() { return traitementPropose; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `traitementPropose`
    public void setTraitementPropose(String traitementPropose) { this.traitementPropose = traitementPropose; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `teleconsultation`
    public Boolean getTeleconsultation() { return teleconsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `teleconsultation`
    public void setTeleconsultation(Boolean teleconsultation) { this.teleconsultation = teleconsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
