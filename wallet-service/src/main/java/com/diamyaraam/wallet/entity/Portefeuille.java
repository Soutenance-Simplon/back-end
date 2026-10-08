// Déclaration du package Java : `com.diamyaraam.wallet.entity`
package com.diamyaraam.wallet.entity;

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
 * ENTITÉ : Portefeuille (Health Wallet) — RM131 à RM133
 *
 * RM131 — Création automatique à la création du compte. Un seul portefeuille principal par utilisateur.
 * RM132 — Identifiant UUID indépendant du compte utilisateur.
 * RM133 — Solde exprimé en FCFA, jamais négatif.
 */
@Entity
// Table SQL associée à l'entité : table « portefeuille », schéma « wallet_schema »
@Table(name = "portefeuille", schema = "wallet_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `Portefeuille`
public class Portefeuille {

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

    // Solde en FCFA (jamais négatif) — RM133
    @Column(nullable = false, precision = 15, scale = 2)
    // Attribut `solde` (solde en FCFA) de type montant décimal précis [privée] ; valeur initiale : la valeur de BigDecimal.ZERO
    private BigDecimal solde = BigDecimal.ZERO;

    // Colonne SQL associée à l'attribut : nom « devise », obligatoire (NOT NULL), longueur max 10
    @Column(name = "devise", length = 10, nullable = false)
    // Attribut `devise` (devise) de type chaîne de caractères [privée] ; valeur initiale : le texte "FCFA"
    private String devise = "FCFA";

    // Déclaration de l'énumération `StatutPortefeuille`
    public enum StatutPortefeuille { ACTIF, SUSPENDU, BLOQUE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutPortefeuille [privée] ; valeur initiale : la valeur de StatutPortefeuille.ACTIF
    private StatutPortefeuille statut = StatutPortefeuille.ACTIF;

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

    // Constructeur de `Portefeuille` sans paramètre
    public Portefeuille() {}

    // Méthode `aSoldeSuffisant` (publique) — paramètres : `montant` (montant décimal précis) ; retourne : booléen
    public boolean aSoldeSuffisant(BigDecimal montant) {
        // Condition : exécute le bloc suivant seulement si `montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) return false;`
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) return false;
        // Retourne `this.solde.compareTo(montant) >= 0`
        return this.solde.compareTo(montant) >= 0;
    }

    // Méthode `crediter` (publique) — paramètres : `montant` (montant décimal précis) ; retourne : aucune valeur
    public void crediter(BigDecimal montant) {
        // Condition : exécute le bloc suivant seulement si `montant != null && montant.compareTo(BigDecimal.ZERO) > 0`
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            // Initialise l'attribut `solde` avec `this.solde.add(montant)`
            this.solde = this.solde.add(montant);
        }
    }

    // Méthode `debiter` (publique) — paramètres : `montant` (montant décimal précis) ; retourne : aucune valeur
    public void debiter(BigDecimal montant) {
        // Condition : exécute le bloc suivant seulement si `!aSoldeSuffisant(montant)`
        if (!aSoldeSuffisant(montant)) {
            // Lève l'exception IllegalStateException avec le message « Solde insuffisant dans le Portefeuille Santé. »
            throw new IllegalStateException("Solde insuffisant dans le Portefeuille Santé.");
        }
        // Initialise l'attribut `solde` avec `this.solde.subtract(montant)`
        this.solde = this.solde.subtract(montant);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `solde`
    public BigDecimal getSolde() { return solde; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `solde`
    public void setSolde(BigDecimal solde) { this.solde = solde; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `devise`
    public String getDevise() { return devise; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `devise`
    public void setDevise(String devise) { this.devise = devise; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutPortefeuille getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutPortefeuille statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
