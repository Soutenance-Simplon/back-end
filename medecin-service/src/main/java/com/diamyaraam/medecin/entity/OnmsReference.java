// Déclaration du package Java : `com.diamyaraam.medecin.entity`
package com.diamyaraam.medecin.entity;

// Import de la classe `JsonIgnoreProperties` (paquet com.fasterxml.jackson.annotation)
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;

/**
 * Base de référence officielle ONMS (RM031-RM038)
 */
@Entity
// Table SQL associée à l'entité : table « onms_reference », schéma « medecin_schema »
@Table(name = "onms_reference", schema = "medecin_schema")
// Ignore certaines propriétés JSON
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
// Déclaration de la classe `OnmsReference`
public class OnmsReference {

    // Clé primaire de l'entité
    @Id
    // Valeur de la clé primaire générée automatiquement
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Attribut `id` (identifiant unique) de type entier long [privée]
    private Long id;

    // Colonne SQL associée à l'attribut : nom « numero_ordre », obligatoire (NOT NULL), valeur unique, longueur max 50
    @Column(name = "numero_ordre", unique = true, nullable = false, length = 50)
    // Attribut `numeroOrdre` (numéro d'ordre officiel (ONMS)) de type chaîne de caractères [privée]
    private String numeroOrdre;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 150
    @Column(nullable = false, length = 150)
    // Attribut `nom` (nom) de type chaîne de caractères [privée]
    private String nom;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 150
    @Column(nullable = false, length = 150)
    // Attribut `prenom` (prénom) de type chaîne de caractères [privée]
    private String prenom;

    // Colonne SQL associée à l'attribut : obligatoire (NOT NULL), longueur max 100
    @Column(nullable = false, length = 100)
    // Attribut `specialite` (spécialité médicale) de type chaîne de caractères [privée]
    private String specialite;

    // Colonne SQL associée à l'attribut : nom « date_inscription »
    @Column(name = "date_inscription")
    // Attribut `dateInscription` de type java.time.LocalDate [privée]
    private java.time.LocalDate dateInscription;

    // Déclaration de l'énumération `StatutProfessionnel`
    public enum StatutProfessionnel { ACTIF, SUSPENDU, RADIE, RETRAITE }

    // Enregistre l'énumération sous forme de texte en base
    @Enumerated(EnumType.STRING)
    // Colonne SQL associée à l'attribut : nom « statut_professionnel », obligatoire (NOT NULL), longueur max 20
    @Column(name = "statut_professionnel", length = 20, nullable = false)
    // Attribut `statutProfessionnel` de type StatutProfessionnel [privée] ; valeur initiale : la valeur de StatutProfessionnel.ACTIF
    private StatutProfessionnel statutProfessionnel = StatutProfessionnel.ACTIF;

    // Colonne SQL associée à l'attribut : longueur max 200
    @Column(length = 200)
    // Attribut `etablissement` de type chaîne de caractères [privée]
    private String etablissement;

    // Colonne SQL associée à l'attribut : longueur max 100
    @Column(length = 100)
    // Attribut `region` de type chaîne de caractères [privée]
    private String region;

    // Colonne SQL associée à l'attribut : longueur max 50
    @Column(length = 50)
    // Attribut `section` de type chaîne de caractères [privée]
    private String section;

    // Colonne SQL associée à l'attribut : longueur max 20
    @Column(length = 20)
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Colonne SQL associée à l'attribut : nom « derniere_synchro »
    @Column(name = "derniere_synchro")
    // Attribut `derniereSynchro` de type date-heure [privée]
    private LocalDateTime derniereSynchro;

    // Constructeur de `OnmsReference` sans paramètre
    public OnmsReference() {}

    // Méthode `estAutoriseAExercer` (publique) — sans paramètre ; retourne : booléen
    public boolean estAutoriseAExercer() {
        // Retourne `StatutProfessionnel.ACTIF.equals(this.statutProfessionnel)`
        return StatutProfessionnel.ACTIF.equals(this.statutProfessionnel);
    }

    // Getters / Setters
    public Long getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(Long id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `numeroOrdre`
    public String getNumeroOrdre() { return numeroOrdre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `numeroOrdre`
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nom`
    public String getNom() { return nom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nom`
    public void setNom(String nom) { this.nom = nom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `prenom`
    public String getPrenom() { return prenom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `prenom`
    public void setPrenom(String prenom) { this.prenom = prenom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `specialite`
    public String getSpecialite() { return specialite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `specialite`
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateInscription`
    public java.time.LocalDate getDateInscription() { return dateInscription; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateInscription`
    public void setDateInscription(java.time.LocalDate dateInscription) { this.dateInscription = dateInscription; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statutProfessionnel`
    public StatutProfessionnel getStatutProfessionnel() { return statutProfessionnel; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statutProfessionnel`
    public void setStatutProfessionnel(StatutProfessionnel statutProfessionnel) { this.statutProfessionnel = statutProfessionnel; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `etablissement`
    public String getEtablissement() { return etablissement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `etablissement`
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `region`
    public String getRegion() { return region; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `region`
    public void setRegion(String region) { this.region = region; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `section`
    public String getSection() { return section; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `section`
    public void setSection(String section) { this.section = section; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `derniereSynchro`
    public LocalDateTime getDerniereSynchro() { return derniereSynchro; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `derniereSynchro`
    public void setDerniereSynchro(LocalDateTime derniereSynchro) { this.derniereSynchro = derniereSynchro; }
}
