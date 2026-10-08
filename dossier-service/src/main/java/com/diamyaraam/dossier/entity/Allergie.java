// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

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
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « allergie », schéma « dossier_schema »
@Table(name = "allergie", schema = "dossier_schema")
// Déclaration de la classe `Allergie`
public class Allergie {

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

    // Déclaration de l'énumération `TypeAllergie`
    public enum TypeAllergie { MEDICAMENTEUSE, ALIMENTAIRE, ENVIRONNEMENTALE, AUTRE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_allergie », obligatoire (NOT NULL), longueur max 30
    @Column(name = "type_allergie", length = 30, nullable = false)
    // Attribut `typeAllergie` de type TypeAllergie [privée]
    private TypeAllergie typeAllergie;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 200
    @Column(nullable = false, length = 200)
    // Attribut `nom` (nom) de type chaîne de caractères [privée]
    private String nom;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `description` (description) de type chaîne de caractères [privée]
    private String description;

    // Déclaration de l'énumération `SourceAllergie`
    public enum SourceAllergie { PATIENT, MEDECIN, EXAMEN_MEDICAL }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « source_allergie », obligatoire (NOT NULL), longueur max 20
    @Column(name = "source_allergie", length = 20, nullable = false)
    // Attribut `sourceAllergie` de type SourceAllergie [privée]
    private SourceAllergie sourceAllergie;

    // Déclaration de l'énumération `Severite`
    public enum Severite { LEGERE, MODEREE, SEVERE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 10
    @Column(length = 10, nullable = false)
    // Attribut `severite` de type Severite [privée] ; valeur initiale : la valeur de Severite.MODEREE
    private Severite severite = Severite.MODEREE;

    // Colonne SQL associée à l'attribut : nom « est_critique », obligatoire (NOT NULL)
    @Column(name = "est_critique", nullable = false)
    // Attribut `estCritique` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean estCritique = false;

    // Déclaration de l'énumération `StatutInfo`
    public enum StatutInfo { DECLARE_PATIENT, VALIDE_MEDECIN, MODIFIE_MEDECIN, ARCHIVE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 20
    @Column(name = "statut", length = 20, nullable = false)
    // Attribut `statut` (statut) de type StatutInfo [privée] ; valeur initiale : la valeur de StatutInfo.DECLARE_PATIENT
    private StatutInfo statut = StatutInfo.DECLARE_PATIENT;

    // UUID reference to Medecin in medecin-service
    @Column(name = "medecin_validateur_id")
    // Attribut `medecinValidateurId` de type identifiant UUID [privée]
    private UUID medecinValidateurId;

    // Colonne SQL associée à l'attribut : nom « date_validation »
    @Column(name = "date_validation")
    // Attribut `dateValidation` de type date [privée]
    private LocalDate dateValidation;

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

    // Constructeur de `Allergie` sans paramètre
    public Allergie() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeAllergie`
    public TypeAllergie getTypeAllergie() { return typeAllergie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeAllergie`
    public void setTypeAllergie(TypeAllergie typeAllergie) { this.typeAllergie = typeAllergie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nom`
    public String getNom() { return nom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nom`
    public void setNom(String nom) { this.nom = nom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `description`
    public String getDescription() { return description; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `description`
    public void setDescription(String description) { this.description = description; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `sourceAllergie`
    public SourceAllergie getSourceAllergie() { return sourceAllergie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `sourceAllergie`
    public void setSourceAllergie(SourceAllergie sourceAllergie) { this.sourceAllergie = sourceAllergie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `severite`
    public Severite getSeverite() { return severite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `severite`
    public void setSeverite(Severite severite) { this.severite = severite; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `estCritique`
    public Boolean getEstCritique() { return estCritique; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `estCritique`
    public void setEstCritique(Boolean estCritique) { this.estCritique = estCritique; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutInfo getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutInfo statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinValidateurId`
    public UUID getMedecinValidateurId() { return medecinValidateurId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinValidateurId`
    public void setMedecinValidateurId(UUID medecinValidateurId) { this.medecinValidateurId = medecinValidateurId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateValidation`
    public LocalDate getDateValidation() { return dateValidation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateValidation`
    public void setDateValidation(LocalDate dateValidation) { this.dateValidation = dateValidation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
