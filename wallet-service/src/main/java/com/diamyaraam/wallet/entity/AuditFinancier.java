// Déclaration du package Java : `com.diamyaraam.wallet.entity`
package com.diamyaraam.wallet.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `BigDecimal` (paquet java.math)
import java.math.BigDecimal;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ENTITÉ : AuditFinancier — Traçabilité et détection d'anomalies (RM145)
 */
@Entity
// Table SQL associée à l'entité : table « audit_financier », schéma « wallet_schema »
@Table(name = "audit_financier", schema = "wallet_schema")
// Déclaration de la classe `AuditFinancier`
public class AuditFinancier {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Colonne SQL associée à l'attribut : nom « user_id », obligatoire (NOT NULL)
    @Column(name = "user_id", nullable = false)
    // Attribut `userId` (identifiant de l'utilisateur) de type identifiant UUID [privée]
    private UUID userId;

    // Colonne SQL associée à l'attribut : nom « transaction_id »
    @Column(name = "transaction_id")
    // Attribut `transactionId` de type identifiant UUID [privée]
    private UUID transactionId;

    // Déclaration de l'énumération `ActionFinanciere`
    public enum ActionFinanciere {
        // Constante(s) de l'énumération : DEPOT, PAIEMENT, REMBOURSEMENT, ECHEC_SOLDE_INSUFFISANT, ANOMALIE_SUSPECTE
        DEPOT, PAIEMENT, REMBOURSEMENT, ECHEC_SOLDE_INSUFFISANT, ANOMALIE_SUSPECTE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « action », obligatoire (NOT NULL), longueur max 35
    @Column(name = "action", length = 35, nullable = false)
    // Attribut `action` (action effectuée) de type ActionFinanciere [privée]
    private ActionFinanciere action;

    // Colonne SQL associée à l'attribut
    @Column(precision = 15, scale = 2)
    // Attribut `montant` (montant en FCFA) de type montant décimal précis [privée]
    private BigDecimal montant;

    // Colonne SQL associée à l'attribut : nom « ip_address », longueur max 45
    @Column(name = "ip_address", length = 45)
    // Attribut `ipAddress` de type chaîne de caractères [privée]
    private String ipAddress;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false)
    // Attribut `success` (indicateur de succès) de type booléen [privée] ; valeur initiale : le booléen vrai
    private Boolean success = true;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `details` (détails) de type chaîne de caractères [privée]
    private String details;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `AuditFinancier` sans paramètre
    public AuditFinancier() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `transactionId`
    public UUID getTransactionId() { return transactionId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `transactionId`
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `action`
    public ActionFinanciere getAction() { return action; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `action`
    public void setAction(ActionFinanciere action) { this.action = action; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `montant`
    public BigDecimal getMontant() { return montant; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `montant`
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `ipAddress`
    public String getIpAddress() { return ipAddress; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `ipAddress`
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `success`
    public Boolean getSuccess() { return success; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `success`
    public void setSuccess(Boolean success) { this.success = success; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `details`
    public String getDetails() { return details; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `details`
    public void setDetails(String details) { this.details = details; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
