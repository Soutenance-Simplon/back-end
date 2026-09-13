package com.diamyaraam.dossier.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dossier_vaccination", schema = "dossier_schema")
public class Vaccination {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    @Column(name = "nom_vaccin", nullable = false, length = 150)
    private String nomVaccin;

    @Column(name = "maladie_cible", length = 150)
    private String maladieCible;

    @Column(name = "date_injection", nullable = false)
    private LocalDate dateInjection;

    @Column(name = "date_rappel")
    private LocalDate dateRappel;

    @Column(name = "numero_lot", length = 50)
    private String numeroLot;

    @Column(name = "centre_vaccination", length = 200)
    private String centreVaccination;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Vaccination() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DossierMedical getDossierMedical() { return dossierMedical; }
    public void setDossierMedical(DossierMedical dossierMedical) { this.dossierMedical = dossierMedical; }

    public String getNomVaccin() { return nomVaccin; }
    public void setNomVaccin(String nomVaccin) { this.nomVaccin = nomVaccin; }

    public String getMaladieCible() { return maladieCible; }
    public void setMaladieCible(String maladieCible) { this.maladieCible = maladieCible; }

    public LocalDate getDateInjection() { return dateInjection; }
    public void setDateInjection(LocalDate dateInjection) { this.dateInjection = dateInjection; }

    public LocalDate getDateRappel() { return dateRappel; }
    public void setDateRappel(LocalDate dateRappel) { this.dateRappel = dateRappel; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public String getCentreVaccination() { return centreVaccination; }
    public void setCentreVaccination(String centreVaccination) { this.centreVaccination = centreVaccination; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
