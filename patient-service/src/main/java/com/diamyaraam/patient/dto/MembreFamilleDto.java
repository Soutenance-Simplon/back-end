package com.diamyaraam.patient.dto;

import java.time.LocalDate;
import java.util.UUID;

public class MembreFamilleDto {
    private UUID id;
    private UUID parentUserId;
    private UUID enfantUserId;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String genre;
    private String lienParente;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getParentUserId() { return parentUserId; }
    public void setParentUserId(UUID parentUserId) { this.parentUserId = parentUserId; }
    public UUID getEnfantUserId() { return enfantUserId; }
    public void setEnfantUserId(UUID enfantUserId) { this.enfantUserId = enfantUserId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLienParente() { return lienParente; }
    public void setLienParente(String lienParente) { this.lienParente = lienParente; }
}
