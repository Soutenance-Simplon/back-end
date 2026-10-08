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
 * ENTITÉ : Transaction — Historique immuable des opérations financières (RM142)
 *
 * RM142 — Historique complet des transactions (ne peut pas être supprimé).
 */
@Entity
// Table SQL associée à l'entité : table « transaction_financiere », schéma « wallet_schema »
@Table(name = "transaction_financiere", schema = "wallet_schema")
// Déclaration de la classe `Transaction`
public class Transaction {

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
    // Colonne de clé étrangère de la relation : colonne « portefeuille_id »
    @JoinColumn(name = "portefeuille_id", nullable = false)
    // Attribut `portefeuille` de type Portefeuille [privée]
    private Portefeuille portefeuille;

    // Déclaration de l'énumération `TypeTransaction`
    public enum TypeTransaction {
        // Constante(s) de l'énumération : DEPOT
        DEPOT,                  // Alimentation par Mobile Money, CB, etc. (RM134)
        // Constante(s) de l'énumération : PAIEMENT_TELECONSULTATION
        PAIEMENT_TELECONSULTATION, // Règlement consultation (RM139)
        // Constante(s) de l'énumération : PAIEMENT_PARTENAIRE
        PAIEMENT_PARTENAIRE,    // Règlement établissement partenaire (RM140)
        // Constante(s) de l'énumération : PAIEMENT_FAMILIAL
        PAIEMENT_FAMILIAL,      // Prise en charge d'un proche (RM138)
        // Constante(s) de l'énumération : REMBOURSEMENT
        REMBOURSEMENT,          // Remboursement en cas d'annulation
        // Constante(s) de l'énumération : HONORAIRES_CONSULTATION
        HONORAIRES_CONSULTATION // Crédit honoraires reçus par le médecin (RM139)
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_transaction », obligatoire (NOT NULL), longueur max 35
    @Column(name = "type_transaction", length = 35, nullable = false)
    // Attribut `typeTransaction` de type TypeTransaction [privée]
    private TypeTransaction typeTransaction;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false, precision = 15, scale = 2)
    // Attribut `montant` (montant en FCFA) de type montant décimal précis [privée]
    private BigDecimal montant;

    // Déclaration de l'énumération `MoyenPaiement`
    public enum MoyenPaiement {
        // Constante(s) de l'énumération : MOBILE_MONEY_ORANGE
        MOBILE_MONEY_ORANGE,
        // Constante(s) de l'énumération : MOBILE_MONEY_WAVE
        MOBILE_MONEY_WAVE,
        // Constante(s) de l'énumération : CARTE_BANCAIRE
        CARTE_BANCAIRE,
        // Constante(s) de l'énumération : VIREMENT
        VIREMENT,
        // Constante(s) de l'énumération : SOLDE_PORTEFEUILLE
        SOLDE_PORTEFEUILLE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « moyen_paiement », longueur max 30
    @Column(name = "moyen_paiement", length = 30)
    // Attribut `moyenPaiement` de type MoyenPaiement [privée]
    private MoyenPaiement moyenPaiement;

    // Déclaration de l'énumération `StatutTransaction`
    public enum StatutTransaction {
        // Constante(s) de l'énumération : EN_ATTENTE
        EN_ATTENTE,   // En attente de confirmation du prestataire (RM135)
        // Constante(s) de l'énumération : VALIDE
        VALIDE,       // Confirmé et comptabilisé
        // Constante(s) de l'énumération : ECHOUE
        ECHOUE,       // Échec ou solde insuffisant (RM141)
        // Constante(s) de l'énumération : ANNULE
        ANNULE        // Annulé par l'utilisateur ou le prestataire
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutTransaction [privée] ; valeur initiale : la valeur de StatutTransaction.EN_ATTENTE
    private StatutTransaction statut = StatutTransaction.EN_ATTENTE;

    // Colonne SQL associée à l'attribut : nom « reference_externe », longueur max 100
    @Column(name = "reference_externe", length = 100)
    // Attribut `referenceExterne` de type chaîne de caractères [privée]
    private String referenceExterne;   // Référence Orange Money / Wave / CB

    // Colonne SQL associée à l'attribut : nom « beneficiaire_user_id »
    @Column(name = "beneficiaire_user_id")
    // Attribut `beneficiaireUserId` de type identifiant UUID [privée]
    private UUID beneficiaireUserId;   // Proche bénéficiaire du soin

    // Colonne SQL associée à l'attribut : nom « service_id »
    @Column(name = "service_id")
    // Attribut `serviceId` (identifiant du service concerné) de type identifiant UUID [privée]
    private UUID serviceId;            // ID du RDV ou de la consultation

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `description` (description) de type chaîne de caractères [privée]
    private String description;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « date_transaction », non modifiable après création
    @Column(name = "date_transaction", updatable = false)
    // Attribut `dateTransaction` (date de la transaction) de type date-heure [privée]
    private LocalDateTime dateTransaction;

    // Constructeur de `Transaction` sans paramètre
    public Transaction() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `portefeuille`
    public Portefeuille getPortefeuille() { return portefeuille; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `portefeuille`
    public void setPortefeuille(Portefeuille portefeuille) { this.portefeuille = portefeuille; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeTransaction`
    public TypeTransaction getTypeTransaction() { return typeTransaction; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeTransaction`
    public void setTypeTransaction(TypeTransaction typeTransaction) { this.typeTransaction = typeTransaction; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `montant`
    public BigDecimal getMontant() { return montant; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `montant`
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `moyenPaiement`
    public MoyenPaiement getMoyenPaiement() { return moyenPaiement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `moyenPaiement`
    public void setMoyenPaiement(MoyenPaiement moyenPaiement) { this.moyenPaiement = moyenPaiement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutTransaction getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutTransaction statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `referenceExterne`
    public String getReferenceExterne() { return referenceExterne; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `referenceExterne`
    public void setReferenceExterne(String referenceExterne) { this.referenceExterne = referenceExterne; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `beneficiaireUserId`
    public UUID getBeneficiaireUserId() { return beneficiaireUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `beneficiaireUserId`
    public void setBeneficiaireUserId(UUID beneficiaireUserId) { this.beneficiaireUserId = beneficiaireUserId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `serviceId`
    public UUID getServiceId() { return serviceId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `serviceId`
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `description`
    public String getDescription() { return description; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `description`
    public void setDescription(String description) { this.description = description; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateTransaction`
    public LocalDateTime getDateTransaction() { return dateTransaction; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateTransaction`
    public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }
}
