package com.diamyaraam.medecin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Base de référence officielle ONMS (RM031-RM038)
 */
@Entity
@Table(name = "onms_reference", schema = "medecin_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OnmsReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ordre", unique = true, nullable = false, length = 50)
    private String numeroOrdre;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 150)
    private String prenom;

    @Column(nullable = false, length = 100)
    private String specialite;

    @Column(name = "date_inscription")
    private java.time.LocalDate dateInscription;

    public enum StatutProfessionnel { ACTIF, SUSPENDU, RADIE, RETRAITE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_professionnel", length = 20, nullable = false)
    private StatutProfessionnel statutProfessionnel = StatutProfessionnel.ACTIF;

    @Column(length = 200)
    private String etablissement;

    @Column(length = 100)
    private String region;

    @Column(length = 50)
    private String section;

    @Column(length = 20)
    private String telephone;

    @Column(name = "derniere_synchro")
    private LocalDateTime derniereSynchro;

    public OnmsReference() {}

    public boolean estAutoriseAExercer() {
        return StatutProfessionnel.ACTIF.equals(this.statutProfessionnel);
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroOrdre() { return numeroOrdre; }
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public java.time.LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(java.time.LocalDate dateInscription) { this.dateInscription = dateInscription; }

    public StatutProfessionnel getStatutProfessionnel() { return statutProfessionnel; }
    public void setStatutProfessionnel(StatutProfessionnel statutProfessionnel) { this.statutProfessionnel = statutProfessionnel; }

    public String getEtablissement() { return etablissement; }
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public LocalDateTime getDerniereSynchro() { return derniereSynchro; }
    public void setDerniereSynchro(LocalDateTime derniereSynchro) { this.derniereSynchro = derniereSynchro; }
}
