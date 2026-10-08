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
// Table SQL associée à l'entité : table « maladie_cronique », schéma « dossier_schema »
@Table(name = "maladie_cronique", schema = "dossier_schema")
// Déclaration de la classe `MaladieCronique`
public class MaladieCronique {

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

    // Colonne SQL associée à l'attribut : nom « nom_maladie », obligatoire (NOT NULL), longueur max 200
    @Column(name = "nom_maladie", length = 200, nullable = false)
    // Attribut `nomMaladie` de type chaîne de caractères [privée]
    private String nomMaladie;

    // Colonne SQL associée à l'attribut : nom « code_cim10 », longueur max 10
    @Column(name = "code_cim10", length = 10)
    // Attribut `codeCim10` de type chaîne de caractères [privée]
    private String codeCim10;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `description` (description) de type chaîne de caractères [privée]
    private String description;

    // Colonne SQL associée à l'attribut : nom « date_diagnostic »
    @Column(name = "date_diagnostic")
    // Attribut `dateDiagnostic` de type date [privée]
    private LocalDate dateDiagnostic;

    // Déclaration de l'énumération `StatutDiagnostic`
    public enum StatutDiagnostic { A_CONFIRMER, VALIDE_MEDICALEMENT, INFIRME, EN_REMISSION, ARCHIVE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut_diagnostic », obligatoire (NOT NULL), longueur max 25
    @Column(name = "statut_diagnostic", length = 25, nullable = false)
    // Attribut `statutDiagnostic` de type StatutDiagnostic [privée] ; valeur initiale : la valeur de StatutDiagnostic.A_CONFIRMER
    private StatutDiagnostic statutDiagnostic = StatutDiagnostic.A_CONFIRMER;

    // Colonne SQL associée à l'attribut : nom « medecin_diagnostiqueur_id »
    @Column(name = "medecin_diagnostiqueur_id")
    // Attribut `medecinDiagnostiqueurId` de type identifiant UUID [privée]
    private UUID medecinDiagnostiqueurId;

    // Colonne SQL associée à l'attribut : nom « archive », obligatoire (NOT NULL)
    @Column(name = "archive", nullable = false)
    // Attribut `archive` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean archive = false;

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

    // Constructeur de `MaladieCronique` sans paramètre
    public MaladieCronique() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nomMaladie`
    public String getNomMaladie() { return nomMaladie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nomMaladie`
    public void setNomMaladie(String nomMaladie) { this.nomMaladie = nomMaladie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `codeCim10`
    public String getCodeCim10() { return codeCim10; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `codeCim10`
    public void setCodeCim10(String codeCim10) { this.codeCim10 = codeCim10; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `description`
    public String getDescription() { return description; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `description`
    public void setDescription(String description) { this.description = description; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateDiagnostic`
    public LocalDate getDateDiagnostic() { return dateDiagnostic; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateDiagnostic`
    public void setDateDiagnostic(LocalDate dateDiagnostic) { this.dateDiagnostic = dateDiagnostic; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statutDiagnostic`
    public StatutDiagnostic getStatutDiagnostic() { return statutDiagnostic; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statutDiagnostic`
    public void setStatutDiagnostic(StatutDiagnostic statutDiagnostic) { this.statutDiagnostic = statutDiagnostic; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinDiagnostiqueurId`
    public UUID getMedecinDiagnostiqueurId() { return medecinDiagnostiqueurId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinDiagnostiqueurId`
    public void setMedecinDiagnostiqueurId(UUID medecinDiagnostiqueurId) { this.medecinDiagnostiqueurId = medecinDiagnostiqueurId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `archive`
    public Boolean getArchive() { return archive; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `archive`
    public void setArchive(Boolean archive) { this.archive = archive; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
