// Déclaration du package Java : `com.diamyaraam.rdv.entity`
package com.diamyaraam.rdv.entity;

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

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « rendez_vous », schéma « rdv_schema »
@Table(name = "rendez_vous", schema = "rdv_schema")
// Déclaration de la classe `RendezVous`
public class RendezVous {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // UUID references to other services
    @Column(name = "patient_id", nullable = false)
    // Attribut `patientId` (identifiant du patient) de type identifiant UUID [privée]
    private UUID patientId;

    // Colonne SQL associée à l'attribut : nom « medecin_id », obligatoire (NOT NULL)
    @Column(name = "medecin_id", nullable = false)
    // Attribut `medecinId` (identifiant du médecin) de type identifiant UUID [privée]
    private UUID medecinId;

    // Colonne SQL associée à l'attribut : nom « dossier_medical_id »
    @Column(name = "dossier_medical_id")
    // Attribut `dossierMedicalId` de type identifiant UUID [privée]
    private UUID dossierMedicalId;

    // Colonne SQL associée à l'attribut : nom « creneau_id »
    @Column(name = "creneau_id")
    // Attribut `creneauId` de type identifiant UUID [privée]
    private UUID creneauId;

    // Colonne SQL associée à l'attribut : nom « date_heure_souhaitee », obligatoire (NOT NULL)
    @Column(name = "date_heure_souhaitee", nullable = false)
    // Attribut `dateHeureSouhaitee` de type date-heure [privée]
    private LocalDateTime dateHeureSouhaitee;

    // Colonne SQL associée à l'attribut : nom « date_heure_confirmee »
    @Column(name = "date_heure_confirmee")
    // Attribut `dateHeureConfirmee` de type date-heure [privée]
    private LocalDateTime dateHeureConfirmee;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), type SQL TEXT
    @Column(nullable = false, columnDefinition = "TEXT")
    // Attribut `motif` (motif) de type chaîne de caractères [privée]
    private String motif;

    // Colonne SQL associée à l'attribut : nom « est_urgent », obligatoire (NOT NULL)
    @Column(name = "est_urgent", nullable = false)
    // Attribut `estUrgent` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean estUrgent = false;

    // Déclaration de l'énumération `TypeConsultation`
    public enum TypeConsultation { PRESENTIELLE, TELECONSULTATION, DOMICILE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_consultation », obligatoire (NOT NULL), longueur max 20
    @Column(name = "type_consultation", length = 20, nullable = false)
    // Attribut `typeConsultation` de type TypeConsultation [privée] ; valeur initiale : la valeur de TypeConsultation.TELECONSULTATION
    private TypeConsultation typeConsultation = TypeConsultation.TELECONSULTATION;


    // Déclaration de l'énumération `StatutRendezVous`
    public enum StatutRendezVous {
        // Constante(s) de l'énumération : EN_ATTENTE, ACCEPTE, CONFIRME, EN_COURS, TERMINE, ANNULE, ABSENT
        EN_ATTENTE, ACCEPTE, CONFIRME, EN_COURS, TERMINE, ANNULE, ABSENT
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutRendezVous [privée] ; valeur initiale : la valeur de StatutRendezVous.EN_ATTENTE
    private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;

    // Colonne SQL associée à l'attribut : nom « tarif_applique »
    @Column(name = "tarif_applique", precision = 10, scale = 2)
    // Attribut `tarifApplique` de type montant décimal précis [privée]
    private BigDecimal tarifApplique;

    // Colonne SQL associée à l'attribut : nom « paiement_valide », obligatoire (NOT NULL)
    @Column(name = "paiement_valide", nullable = false)
    // Attribut `paiementValide` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean paiementValide = false;

    // Colonne SQL associée à l'attribut : nom « reference_paiement », longueur max 100
    @Column(name = "reference_paiement", length = 100)
    // Attribut `referencePaiement` de type chaîne de caractères [privée]
    private String referencePaiement;

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

    // Constructeur de `RendezVous` sans paramètre
    public RendezVous() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `patientId`
    public UUID getPatientId() { return patientId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `patientId`
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinId`
    public UUID getMedecinId() { return medecinId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinId`
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedicalId`
    public UUID getDossierMedicalId() { return dossierMedicalId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedicalId`
    public void setDossierMedicalId(UUID dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `creneauId`
    public UUID getCreneauId() { return creneauId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `creneauId`
    public void setCreneauId(UUID creneauId) { this.creneauId = creneauId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateHeureSouhaitee`
    public LocalDateTime getDateHeureSouhaitee() { return dateHeureSouhaitee; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateHeureSouhaitee`
    public void setDateHeureSouhaitee(LocalDateTime dateHeureSouhaitee) { this.dateHeureSouhaitee = dateHeureSouhaitee; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateHeureConfirmee`
    public LocalDateTime getDateHeureConfirmee() { return dateHeureConfirmee; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateHeureConfirmee`
    public void setDateHeureConfirmee(LocalDateTime dateHeureConfirmee) { this.dateHeureConfirmee = dateHeureConfirmee; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `motif`
    public String getMotif() { return motif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `motif`
    public void setMotif(String motif) { this.motif = motif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `estUrgent`
    public Boolean getEstUrgent() { return estUrgent; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `estUrgent`
    public void setEstUrgent(Boolean estUrgent) { this.estUrgent = estUrgent; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeConsultation`
    public TypeConsultation getTypeConsultation() { return typeConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeConsultation`
    public void setTypeConsultation(TypeConsultation typeConsultation) { this.typeConsultation = typeConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutRendezVous getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutRendezVous statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tarifApplique`
    public BigDecimal getTarifApplique() { return tarifApplique; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tarifApplique`
    public void setTarifApplique(BigDecimal tarifApplique) { this.tarifApplique = tarifApplique; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `paiementValide`
    public Boolean getPaiementValide() { return paiementValide; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `paiementValide`
    public void setPaiementValide(Boolean paiementValide) { this.paiementValide = paiementValide; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `referencePaiement`
    public String getReferencePaiement() { return referencePaiement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `referencePaiement`
    public void setReferencePaiement(String referencePaiement) { this.referencePaiement = referencePaiement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
