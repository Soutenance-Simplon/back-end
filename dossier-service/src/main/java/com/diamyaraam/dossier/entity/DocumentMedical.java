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
// Table SQL associée à l'entité : table « dossier_document_medical », schéma « dossier_schema »
@Table(name = "dossier_document_medical", schema = "dossier_schema")
// Déclaration de la classe `DocumentMedical`
public class DocumentMedical {

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

    // Colonne SQL associée à l'attribut : nom « titre », obligatoire (NOT NULL), longueur max 200
    @Column(name = "titre", nullable = false, length = 200)
    // Attribut `titre` (titre) de type chaîne de caractères [privée]
    private String titre;

    // Déclaration de l'énumération `TypeDocument`
    public enum TypeDocument {
        // Constante(s) de l'énumération : ORDONNANCE
        ORDONNANCE,
        // Constante(s) de l'énumération : ANALYSES_LABORATOIRE
        ANALYSES_LABORATOIRE,
        // Constante(s) de l'énumération : IMAGERIE_RADIOLOGIE
        IMAGERIE_RADIOLOGIE,
        // Constante(s) de l'énumération : COMPTE_RENDU
        COMPTE_RENDU,
        // Constante(s) de l'énumération : CERTIFICAT_MEDICAL
        CERTIFICAT_MEDICAL,
        // Constante(s) de l'énumération : AUTRE
        AUTRE
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « type_document », obligatoire (NOT NULL), longueur max 50
    @Column(name = "type_document", nullable = false, length = 50)
    // Attribut `typeDocument` de type TypeDocument [privée]
    private TypeDocument typeDocument;

    // Colonne SQL associée à l'attribut : nom « file_url », obligatoire (NOT NULL), longueur max 500
    @Column(name = "file_url", nullable = false, length = 500)
    // Attribut `fileUrl` de type chaîne de caractères [privée]
    private String fileUrl;

    // Colonne SQL associée à l'attribut : nom « file_type », longueur max 50
    @Column(name = "file_type", length = 50)
    // Attribut `fileType` de type chaîne de caractères [privée]
    private String fileType; // pdf, image/png, etc.

    // Colonne SQL associée à l'attribut : nom « taille_octets »
    @Column(name = "taille_octets")
    // Attribut `tailleOctets` de type entier long [privée]
    private Long tailleOctets;

    // Horodatage rempli automatiquement à la création de la ligne
    @CreationTimestamp
    // Colonne SQL associée à l'attribut : nom « created_at », non modifiable après création
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Constructeur de `DocumentMedical` sans paramètre
    public DocumentMedical() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dossierMedical`
    public DossierMedical getDossierMedical() { return dossierMedical; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dossierMedical`
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `titre`
    public String getTitre() { return titre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `titre`
    public void setTitre(String titre) { this.titre = titre; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeDocument`
    public TypeDocument getTypeDocument() { return typeDocument; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeDocument`
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `fileUrl`
    public String getFileUrl() { return fileUrl; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `fileUrl`
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `fileType`
    public String getFileType() { return fileType; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `fileType`
    public void setFileType(String fileType) { this.fileType = fileType; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tailleOctets`
    public Long getTailleOctets() { return tailleOctets; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tailleOctets`
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
