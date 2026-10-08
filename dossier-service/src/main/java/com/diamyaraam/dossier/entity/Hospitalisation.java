// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « dossier_hospitalisation », schéma « dossier_schema »
@Table(name = "dossier_hospitalisation", schema = "dossier_schema")
// Déclaration de la classe `Hospitalisation`
public class Hospitalisation {

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

    // Colonne SQL associée à l'attribut : nom « etablissement », obligatoire (NOT NULL), longueur max 200
    @Column(name = "etablissement", nullable = false, length = 200)
    // Attribut `etablissement` de type chaîne de caractères [privée]
    private String etablissement;

    // Colonne SQL associée à l'attribut : nom « service_hospitalier », longueur max 150
    @Column(name = "service_hospitalier", length = 150)
    // Attribut `serviceHospitalier` de type chaîne de caractères [privée]
    private String serviceHospitalier;

    // Colonne SQL associée à l'attribut : nom « motif_admission », obligatoire (NOT NULL), longueur max 250
    @Column(name = "motif_admission", nullable = false, length = 250)
    // Attribut `motifAdmission` de type chaîne de caractères [privée]
    private String motifAdmission;

    // Colonne SQL associée à l'attribut : nom « date_entree », obligatoire (NOT NULL)
    @Column(name = "date_entree", nullable = false)
    // Attribut `dateEntree` de type date [privée]
    private LocalDate dateEntree;

    // Colonne SQL associée à l'attribut : nom « date_sortie »
    @Column(name = "date_sortie")
    // Attribut `dateSortie` de type date [privée]
    private LocalDate dateSortie;

    // Colonne SQL associée à l'attribut : nom « compte_rendu », type SQL TEXT
    @Column(name = "compte_rendu", columnDefinition = "TEXT")
    // Attribut `compteRendu` de type chaîne de caractères [privée]
    private String compteRendu;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `Hospitalisation` sans paramètre
    public Hospitalisation() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `etablissement`
    public String getEtablissement() { return etablissement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `etablissement`
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `serviceHospitalier`
    public String getServiceHospitalier() { return serviceHospitalier; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `serviceHospitalier`
    public void setServiceHospitalier(String serviceHospitalier) { this.serviceHospitalier = serviceHospitalier; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `motifAdmission`
    public String getMotifAdmission() { return motifAdmission; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `motifAdmission`
    public void setMotifAdmission(String motifAdmission) { this.motifAdmission = motifAdmission; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateEntree`
    public LocalDate getDateEntree() { return dateEntree; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateEntree`
    public void setDateEntree(LocalDate dateEntree) { this.dateEntree = dateEntree; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateSortie`
    public LocalDate getDateSortie() { return dateSortie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateSortie`
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `compteRendu`
    public String getCompteRendu() { return compteRendu; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `compteRendu`
    public void setCompteRendu(String compteRendu) { this.compteRendu = compteRendu; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
