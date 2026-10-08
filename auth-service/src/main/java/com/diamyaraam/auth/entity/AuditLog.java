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
 * Journal des actions sensibles (RM029)
 */
@Entity
// Table SQL associée à l'entité : table « audit_log », schéma « auth_schema »
@Table(name = "audit_log", schema = "auth_schema")
// Déclaration de la classe `AuditLog`
public class AuditLog {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Déclaration de l'énumération `ActionType`
    public enum ActionType {
        // Constante(s) de l'énumération : CREATION_COMPTE
        CREATION_COMPTE,
        // Constante(s) de l'énumération : ACTIVATION_COMPTE
        ACTIVATION_COMPTE,
        // Constante(s) de l'énumération : CONNEXION_REUSSIE
        CONNEXION_REUSSIE,
        // Constante(s) de l'énumération : ECHEC_CONNEXION
        ECHEC_CONNEXION,
        // Constante(s) de l'énumération : COMPTE_BLOQUE
        COMPTE_BLOQUE,
        // Constante(s) de l'énumération : COMPTE_DEBLOQUE
        COMPTE_DEBLOQUE,
        // Constante(s) de l'énumération : CHANGEMENT_MOT_DE_PASSE
        CHANGEMENT_MOT_DE_PASSE,
        // Constante(s) de l'énumération : DEMANDE_REINITIALISATION_MDP
        DEMANDE_REINITIALISATION_MDP,
        // Constante(s) de l'énumération : OTP_ENVOYE
        OTP_ENVOYE,
        // Constante(s) de l'énumération : OTP_VALIDE
        OTP_VALIDE,
        // Constante(s) de l'énumération : OTP_EXPIRE
        OTP_EXPIRE,
        // Constante(s) de l'énumération : OTP_ECHEC
        OTP_ECHEC,
        // Constante(s) de l'énumération : OTP_MAX_ATTEINT
        OTP_MAX_ATTEINT,
        // Constante(s) de l'énumération : MODIFICATION_PROFIL
        MODIFICATION_PROFIL,
        // Constante(s) de l'énumération : ACCES_DOSSIER_MEDECIN
        ACCES_DOSSIER_MEDECIN,
        // Constante(s) de l'énumération : ACCES_DOSSIER_URGENCE_BRIS_DE_GLACE
        ACCES_DOSSIER_URGENCE_BRIS_DE_GLACE,
        // Constante(s) de l'énumération : SIGNALEMENT_ACCES_ABUSIF
        SIGNALEMENT_ACCES_ABUSIF
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « action_type », obligatoire (NOT NULL), longueur max 40
    @Column(name = "action_type", length = 40, nullable = false)
    // Attribut `actionType` de type ActionType [privée]
    private ActionType actionType;

    // Relation plusieurs-à-un avec une autre entité
    @ManyToOne(fetch = FetchType.LAZY)
    // Colonne de clé étrangère de la relation : colonne « user_id »
    @JoinColumn(name = "user_id", nullable = true)
    // Attribut `user` de type User [privée]
    private User user;

    // Colonne SQL associée à l'attribut : nom « telephone_tente », longueur max 20
    @Column(name = "telephone_tente", length = 20)
    // Attribut `telephoneTente` de type chaîne de caractères [privée]
    private String telephoneTente;

    // Colonne SQL associée à l'attribut : nom « ip_address », longueur max 45
    @Column(name = "ip_address", length = 45)
    // Attribut `ipAddress` de type chaîne de caractères [privée]
    private String ipAddress;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `details` (détails) de type chaîne de caractères [privée]
    private String details;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false)
    // Attribut `success` (indicateur de succès) de type booléen [privée] ; valeur initiale : le booléen vrai
    private Boolean success = true;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `AuditLog` sans paramètre
    public AuditLog() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `actionType`
    public ActionType getActionType() { return actionType; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `actionType`
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `user`
    public User getUser() { return user; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `user`
    public void setUser(User user) { this.user = user; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephoneTente`
    public String getTelephoneTente() { return telephoneTente; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephoneTente`
    public void setTelephoneTente(String telephoneTente) { this.telephoneTente = telephoneTente; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `ipAddress`
    public String getIpAddress() { return ipAddress; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `ipAddress`
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `details`
    public String getDetails() { return details; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `details`
    public void setDetails(String details) { this.details = details; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `success`
    public Boolean getSuccess() { return success; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `success`
    public void setSuccess(Boolean success) { this.success = success; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
