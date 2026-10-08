// Déclaration du package Java : `com.diamyaraam.notification.entity`
package com.diamyaraam.notification.entity;

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
// Table SQL associée à l'entité : table « notification », schéma « notification_schema »
@Table(name = "notification", schema = "notification_schema")
// Déclaration de la classe `Notification`
public class Notification {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // UUID reference to User in auth-service
    @Column(name = "destinataire_id", nullable = false)
    // Attribut `destinataireId` de type identifiant UUID [privée]
    private UUID destinataireId;

    // Déclaration de l'énumération `TypeNotification`
    public enum TypeNotification {
        // Constante(s) de l'énumération : DEMANDE_RDV, RDV_ACCEPTE, RDV_CONFIRME, RDV_ANNULE
        DEMANDE_RDV, RDV_ACCEPTE, RDV_CONFIRME, RDV_ANNULE,
        // Constante(s) de l'énumération : RDV_RAPPEL_24H, RDV_RAPPEL_1H, PATIENT_ABSENT
        RDV_RAPPEL_24H, RDV_RAPPEL_1H, PATIENT_ABSENT,
        // Constante(s) de l'énumération : ACCES_DOSSIER_DEMANDE, ACCES_DOSSIER_ACCORDE
        ACCES_DOSSIER_DEMANDE, ACCES_DOSSIER_ACCORDE,
        // Constante(s) de l'énumération : ACCES_DOSSIER_CONSULTE, ACCES_DOSSIER_URGENCE, SIGNALEMENT_ACCES_ABUSIF
        ACCES_DOSSIER_CONSULTE, ACCES_DOSSIER_URGENCE, SIGNALEMENT_ACCES_ABUSIF,
        // Constante(s) de l'énumération : NOUVELLE_PRESCRIPTION, OTP_ENVOYE, COMPTE_BLOQUE, CHANGEMENT_STATUT
        NOUVELLE_PRESCRIPTION, OTP_ENVOYE, COMPTE_BLOQUE, CHANGEMENT_STATUT
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type », obligatoire (NOT NULL), longueur max 30
    @Column(name = "type", length = 30, nullable = false)
    // Attribut `type` (type) de type TypeNotification [privée]
    private TypeNotification type;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 200
    @Column(nullable = false, length = 200)
    // Attribut `titre` (titre) de type chaîne de caractères [privée]
    private String titre;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), type SQL TEXT
    @Column(nullable = false, columnDefinition = "TEXT")
    // Attribut `message` (message) de type chaîne de caractères [privée]
    private String message;

    // Colonne SQL associée à l'attribut : nom « rendez_vous_id »
    @Column(name = "rendez_vous_id")
    // Attribut `rendezVousId` de type identifiant UUID [privée]
    private UUID rendezVousId;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL)
    @Column(nullable = false)
    // Attribut `lue` (indicateur de lecture) de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean lue = false;

    // Colonne SQL associée à l'attribut : nom « date_lecture »
    @Column(name = "date_lecture")
    // Attribut `dateLecture` de type date-heure [privée]
    private LocalDateTime dateLecture;

    // Déclaration de l'énumération `Canal`
    public enum Canal { IN_APP, SMS, EMAIL }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 10
    @Column(length = 10, nullable = false)
    // Attribut `canal` de type Canal [privée] ; valeur initiale : la valeur de Canal.IN_APP
    private Canal canal = Canal.IN_APP;

    // Colonne SQL associée à l'attribut : nom « envoyee », obligatoire (NOT NULL)
    @Column(name = "envoyee", nullable = false)
    // Attribut `envoyee` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean envoyee = false;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `Notification` sans paramètre
    public Notification() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `destinataireId`
    public UUID getDestinataireId() { return destinataireId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `destinataireId`
    public void setDestinataireId(UUID destinataireId) { this.destinataireId = destinataireId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `type`
    public TypeNotification getType() { return type; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `type`
    public void setType(TypeNotification type) { this.type = type; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `titre`
    public String getTitre() { return titre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `titre`
    public void setTitre(String titre) { this.titre = titre; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `message`
    public String getMessage() { return message; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `message`
    public void setMessage(String message) { this.message = message; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `rendezVousId`
    public UUID getRendezVousId() { return rendezVousId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `rendezVousId`
    public void setRendezVousId(UUID rendezVousId) { this.rendezVousId = rendezVousId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lue`
    public Boolean getLue() { return lue; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lue`
    public void setLue(Boolean lue) { this.lue = lue; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateLecture`
    public LocalDateTime getDateLecture() { return dateLecture; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateLecture`
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `canal`
    public Canal getCanal() { return canal; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `canal`
    public void setCanal(Canal canal) { this.canal = canal; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `envoyee`
    public Boolean getEnvoyee() { return envoyee; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `envoyee`
    public void setEnvoyee(Boolean envoyee) { this.envoyee = envoyee; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
