// Déclaration du package Java : `com.diamyaraam.patient.entity`
package com.diamyaraam.patient.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UpdateTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UpdateTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « permission_acces », schéma « patient_schema »
@Table(name = "permission_acces", schema = "patient_schema")
// Déclaration de la classe `PermissionAcces`
public class PermissionAcces {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Colonne SQL associée à l'attribut : nom « dossier_medical_id », obligatoire (NOT NULL)
    @Column(name = "dossier_medical_id", nullable = false)
    // Attribut `dossierMedicalId` de type identifiant UUID [privée]
    private UUID dossierMedicalId;

    // Colonne SQL associée à l'attribut : nom « utilisateur_autorise_id », obligatoire (NOT NULL)
    @Column(name = "utilisateur_autorise_id", nullable = false)
    // Attribut `utilisateurAutoriseId` de type identifiant UUID [privée]
    private UUID utilisateurAutoriseId;

    // Déclaration de l'énumération `TypeAcces`
    public enum TypeAcces {
        // Constante(s) de l'énumération : LECTURE_TOTALE, LECTURE_PARTIELLE, URGENCE_UNIQUEMENT, FAMILLE_LECTURE
        LECTURE_TOTALE, LECTURE_PARTIELLE, URGENCE_UNIQUEMENT, FAMILLE_LECTURE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_acces », obligatoire (NOT NULL), longueur max 30
    @Column(name = "type_acces", length = 30, nullable = false)
    // Attribut `typeAcces` de type TypeAcces [privée]
    private TypeAcces typeAcces;

    // Déclaration de l'énumération `LienFamilial`
    public enum LienFamilial {
        // Constante(s) de l'énumération : CONJOINT, PARENT, ENFANT, FRERE_SOEUR, TUTEUR_LEGAL, AUTRE
        CONJOINT, PARENT, ENFANT, FRERE_SOEUR, TUTEUR_LEGAL, AUTRE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « lien_familial », longueur max 20
    @Column(name = "lien_familial", length = 20)
    // Attribut `lienFamilial` de type LienFamilial [privée]
    private LienFamilial lienFamilial;

    // Colonne SQL associée à l'attribut : nom « sections_autorisees », type SQL TEXT
    @Column(name = "sections_autorisees", columnDefinition = "TEXT")
    // Attribut `sectionsAutorisees` de type chaîne de caractères [privée]
    private String sectionsAutorisees;

    // Colonne SQL associée à l'attribut : nom « date_expiration »
    @Column(name = "date_expiration")
    // Attribut `dateExpiration` de type date-heure [privée]
    private LocalDateTime dateExpiration;

    // Colonne SQL associée à l'attribut : nom « actif », obligatoire (NOT NULL)
    @Column(name = "actif", nullable = false)
    // Attribut `actif` de type booléen [privée] ; valeur initiale : le booléen vrai
    private Boolean actif = true;

    // Colonne SQL associée à l'attribut : longueur max 300
    @Column(length = 300)
    // Attribut `motif` (motif) de type chaîne de caractères [privée]
    private String motif;

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

    // Constructeur de `PermissionAcces` sans paramètre
    public PermissionAcces() {}

    // Méthode `isValide` (publique) — sans paramètre ; retourne : booléen ; intention : teste si (is valide)
    public boolean isValide() {
        // Condition : exécute le bloc suivant seulement si `!Boolean.TRUE.equals(this.actif)) return false;`
        if (!Boolean.TRUE.equals(this.actif)) return false;
        // Condition : exécute le bloc suivant seulement si `this.dateExpiration == null) return true;`
        if (this.dateExpiration == null) return true;
        // Retourne la date et l'heure courantes (LocalDateTime.now().isBefore(this.dateExpiration))
        return LocalDateTime.now().isBefore(this.dateExpiration);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedicalId`
    public UUID getDossierMedicalId() { return dossierMedicalId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedicalId`
    public void setDossierMedicalId(UUID dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `utilisateurAutoriseId`
    public UUID getUtilisateurAutoriseId() { return utilisateurAutoriseId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `utilisateurAutoriseId`
    public void setUtilisateurAutoriseId(UUID utilisateurAutoriseId) { this.utilisateurAutoriseId = utilisateurAutoriseId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeAcces`
    public TypeAcces getTypeAcces() { return typeAcces; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeAcces`
    public void setTypeAcces(TypeAcces typeAcces) { this.typeAcces = typeAcces; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lienFamilial`
    public LienFamilial getLienFamilial() { return lienFamilial; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lienFamilial`
    public void setLienFamilial(LienFamilial lienFamilial) { this.lienFamilial = lienFamilial; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `sectionsAutorisees`
    public String getSectionsAutorisees() { return sectionsAutorisees; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `sectionsAutorisees`
    public void setSectionsAutorisees(String sectionsAutorisees) { this.sectionsAutorisees = sectionsAutorisees; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateExpiration`
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateExpiration`
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `actif`
    public Boolean getActif() { return actif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `actif`
    public void setActif(Boolean actif) { this.actif = actif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `motif`
    public String getMotif() { return motif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `motif`
    public void setMotif(String motif) { this.motif = motif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
