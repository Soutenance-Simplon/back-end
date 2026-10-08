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

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « carte_physique », schéma « dossier_schema »
@Table(name = "carte_physique", schema = "dossier_schema")
// Déclaration de la classe `CartePhysique`
public class CartePhysique {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Le numéro de série lisible par un humain imprimé sur la carte (ex: DY-123-456)
    @Column(name = "numero_serie", nullable = false, unique = true, length = 50)
    // Attribut `numeroSerie` de type chaîne de caractères [privée]
    private String numeroSerie;

    // Le code caché dans le QR Code (le "jeton sécurisé").
    // C'est ce que l'agent scanne pour lier la carte.
    @Column(name = "qr_token_securise", nullable = false, unique = true, length = 255)
    // Attribut `qrTokenSecurise` de type chaîne de caractères [privée]
    private String qrTokenSecurise;

    // L'ID du patient auquel la carte est liée.
    // Il est NULL au début quand la carte est juste imprimée et stockée en agence.
    @Column(name = "patient_id")
    // Attribut `patientId` (identifiant du patient) de type identifiant UUID [privée]
    private UUID patientId;

    // Déclaration de l'énumération `StatutCarte`
    public enum StatutCarte {
        // Constante(s) de l'énumération : EN_STOCK
        EN_STOCK,     // Imprimée mais pas encore vendue
        // Constante(s) de l'énumération : ACTIVE
        ACTIVE,       // Vendue et liée à un patient
        // Constante(s) de l'énumération : PERDUE
        PERDUE,       // Le patient a déclaré la perte de la carte
        // Constante(s) de l'énumération : DESACTIVEE
        DESACTIVEE    // La carte a été remplacée
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut_carte », obligatoire (NOT NULL), longueur max 20
    @Column(name = "statut_carte", length = 20, nullable = false)
    // Attribut `statut` (statut) de type StatutCarte [privée] ; valeur initiale : la valeur de StatutCarte.EN_STOCK
    private StatutCarte statut = StatutCarte.EN_STOCK;

    // La date à laquelle la carte a été liée à un patient
    @Column(name = "date_activation")
    // Attribut `dateActivation` de type date-heure [privée]
    private LocalDateTime dateActivation;

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

    // Constructeur de `CartePhysique` sans paramètre
    public CartePhysique() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `numeroSerie`
    public String getNumeroSerie() { return numeroSerie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `numeroSerie`
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `qrTokenSecurise`
    public String getQrTokenSecurise() { return qrTokenSecurise; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `qrTokenSecurise`
    public void setQrTokenSecurise(String qrTokenSecurise) { this.qrTokenSecurise = qrTokenSecurise; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `patientId`
    public UUID getPatientId() { return patientId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `patientId`
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutCarte getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutCarte statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateActivation`
    public LocalDateTime getDateActivation() { return dateActivation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateActivation`
    public void setDateActivation(LocalDateTime dateActivation) { this.dateActivation = dateActivation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
