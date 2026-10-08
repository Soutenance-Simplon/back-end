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
// Table SQL associée à l'entité : table « antecedent_medical », schéma « dossier_schema »
@Table(name = "antecedent_medical", schema = "dossier_schema")
// Déclaration de la classe `AntecedentMedical`
public class AntecedentMedical {

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

    // Déclaration de l'énumération `TypeAntecedent`
    public enum TypeAntecedent {
        // Constante(s) de l'énumération : MALADIE_ANCIENNE, CHIRURGIE, HOSPITALISATION, TRAUMATISME, TRAITEMENT_PASSE, ANTECEDENT_FAMILIAL, AUTRE
        MALADIE_ANCIENNE, CHIRURGIE, HOSPITALISATION, TRAUMATISME, TRAITEMENT_PASSE, ANTECEDENT_FAMILIAL, AUTRE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_antecedent », obligatoire (NOT NULL), longueur max 30
    @Column(name = "type_antecedent", length = 30, nullable = false)
    // Attribut `typeAntecedent` de type TypeAntecedent [privée]
    private TypeAntecedent typeAntecedent;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 300
    @Column(nullable = false, length = 300)
    // Attribut `titre` (titre) de type chaîne de caractères [privée]
    private String titre;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `description` (description) de type chaîne de caractères [privée]
    private String description;

    // Colonne SQL associée à l'attribut : nom « date_evenement »
    @Column(name = "date_evenement")
    // Attribut `dateEvenement` de type date [privée]
    private LocalDate dateEvenement;

    // Déclaration de l'énumération `StatutInfo`
    public enum StatutInfo { DECLARE_PATIENT, VALIDE_MEDECIN, MODIFIE_MEDECIN, ARCHIVE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 20
    @Column(name = "statut", length = 20, nullable = false)
    // Attribut `statut` (statut) de type StatutInfo [privée] ; valeur initiale : la valeur de StatutInfo.DECLARE_PATIENT
    private StatutInfo statut = StatutInfo.DECLARE_PATIENT;

    // Colonne SQL associée à l'attribut : nom « medecin_auteur_id »
    @Column(name = "medecin_auteur_id")
    // Attribut `medecinAuteurId` de type identifiant UUID [privée]
    private UUID medecinAuteurId;

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

    // Constructeur de `AntecedentMedical` sans paramètre
    public AntecedentMedical() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeAntecedent`
    public TypeAntecedent getTypeAntecedent() { return typeAntecedent; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeAntecedent`
    public void setTypeAntecedent(TypeAntecedent typeAntecedent) { this.typeAntecedent = typeAntecedent; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `titre`
    public String getTitre() { return titre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `titre`
    public void setTitre(String titre) { this.titre = titre; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `description`
    public String getDescription() { return description; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `description`
    public void setDescription(String description) { this.description = description; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateEvenement`
    public LocalDate getDateEvenement() { return dateEvenement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateEvenement`
    public void setDateEvenement(LocalDate dateEvenement) { this.dateEvenement = dateEvenement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutInfo getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutInfo statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinAuteurId`
    public UUID getMedecinAuteurId() { return medecinAuteurId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinAuteurId`
    public void setMedecinAuteurId(UUID medecinAuteurId) { this.medecinAuteurId = medecinAuteurId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
