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

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « membre_famille », schéma « patient_schema »
@Table(name = "membre_famille", schema = "patient_schema")
// Déclaration de la classe `MembreFamille`
public class MembreFamille {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Colonne SQL associée à l'attribut : nom « parent_user_id », obligatoire (NOT NULL)
    @Column(name = "parent_user_id", nullable = false)
    // Attribut `parentUserId` de type identifiant UUID [privée]
    private UUID parentUserId;

    // Colonne SQL associée à l'attribut : nom « enfant_user_id », obligatoire (NOT NULL), valeur unique
    @Column(name = "enfant_user_id", nullable = false, unique = true)
    // Attribut `enfantUserId` de type identifiant UUID [privée]
    private UUID enfantUserId;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 150
    @Column(nullable = false, length = 150)
    // Attribut `nom` (nom) de type chaîne de caractères [privée]
    private String nom;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 150
    @Column(nullable = false, length = 150)
    // Attribut `prenom` (prénom) de type chaîne de caractères [privée]
    private String prenom;

    // Colonne SQL associée à l'attribut : nom « date_naissance »
    @Column(name = "date_naissance")
    // Attribut `dateNaissance` (date de naissance) de type date [privée]
    private LocalDate dateNaissance;

    // Colonne SQL associée à l'attribut : longueur max 20
    @Column(length = 20)
    // Attribut `genre` (genre (M ou F)) de type chaîne de caractères [privée]
    private String genre;

    // Colonne SQL associée à l'attribut : nom « lien_parente », longueur max 50
    @Column(name = "lien_parente", length = 50)
    // Attribut `lienParente` de type chaîne de caractères [privée]
    private String lienParente;

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

    // Constructeur de `MembreFamille` sans paramètre
    public MembreFamille() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `parentUserId`
    public UUID getParentUserId() { return parentUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `parentUserId`
    public void setParentUserId(UUID parentUserId) { this.parentUserId = parentUserId; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `enfantUserId`
    public UUID getEnfantUserId() { return enfantUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `enfantUserId`
    public void setEnfantUserId(UUID enfantUserId) { this.enfantUserId = enfantUserId; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `nom`
    public String getNom() { return nom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nom`
    public void setNom(String nom) { this.nom = nom; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `prenom`
    public String getPrenom() { return prenom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `prenom`
    public void setPrenom(String prenom) { this.prenom = prenom; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `dateNaissance`
    public LocalDate getDateNaissance() { return dateNaissance; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateNaissance`
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `genre`
    public String getGenre() { return genre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `genre`
    public void setGenre(String genre) { this.genre = genre; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `lienParente`
    public String getLienParente() { return lienParente; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lienParente`
    public void setLienParente(String lienParente) { this.lienParente = lienParente; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
