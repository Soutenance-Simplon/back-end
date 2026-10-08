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
 * ENTITÉ : Beneficiaire — Portefeuille Familial (RM136 à RM138)
 *
 * RM136 — Prise en charge financière d'un proche (enfant, parent, conjoint...)
 * RM137 — Gestion de l'accès (ACTIF, SUSPENDU) par le titulaire
 * RM138 — Débit automatique du tuteur
 */
@Entity
// Table SQL associée à l'entité : table « beneficiaire », schéma « wallet_schema »
@Table(name = "beneficiaire", schema = "wallet_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `Beneficiaire`
public class Beneficiaire {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Le portefeuille du tuteur (payeur)
    @ManyToOne(fetch = FetchType.EAGER)
    // Colonne de clé étrangère de la relation : colonne « portefeuille_id »
    @JoinColumn(name = "portefeuille_id", nullable = false)
    // Ignore certaines propriétés JSON
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    // Attribut `portefeuille` de type Portefeuille [privée]
    private Portefeuille portefeuille;

    // L'utilisateur proche bénéficiaire
    @Column(name = "beneficiaire_user_id", nullable = false)
    // Attribut `beneficiaireUserId` de type identifiant UUID [privée]
    private UUID beneficiaireUserId;

    // Déclaration de l'énumération `LienParente`
    public enum LienParente { ENFANT, PARENT, CONJOINT, AUTRE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « lien_parente », obligatoire (NOT NULL), longueur max 20
    @Column(name = "lien_parente", length = 20, nullable = false)
    // Attribut `lienParente` de type LienParente [privée]
    private LienParente lienParente;

    // Déclaration de l'énumération `StatutBeneficiaire`
    public enum StatutBeneficiaire { EN_ATTENTE, ACTIF, SUSPENDU, REJETE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutBeneficiaire [privée] ; valeur initiale : la valeur de StatutBeneficiaire.EN_ATTENTE
    private StatutBeneficiaire statut = StatutBeneficiaire.EN_ATTENTE;

    // Plafond mensuel de dépense autorisé (null = pas de plafond)
    @Column(name = "plafond_mensuel", precision = 15, scale = 2)
    // Attribut `plafondMensuel` de type montant décimal précis [privée]
    private BigDecimal plafondMensuel;

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

    // Constructeur de `Beneficiaire` sans paramètre
    public Beneficiaire() {}

    // Méthode `isAutorise` (publique) — sans paramètre ; retourne : booléen ; intention : teste si (is autorise)
    public boolean isAutorise() {
        // Retourne `StatutBeneficiaire.ACTIF.equals(this.statut)`
        return StatutBeneficiaire.ACTIF.equals(this.statut);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `portefeuille`
    public Portefeuille getPortefeuille() { return portefeuille; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `portefeuille`
    public void setPortefeuille(Portefeuille portefeuille) { this.portefeuille = portefeuille; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `beneficiaireUserId`
    public UUID getBeneficiaireUserId() { return beneficiaireUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `beneficiaireUserId`
    public void setBeneficiaireUserId(UUID beneficiaireUserId) { this.beneficiaireUserId = beneficiaireUserId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lienParente`
    public LienParente getLienParente() { return lienParente; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lienParente`
    public void setLienParente(LienParente lienParente) { this.lienParente = lienParente; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutBeneficiaire getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutBeneficiaire statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `plafondMensuel`
    public BigDecimal getPlafondMensuel() { return plafondMensuel; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `plafondMensuel`
    public void setPlafondMensuel(BigDecimal plafondMensuel) { this.plafondMensuel = plafondMensuel; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
