package com.diamyaraam.patient.dto;

import java.util.List;

public class QrEmergencyResponseDto {

    private String viewMode; // "CITOYEN_PUBLIC" or "MEDECIN_AUTHENTIFIE"
    private String nom;
    private String prenom;
    private String photoUrl;
    private String telephonePatient;
    private String adresse;
    private String ville;
    private String dateNaissance;
    private String contactUrgenceNom;
    private String contactUrgenceTelephone;
    private String contactUrgenceLien;

    // Champs médicaux supplémentaires uniquement pour VUE_MEDECIN
    private String groupeSanguin;
    private List<String> allergies;
    private List<String> antecedents;
    private List<String> maladiesChroniques;
    private List<String> traitementsEnCours;

    public QrEmergencyResponseDto() {}

    public String getTelephonePatient() { return telephonePatient; }
    public void setTelephonePatient(String telephonePatient) { this.telephonePatient = telephonePatient; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String dateNaissance) { this.dateNaissance = dateNaissance; }

    // Getters and Setters
    public String getViewMode() { return viewMode; }
    public void setViewMode(String viewMode) { this.viewMode = viewMode; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getContactUrgenceNom() { return contactUrgenceNom; }
    public void setContactUrgenceNom(String contactUrgenceNom) { this.contactUrgenceNom = contactUrgenceNom; }

    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) { this.contactUrgenceTelephone = contactUrgenceTelephone; }

    public String getContactUrgenceLien() { return contactUrgenceLien; }
    public void setContactUrgenceLien(String contactUrgenceLien) { this.contactUrgenceLien = contactUrgenceLien; }

    public String getGroupeSanguin() { return groupeSanguin; }
    public void setGroupeSanguin(String groupeSanguin) { this.groupeSanguin = groupeSanguin; }

    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }

    public List<String> getAntecedents() { return antecedents; }
    public void setAntecedents(List<String> antecedents) { this.antecedents = antecedents; }

    public List<String> getMaladiesChroniques() { return maladiesChroniques; }
    public void setMaladiesChroniques(List<String> maladiesChroniques) { this.maladiesChroniques = maladiesChroniques; }

    public List<String> getTraitementsEnCours() { return traitementsEnCours; }
    public void setTraitementsEnCours(List<String> traitementsEnCours) { this.traitementsEnCours = traitementsEnCours; }
}
