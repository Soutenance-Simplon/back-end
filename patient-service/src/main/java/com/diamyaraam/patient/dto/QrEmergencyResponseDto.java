// Déclaration du package Java : `com.diamyaraam.patient.dto`
package com.diamyaraam.patient.dto;

// Import de la classe `List` (paquet java.util)
import java.util.List;

// Déclaration de la classe `QrEmergencyResponseDto` (rôle : transporte des données entre couches)
public class QrEmergencyResponseDto {

    // Attribut `viewMode` de type chaîne de caractères [privée]
    private String viewMode; // "CITOYEN_PUBLIC" or "MEDECIN_AUTHENTIFIE"
    // Attribut `nom` (nom) de type chaîne de caractères [privée]
    private String nom;
    // Attribut `prenom` (prénom) de type chaîne de caractères [privée]
    private String prenom;
    // Attribut `photoUrl` de type chaîne de caractères [privée]
    private String photoUrl;
    // Attribut `telephonePatient` de type chaîne de caractères [privée]
    private String telephonePatient;
    // Attribut `adresse` (adresse) de type chaîne de caractères [privée]
    private String adresse;
    // Attribut `ville` (ville) de type chaîne de caractères [privée]
    private String ville;
    // Attribut `dateNaissance` (date de naissance) de type chaîne de caractères [privée]
    private String dateNaissance;
    // Attribut `contactUrgenceNom` (nom du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceNom;
    // Attribut `contactUrgenceTelephone` (téléphone du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceTelephone;
    // Attribut `contactUrgenceLien` (lien de parenté du contact d'urgence) de type chaîne de caractères [privée]
    private String contactUrgenceLien;

    // Champs médicaux supplémentaires uniquement pour VUE_MEDECIN
    private String groupeSanguin;
    // Attribut `allergies` de type liste de chaîne de caractères [privée]
    private List<String> allergies;
    // Attribut `antecedents` de type liste de chaîne de caractères [privée]
    private List<String> antecedents;
    // Attribut `maladiesChroniques` de type liste de chaîne de caractères [privée]
    private List<String> maladiesChroniques;
    // Attribut `traitementsEnCours` de type liste de chaîne de caractères [privée]
    private List<String> traitementsEnCours;

    // Constructeur de `QrEmergencyResponseDto` sans paramètre
    public QrEmergencyResponseDto() {}

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephonePatient`
    public String getTelephonePatient() { return telephonePatient; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephonePatient`
    public void setTelephonePatient(String telephonePatient) { this.telephonePatient = telephonePatient; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `adresse`
    public String getAdresse() { return adresse; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `adresse`
    public void setAdresse(String adresse) { this.adresse = adresse; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `ville`
    public String getVille() { return ville; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `ville`
    public void setVille(String ville) { this.ville = ville; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateNaissance`
    public String getDateNaissance() { return dateNaissance; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateNaissance`
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

    // Getters and Setters
    public String getViewMode() { return viewMode; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `viewMode`
    public void setViewMode(String viewMode) { this.viewMode = viewMode; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nom`
    public String getNom() { return nom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nom`
    public void setNom(String nom) { this.nom = nom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `prenom`
    public String getPrenom() { return prenom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `prenom`
    public void setPrenom(String prenom) { this.prenom = prenom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoUrl`
    public String getPhotoUrl() { return photoUrl; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoUrl`
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceNom`
    public String getContactUrgenceNom() { return contactUrgenceNom; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceNom`
    public void setContactUrgenceNom(String contactUrgenceNom) { this.contactUrgenceNom = contactUrgenceNom; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceTelephone`
    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceTelephone`
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) { this.contactUrgenceTelephone = contactUrgenceTelephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `contactUrgenceLien`
    public String getContactUrgenceLien() { return contactUrgenceLien; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `contactUrgenceLien`
    public void setContactUrgenceLien(String contactUrgenceLien) { this.contactUrgenceLien = contactUrgenceLien; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `groupeSanguin`
    public String getGroupeSanguin() { return groupeSanguin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `groupeSanguin`
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `allergies`
    public List<String> getAllergies() { return allergies; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `allergies`
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `antecedents`
    public List<String> getAntecedents() { return antecedents; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `antecedents`
    public void setAntecedents(List<String> antecedents) { this.antecedents = antecedents; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `maladiesChroniques`
    public List<String> getMaladiesChroniques() { return maladiesChroniques; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `maladiesChroniques`
    public void setMaladiesChroniques(List<String> maladiesChroniques) { this.maladiesChroniques = maladiesChroniques; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `traitementsEnCours`
    public List<String> getTraitementsEnCours() { return traitementsEnCours; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `traitementsEnCours`
    public void setTraitementsEnCours(List<String> traitementsEnCours) { this.traitementsEnCours = traitementsEnCours; }
}
