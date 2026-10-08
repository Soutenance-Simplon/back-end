// Déclaration du package Java : `com.diamyaraam.patient.dto`
package com.diamyaraam.patient.dto;

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclaration de la classe `MembreFamilleDto` (rôle : transporte des données entre couches)
public class MembreFamilleDto {
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;
    // Attribut `parentUserId` de type identifiant UUID [privée]
    private UUID parentUserId;
    // Attribut `enfantUserId` de type identifiant UUID [privée]
    private UUID enfantUserId;
    // Attribut `nom` (nom) de type chaîne de caractères [privée]
    private String nom;
    // Attribut `prenom` (prénom) de type chaîne de caractères [privée]
    private String prenom;
    // Attribut `dateNaissance` (date de naissance) de type date [privée]
    private LocalDate dateNaissance;
    // Attribut `genre` (genre (M ou F)) de type chaîne de caractères [privée]
    private String genre;
    // Attribut `lienParente` de type chaîne de caractères [privée]
    private String lienParente;

    // Getters and Setters
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `parentUserId`
    public UUID getParentUserId() { return parentUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `parentUserId`
    public void setParentUserId(UUID parentUserId) { this.parentUserId = parentUserId; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `enfantUserId`
    public UUID getEnfantUserId() { return enfantUserId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `enfantUserId`
    public void setEnfantUserId(UUID enfantUserId) { this.enfantUserId = enfantUserId; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `nom`
    public String getNom() { return nom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nom`
    public void setNom(String nom) { this.nom = nom; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `prenom`
    public String getPrenom() { return prenom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `prenom`
    public void setPrenom(String prenom) { this.prenom = prenom; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `dateNaissance`
    public LocalDate getDateNaissance() { return dateNaissance; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateNaissance`
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `genre`
    public String getGenre() { return genre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `genre`
    public void setGenre(String genre) { this.genre = genre; }
    // Accesseur (getter) : renvoie la valeur de l'attribut `lienParente`
    public String getLienParente() { return lienParente; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lienParente`
    public void setLienParente(String lienParente) { this.lienParente = lienParente; }
}
