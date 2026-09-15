package com.diamyaraam.wallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITÉ : Beneficiaire — Portefeuille Familial (RM136 à RM138)
 *
 * RM136 — Prise en charge financière d'un proche (enfant, parent, conjoint...)
 * RM137 — Gestion de l'accès (ACTIF, SUSPENDU) par le titulaire
 * RM138 — Débit automatique du tuteur
 */
@Entity
@Table(name = "beneficiaire", schema = "wallet_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Beneficiaire {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // Le portefeuille du tuteur (payeur)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "portefeuille_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Portefeuille portefeuille;

    // L'utilisateur proche bénéficiaire
    @Column(name = "beneficiaire_user_id", nullable = false)
    private UUID beneficiaireUserId;

    public enum LienParente { ENFANT, PARENT, CONJOINT, AUTRE }

    @Enumerated(EnumType.STRING)
    @Column(name = "lien_parente", length = 20, nullable = false)
    private LienParente lienParente;

    public enum StatutBeneficiaire { EN_ATTENTE, ACTIF, SUSPENDU, REJETE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutBeneficiaire statut = StatutBeneficiaire.EN_ATTENTE;

    // Plafond mensuel de dépense autorisé (null = pas de plafond)
    @Column(name = "plafond_mensuel", precision = 15, scale = 2)
    private BigDecimal plafondMensuel;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Beneficiaire() {}

    public boolean isAutorise() {
        return StatutBeneficiaire.ACTIF.equals(this.statut);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Portefeuille getPortefeuille() { return portefeuille; }
    public void setPortefeuille(Portefeuille portefeuille) { this.portefeuille = portefeuille; }

    public UUID getBeneficiaireUserId() { return beneficiaireUserId; }
    public void setBeneficiaireUserId(UUID beneficiaireUserId) { this.beneficiaireUserId = beneficiaireUserId; }

    public LienParente getLienParente() { return lienParente; }
    public void setLienParente(LienParente lienParente) { this.lienParente = lienParente; }

    public StatutBeneficiaire getStatut() { return statut; }
    public void setStatut(StatutBeneficiaire statut) { this.statut = statut; }

    public BigDecimal getPlafondMensuel() { return plafondMensuel; }
    public void setPlafondMensuel(BigDecimal plafondMensuel) { this.plafondMensuel = plafondMensuel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
