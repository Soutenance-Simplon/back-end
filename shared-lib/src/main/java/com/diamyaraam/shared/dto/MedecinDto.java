package com.diamyaraam.shared.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class MedecinDto {

    private UUID id;
    private UUID userId;
    private String nomComplet;
    private String specialite;
    private String etablissement;
    private String region;
    private boolean isVerified;
    private String photoProfessionnelle;
    private String biographie;
    private String languesParlees;

    private boolean teleconsultationActive;
    private BigDecimal tarifConsultation;
    private int dureeConsultationMinutes;

    private String statutMedecin;

    public MedecinDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getEtablissement() { return etablissement; }
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getPhotoProfessionnelle() { return photoProfessionnelle; }
    public void setPhotoProfessionnelle(String photoProfessionnelle) { this.photoProfessionnelle = photoProfessionnelle; }

    public String getBiographie() { return biographie; }
    public void setBiographie(String biographie) { this.biographie = biographie; }

    public String getLanguesParlees() { return languesParlees; }
    public void setLanguesParlees(String languesParlees) { this.languesParlees = languesParlees; }

    public boolean isTeleconsultationActive() { return teleconsultationActive; }
    public void setTeleconsultationActive(boolean teleconsultationActive) { this.teleconsultationActive = teleconsultationActive; }

    public BigDecimal getTarifConsultation() { return tarifConsultation; }
    public void setTarifConsultation(BigDecimal tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    public int getDureeConsultationMinutes() { return dureeConsultationMinutes; }
    public void setDureeConsultationMinutes(int dureeConsultationMinutes) { this.dureeConsultationMinutes = dureeConsultationMinutes; }

    public String getStatutMedecin() { return statutMedecin; }
    public void setStatutMedecin(String statutMedecin) { this.statutMedecin = statutMedecin; }
}
