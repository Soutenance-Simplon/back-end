// Déclaration du package Java : `com.diamyaraam.rdv.entity`
package com.diamyaraam.rdv.entity;

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
// Table SQL associée à l'entité : table « salle_teleconsultation », schéma « rdv_schema »
@Table(name = "salle_teleconsultation", schema = "rdv_schema")
// Déclaration de la classe `SalleTeleconsultation`
public class SalleTeleconsultation {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Relation un-à-un avec une autre entité
    @OneToOne(fetch = FetchType.LAZY)
    // Colonne de clé étrangère de la relation : colonne « rendez_vous_id »
    @JoinColumn(name = "rendez_vous_id", unique = true, nullable = false)
    // Attribut `rendezVous` de type RendezVous [privée]
    private RendezVous rendezVous;

    // Colonne SQL associée à l'attribut : nom « token_patient », obligatoire (NOT NULL), valeur unique, longueur max 100
    @Column(name = "token_patient", unique = true, nullable = false, length = 100)
    // Attribut `tokenPatient` de type chaîne de caractères [privée]
    private String tokenPatient;

    // Colonne SQL associée à l'attribut : nom « token_medecin », obligatoire (NOT NULL), valeur unique, longueur max 100
    @Column(name = "token_medecin", unique = true, nullable = false, length = 100)
    // Attribut `tokenMedecin` de type chaîne de caractères [privée]
    private String tokenMedecin;

    // Déclaration de l'énumération `StatutSalle`
    public enum StatutSalle { EN_ATTENTE, ACTIVE, TERMINEE, EXPIREE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut", length = 15, nullable = false)
    // Attribut `statut` (statut) de type StatutSalle [privée] ; valeur initiale : la valeur de StatutSalle.EN_ATTENTE
    private StatutSalle statut = StatutSalle.EN_ATTENTE;

    // Colonne SQL associée à l'attribut : nom « patient_connecte », obligatoire (NOT NULL)
    @Column(name = "patient_connecte", nullable = false)
    // Attribut `patientConnecte` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean patientConnecte = false;

    // Colonne SQL associée à l'attribut : nom « medecin_connecte », obligatoire (NOT NULL)
    @Column(name = "medecin_connecte", nullable = false)
    // Attribut `medecinConnecte` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean medecinConnecte = false;

    // Colonne SQL associée à l'attribut : nom « url_salle », longueur max 500
    @Column(name = "url_salle", length = 500)
    // Attribut `urlSalle` de type chaîne de caractères [privée]
    private String urlSalle;

    // Colonne SQL associée à l'attribut : nom « tokens_expire_at », obligatoire (NOT NULL)
    @Column(name = "tokens_expire_at", nullable = false)
    // Attribut `tokensExpireAt` de type date-heure [privée]
    private LocalDateTime tokensExpireAt;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `SalleTeleconsultation` sans paramètre
    public SalleTeleconsultation() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `rendezVous`
    public RendezVous getRendezVous() { return rendezVous; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `rendezVous`
    public void setRendezVous(RendezVous rendezVous) { this.rendezVous = rendezVous; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tokenPatient`
    public String getTokenPatient() { return tokenPatient; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tokenPatient`
    public void setTokenPatient(String tokenPatient) { this.tokenPatient = tokenPatient; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tokenMedecin`
    public String getTokenMedecin() { return tokenMedecin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tokenMedecin`
    public void setTokenMedecin(String tokenMedecin) { this.tokenMedecin = tokenMedecin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statut`
    public StatutSalle getStatut() { return statut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statut`
    public void setStatut(StatutSalle statut) { this.statut = statut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `patientConnecte`
    public Boolean getPatientConnecte() { return patientConnecte; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `patientConnecte`
    public void setPatientConnecte(Boolean patientConnecte) { this.patientConnecte = patientConnecte; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinConnecte`
    public Boolean getMedecinConnecte() { return medecinConnecte; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinConnecte`
    public void setMedecinConnecte(Boolean medecinConnecte) { this.medecinConnecte = medecinConnecte; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `urlSalle`
    public String getUrlSalle() { return urlSalle; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `urlSalle`
    public void setUrlSalle(String urlSalle) { this.urlSalle = urlSalle; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tokensExpireAt`
    public LocalDateTime getTokensExpireAt() { return tokensExpireAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tokensExpireAt`
    public void setTokensExpireAt(LocalDateTime tokensExpireAt) { this.tokensExpireAt = tokensExpireAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
