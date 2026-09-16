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
 * ENTITÉ : Portefeuille (Health Wallet) — RM131 à RM133
 *
 * RM131 — Création automatique à la création du compte. Un seul portefeuille principal par utilisateur.
 * RM132 — Identifiant UUID indépendant du compte utilisateur.
 * RM133 — Solde exprimé en FCFA, jamais négatif.
 */
@Entity
@Table(name = "portefeuille", schema = "wallet_schema")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Portefeuille {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // UUID reference to User in auth-service
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // Solde en FCFA (jamais négatif) — RM133
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal solde = BigDecimal.ZERO;

    @Column(name = "devise", length = 10, nullable = false)
    private String devise = "FCFA";

    public enum StatutPortefeuille { ACTIF, SUSPENDU, BLOQUE }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutPortefeuille statut = StatutPortefeuille.ACTIF;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Portefeuille() {}

    public boolean aSoldeSuffisant(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) return false;
        return this.solde.compareTo(montant) >= 0;
    }

    public void crediter(BigDecimal montant) {
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            this.solde = this.solde.add(montant);
        }
    }

    public void debiter(BigDecimal montant) {
        if (!aSoldeSuffisant(montant)) {
            throw new IllegalStateException("Solde insuffisant dans le Portefeuille Santé.");
        }
        this.solde = this.solde.subtract(montant);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public StatutPortefeuille getStatut() { return statut; }
    public void setStatut(StatutPortefeuille statut) { this.statut = statut; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
