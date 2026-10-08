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
// Table SQL associée à l'entité : table « patients_patient », schéma « patient_schema »
@Table(name = "patients_patient", schema = "patient_schema")
// Déclaration de la classe `Patient`
public class Patient {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // UUID reference to User in auth-service
    @Column(name = "user_id", nullable = false, unique = true)
    // Attribut `userId` (identifiant de l'utilisateur) de type identifiant UUID [privée]
    private UUID userId;

    // Colonne SQL associée à l'attribut : nom « contact_urgence_nom », longueur max 200
    @Column(name = "contact_urgence_nom", length = 200)
    // Attribut `contactUrgenceNom` (nom du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceNom;

    // Colonne SQL associée à l'attribut : nom « contact_urgence_telephone », longueur max 20
    @Column(name = "contact_urgence_telephone", length = 20)
    // Attribut `contactUrgenceTelephone` (téléphone du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceTelephone;

    // Colonne SQL associée à l'attribut : nom « contact_urgence_lien », longueur max 100
    @Column(name = "contact_urgence_lien", length = 100)
    // Attribut `contactUrgenceLien` (lien de parenté du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceLien;

    // Colonne SQL associée à l'attribut : longueur max 500
    @Column(length = 500)
    // Attribut `adresse` (adresse) de type chaîne de caractères [privée]
    private String adresse;

    // Colonne SQL associée à l'attribut : longueur max 100
    @Column(length = 100)
    // Attribut `ville` (ville) de type chaîne de caractères [privée]
    private String ville;

    // Colonne SQL associée à l'attribut : longueur max 100
    @Column(length = 100)
    // Attribut `region` de type chaîne de caractères [privée]
    private String region;

    // Colonne SQL associée à l'attribut : nom « consent_analyse_ia », obligatoire (NOT NULL)
    @Column(name = "consent_analyse_ia", nullable = false)
    // Attribut `consentAnalyseIa` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean consentAnalyseIa = false;

    // Colonne SQL associée à l'attribut : nom « consent_partage_famille », obligatoire (NOT NULL)
    @Column(name = "consent_partage_famille", nullable = false)
    // Attribut `consentPartageFamille` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean consentPartageFamille = false;

    // Colonne SQL associée à l'attribut : nom « nfc_id », longueur max 100
    @Column(name = "nfc_id", length = 100)
    // Attribut `nfcId` de type chaîne de caractères [privée]
    private String nfcId;

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

    // Constructeur de `Patient` sans paramètre
    public Patient() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceNom`
    public String getContactUrgenceNom() { return contactUrgenceNom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceNom`
    public void setContactUrgenceNom(String contactUrgenceNom) { this.contactUrgenceNom = contactUrgenceNom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceTelephone`
    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceTelephone`
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) { this.contactUrgenceTelephone = contactUrgenceTelephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceLien`
    public String getContactUrgenceLien() { return contactUrgenceLien; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceLien`
    public void setContactUrgenceLien(String contactUrgenceLien) { this.contactUrgenceLien = contactUrgenceLien; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `adresse`
    public String getAdresse() { return adresse; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `adresse`
    public void setAdresse(String adresse) { this.adresse = adresse; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `ville`
    public String getVille() { return ville; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `ville`
    public void setVille(String ville) { this.ville = ville; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `region`
    public String getRegion() { return region; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `region`
    public void setRegion(String region) { this.region = region; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `consentAnalyseIa`
    public Boolean getConsentAnalyseIa() { return consentAnalyseIa; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `consentAnalyseIa`
    public void setConsentAnalyseIa(Boolean consentAnalyseIa) { this.consentAnalyseIa = consentAnalyseIa; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `consentPartageFamille`
    public Boolean getConsentPartageFamille() { return consentPartageFamille; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `consentPartageFamille`
    public void setConsentPartageFamille(Boolean consentPartageFamille) { this.consentPartageFamille = consentPartageFamille; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nfcId`
    public String getNfcId() { return nfcId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nfcId`
    public void setNfcId(String nfcId) { this.nfcId = nfcId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
