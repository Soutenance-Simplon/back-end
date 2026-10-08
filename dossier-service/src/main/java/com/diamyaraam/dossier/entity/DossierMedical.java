// Déclaration du package Java : `com.diamyaraam.dossier.entity`
package com.diamyaraam.dossier.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UpdateTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UpdateTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclare la classe comme entité JPA persistée en base de données
@Entity
// Table SQL associée à l'entité : table « dossier_medical », schéma « dossier_schema »
@Table(name = "dossier_medical", schema = "dossier_schema")
// Déclaration de la classe `DossierMedical`
public class DossierMedical {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'un UUID (non prédictible)
    @UuidGenerator
    // Colonne SQL associée à l'attribut : non modifiable après création, type SQL uuid
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // UUID reference to Patient in patient-service
    @Column(name = "patient_id", nullable = false, unique = true)
    // Attribut `patientId` (identifiant du patient) de type identifiant UUID [privée]
    private UUID patientId;

    // Déclaration de l'énumération `GroupeSanguin`
    public enum GroupeSanguin {
        // Constante(s) de l'énumération : A_PLUS, A_MOINS
        A_PLUS("A+"), A_MOINS("A-"),
        // Constante(s) de l'énumération : B_PLUS, B_MOINS
        B_PLUS("B+"), B_MOINS("B-"),
        // Constante(s) de l'énumération : AB_PLUS, AB_MOINS
        AB_PLUS("AB+"), AB_MOINS("AB-"),
        // Constante(s) de l'énumération : O_PLUS, O_MOINS
        O_PLUS("O+"), O_MOINS("O-");

        // Attribut `label` de type chaîne de caractères — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
        private final String label;
        // Constructeur de `GroupeSanguin` — paramètres : `label` (chaîne de caractères) (injection des dépendances par Spring)
        GroupeSanguin(String label) { this.label = label; }
        // Accesseur (getter) : renvoie la valeur de l'attribut `label`
        public String getLabel() { return label; }
    }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « groupe_sanguin », longueur max 10
    @Column(name = "groupe_sanguin", length = 10)
    // Attribut `groupeSanguin` (groupe sanguin) de type GroupeSanguin [privée]
    private GroupeSanguin groupeSanguin;

    // Colonne SQL associée à l'attribut
    @Column(precision = 5)
    // Attribut `taille` de type nombre décimal [privée]
    private Double taille;

    // Colonne SQL associée à l'attribut
    @Column(precision = 5)
    // Attribut `poids` de type nombre décimal [privée]
    private Double poids;

    // Colonne SQL associée à l'attribut : nom « groupe_sanguin_valide », obligatoire (NOT NULL)
    @Column(name = "groupe_sanguin_valide", nullable = false)
    // Attribut `groupeSanguinValide` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean groupeSanguinValide = false;

    // Déclaration de l'énumération `StatutDossier`
    public enum StatutDossier { ACTIF, ARCHIVE, SUSPENDU }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut_dossier », obligatoire (NOT NULL), longueur max 15
    @Column(name = "statut_dossier", length = 15, nullable = false)
    // Attribut `statutDossier` de type StatutDossier [privée] ; valeur initiale : la valeur de StatutDossier.ACTIF
    private StatutDossier statutDossier = StatutDossier.ACTIF;

    // Colonne SQL associée à l'attribut : nom « code_qr_securise », valeur unique, longueur max 100
    @Column(name = "code_qr_securise", unique = true, length = 100)
    // Attribut `codeQrSecurise` de type chaîne de caractères [privée]
    private String codeQrSecurise;

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

    // Constructeur de `DossierMedical` sans paramètre
    public DossierMedical() {}

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `patientId`
    public UUID getPatientId() { return patientId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `patientId`
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `groupeSanguin`
    public GroupeSanguin getGroupeSanguin() { return groupeSanguin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `groupeSanguin`
    public void setGroupeSanguin(GroupeSanguin groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `taille`
    public Double getTaille() { return taille; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `taille`
    public void setTaille(Double taille) { this.taille = taille; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `poids`
    public Double getPoids() { return poids; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `poids`
    public void setPoids(Double poids) { this.poids = poids; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `groupeSanguinValide`
    public Boolean getGroupeSanguinValide() { return groupeSanguinValide; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `groupeSanguinValide`
    public void setGroupeSanguinValide(Boolean groupeSanguinValide) { this.groupeSanguinValide = groupeSanguinValide; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statutDossier`
    public StatutDossier getStatutDossier() { return statutDossier; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statutDossier`
    public void setStatutDossier(StatutDossier statutDossier) { this.statutDossier = statutDossier; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `codeQrSecurise`
    public String getCodeQrSecurise() { return codeQrSecurise; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `codeQrSecurise`
    public void setCodeQrSecurise(String codeQrSecurise) { this.codeQrSecurise = codeQrSecurise; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
