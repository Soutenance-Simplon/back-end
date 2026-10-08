// Déclaration du package Java : `com.diamyaraam.medecin.entity`
package com.diamyaraam.medecin.entity;

// Import de la classe `JsonIgnoreProperties` (paquet com.fasterxml.jackson.annotation)
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UpdateTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UpdateTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `BigDecimal` (paquet java.math)
import java.math.BigDecimal;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ENTITÉ : Medecin — medecin-service
 *
 * Lien par userId (UUID) vers le compte dans auth-service.
 */
@Entity
// Table SQL associée à l'entité : table « medecin », schéma « medecin_schema »
@Table(name = "medecin", schema = "medecin_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `Medecin`
public class Medecin {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Lien par UUID vers auth-service (pas de FK cross-service)
    @Column(name = "user_id", nullable = false, unique = true)
    // Attribut `userId` (identifiant de l'utilisateur) de type identifiant UUID [privée]
    private UUID userId;

    // Lien vers la base officielle ONMS
    @OneToOne(fetch = FetchType.LAZY)
    // Colonne de clé étrangère de la relation : colonne « onms_reference_id »
    @JoinColumn(name = "onms_reference_id", unique = true, nullable = false)
    // Attribut `onmsReference` de type OnmsReference [privée]
    private OnmsReference onmsReference;

    // Colonne SQL associée à l'attribut : nom « is_verified », obligatoire (NOT NULL)
    @Column(name = "is_verified", nullable = false)
    // Attribut `isVerified` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean isVerified = false;

    // Colonne SQL associée à l'attribut : nom « verified_at »
    @Column(name = "verified_at")
    // Attribut `verifiedAt` de type date-heure [privée]
    private LocalDateTime verifiedAt;

    // Colonne SQL associée à l'attribut : nom « photo_professionnelle »
    @Column(name = "photo_professionnelle")
    // Attribut `photoProfessionnelle` de type chaîne de caractères [privée]
    private String photoProfessionnelle;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `biographie` de type chaîne de caractères [privée]
    private String biographie;

    // Colonne SQL associée à l'attribut : longueur max 200
    @Column(length = 200)
    // Attribut `langues` de type chaîne de caractères [privée]
    private String langues;

    // Colonne SQL associée à l'attribut : nom « teleconsultation_active », obligatoire (NOT NULL)
    @Column(name = "teleconsultation_active", nullable = false)
    // Attribut `teleconsultationActive` de type booléen [privée] ; valeur initiale : le booléen vrai
    private Boolean teleconsultationActive = true;

    // Colonne SQL associée à l'attribut : nom « tarif_consultation »
    @Column(name = "tarif_consultation", precision = 10, scale = 2)
    // Attribut `tarifConsultation` de type montant décimal précis [privée]
    private BigDecimal tarifConsultation;

    // Colonne SQL associée à l'attribut : nom « duree_consultation_minutes », obligatoire (NOT NULL)
    @Column(name = "duree_consultation_minutes", nullable = false)
    // Attribut `dureeConsultationMinutes` de type entier [privée] ; valeur initiale : la valeur numérique 30
    private Integer dureeConsultationMinutes = 30;

    // Déclaration de l'énumération `StatutMedecin`
    public enum StatutMedecin { ACTIF, SUSPENDU, EN_ATTENTE_VERIFICATION }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut_medecin », obligatoire (NOT NULL), longueur max 30
    @Column(name = "statut_medecin", length = 30, nullable = false)
    // Attribut `statutMedecin` de type StatutMedecin [privée] ; valeur initiale : la valeur de StatutMedecin.EN_ATTENTE_VERIFICATION
    private StatutMedecin statutMedecin = StatutMedecin.EN_ATTENTE_VERIFICATION;

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

    // Constructeur de `Medecin` sans paramètre
    public Medecin() {}

    // Méthode `peutExercer` (publique) — sans paramètre ; retourne : booléen
    public boolean peutExercer() {
        // Retourne `StatutMedecin.ACTIF.equals(this.statutMedecin) && Boolean.TRUE.equals(this.isVe…`
        return StatutMedecin.ACTIF.equals(this.statutMedecin) && Boolean.TRUE.equals(this.isVerified);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `onmsReference`
    public OnmsReference getOnmsReference() { return onmsReference; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `onmsReference`
    public void setOnmsReference(OnmsReference onmsReference) { this.onmsReference = onmsReference; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `isVerified`
    public Boolean getIsVerified() { return isVerified; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `isVerified`
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `verifiedAt`
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `verifiedAt`
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoProfessionnelle`
    public String getPhotoProfessionnelle() { return photoProfessionnelle; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoProfessionnelle`
    public void setPhotoProfessionnelle(String photoProfessionnelle) { this.photoProfessionnelle = photoProfessionnelle; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `biographie`
    public String getBiographie() { return biographie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `biographie`
    public void setBiographie(String biographie) { this.biographie = biographie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `langues`
    public String getLangues() { return langues; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `langues`
    public void setLangues(String langues) { this.langues = langues; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `teleconsultationActive`
    public Boolean getTeleconsultationActive() { return teleconsultationActive; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `teleconsultationActive`
    public void setTeleconsultationActive(Boolean teleconsultationActive) { this.teleconsultationActive = teleconsultationActive; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tarifConsultation`
    public BigDecimal getTarifConsultation() { return tarifConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tarifConsultation`
    public void setTarifConsultation(BigDecimal tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dureeConsultationMinutes`
    public Integer getDureeConsultationMinutes() { return dureeConsultationMinutes; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dureeConsultationMinutes`
    public void setDureeConsultationMinutes(Integer dureeConsultationMinutes) { this.dureeConsultationMinutes = dureeConsultationMinutes; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statutMedecin`
    public StatutMedecin getStatutMedecin() { return statutMedecin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statutMedecin`
    public void setStatutMedecin(StatutMedecin statutMedecin) { this.statutMedecin = statutMedecin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
