package com.diamyaraam.wallet.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITÉ : AuditFinancier — Traçabilité et détection d'anomalies (RM145)
 */
@Entity
@Table(name = "audit_financier", schema = "wallet_schema")
public class AuditFinancier {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    public enum ActionFinanciere {
        DEPOT, PAIEMENT, REMBOURSEMENT, ECHEC_SOLDE_INSUFFISANT, ANOMALIE_SUSPECTE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 35, nullable = false)
    private ActionFinanciere action;

    @Column(precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private Boolean success = true;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AuditFinancier() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public ActionFinanciere getAction() { return action; }
    public void setAction(ActionFinanciere action) { this.action = action; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
