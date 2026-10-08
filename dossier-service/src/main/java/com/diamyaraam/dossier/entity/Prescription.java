// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

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

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « prescription », schéma « dossier_schema »
@Table(name = "prescription", schema = "dossier_schema")
// Déclaration de la classe `Prescription`
public class Prescription {

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
    // Colonne de clé étrangère de la relation : colonne « dossier_medical_id »
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    // Attribut `dossierMedical` de type DossierMedical [privée]
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DossierMedical dossierMedical;

    // Colonne SQL associée à l'attribut : nom « medecin_id », obligatoire (NOT NULL)
    @Column(name = "medecin_id", nullable = false)
    // Attribut `medecinId` (identifiant du médecin) de type identifiant UUID [privée]
    private UUID medecinId;

    // Colonne SQL associée à l'attribut : nom « numero_prescription », valeur unique, longueur max 50
    @Column(name = "numero_prescription", unique = true, length = 50)
    // Attribut `numeroPrescription` de type chaîne de caractères [privée]
    private String numeroPrescription;

    // Déclaration de l'énumération `StatutPrescription`
    public enum StatutPrescription { BROUILLON, ACTIVE, ANNULEE, REMPLACEE, EXPIREE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutPrescription [privée] ; valeur initiale : la valeur de StatutPrescription.ACTIVE
    private StatutPrescription statut = StatutPrescription.ACTIVE;

    // Colonne SQL associée à l'attribut : type SQL TEXT
    @Column(columnDefinition = "TEXT")
    // Attribut `detailsMedicaments` de type chaîne de caractères [privée]
    private String detailsMedicaments;

    // Colonne SQL associée à l'attribut : nom « rapport_ia », type SQL TEXT
    @Column(name = "rapport_ia", columnDefinition = "TEXT")
    // Attribut `rapportIa` de type chaîne de caractères [privée]
    private String rapportIa;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `Prescription` sans paramètre
    public Prescription() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }
  
    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinId`
    public UUID getMedecinId() { return medecinId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinId`
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `numeroPrescription`
    public String getNumeroPrescription() { return numeroPrescription; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `numeroPrescription`
    public void setNumeroPrescription(String numeroPrescription) { this.numeroPrescription = numeroPrescription; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutPrescription getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutPrescription statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `detailsMedicaments`
    public String getDetailsMedicaments() { return detailsMedicaments; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `detailsMedicaments`
    public void setDetailsMedicaments(String detailsMedicaments) { this.detailsMedicaments = detailsMedicaments; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `rapportIa`
    public String getRapportIa() { return rapportIa; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `rapportIa`
    public void setRapportIa(String rapportIa) { this.rapportIa = rapportIa; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
