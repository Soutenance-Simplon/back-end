// Déclaration du package Java : `com.diamyaraam.auth.entity`
package com.diamyaraam.auth.entity;

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

/**
 * OTP stocké en BDD (RM026-RM028)
 */
@Entity
// Table SQL associée à l'entité : table « otp_code », schéma « auth_schema »
@Table(name = "otp_code", schema = "auth_schema")
// Déclaration de la classe `OtpCode`
public class OtpCode {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Déclaration de l'énumération `OtpType`
    public enum OtpType {
        // Constante(s) de l'énumération : VERIFICATION_TELEPHONE
        VERIFICATION_TELEPHONE,
        // Constante(s) de l'énumération : REINITIALISATION_MDP
        REINITIALISATION_MDP,
        // Constante(s) de l'énumération : DOUBLE_AUTHENTIFICATION
        DOUBLE_AUTHENTIFICATION
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « otp_type », obligatoire (NOT NULL), longueur max 30
    @Column(name = "otp_type", length = 30, nullable = false)
    // Attribut `otpType` de type OtpType [privée]
    private OtpType otpType;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 20
    @Column(nullable = false, length = 20)
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 6
    @Column(nullable = false, length = 6)
    // Attribut `code` (code) de type chaîne de caractères [privée]
    private String code;

    // Colonne SQL associée à l'attribut : nom « expires_at », obligatoire (NOT NULL)
    @Column(name = "expires_at", nullable = false)
    // Attribut `expiresAt` de type date-heure [privée]
    private LocalDateTime expiresAt;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false)
    // Attribut `attempts` de type entier [privée] ; valeur initiale : la valeur numérique 0
    private Integer attempts = 0;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false)
    // Attribut `used` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean used = false;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `OtpCode` sans paramètre
    public OtpCode() {}

    // Méthode `isExpired` (publique) — sans paramètre ; retourne : booléen ; intention : teste si (is expired)
    public boolean isExpired() {
        // Retourne la date et l'heure courantes (LocalDateTime.now().isAfter(this.expiresAt))
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // Méthode `isMaxAttemptsReached` (publique) — paramètres : `max` (entier) ; retourne : booléen ; intention : teste si (is max attempts reached)
    public boolean isMaxAttemptsReached(int max) {
        // Retourne `this.attempts >= max`
        return this.attempts >= max;
    }

    // Méthode `isValid` (publique) — paramètres : `max` (entier) ; retourne : booléen ; intention : teste si (is valid)
    public boolean isValid(int max) {
        // Retourne `!this.used && !isExpired() && !isMaxAttemptsReached(max)`
        return !this.used && !isExpired() && !isMaxAttemptsReached(max);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `otpType`
    public OtpType getOtpType() { return otpType; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `otpType`
    public void setOtpType(OtpType otpType) { this.otpType = otpType; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `code`
    public String getCode() { return code; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `code`
    public void setCode(String code) { this.code = code; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `expiresAt`
    public LocalDateTime getExpiresAt() { return expiresAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `expiresAt`
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `attempts`
    public Integer getAttempts() { return attempts; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `attempts`
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `used`
    public Boolean getUsed() { return used; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `used`
    public void setUsed(Boolean used) { this.used = used; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
