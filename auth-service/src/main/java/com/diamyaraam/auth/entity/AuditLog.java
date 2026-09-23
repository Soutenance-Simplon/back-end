package com.diamyaraam.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Journal des actions sensibles (RM029)
 */
@Entity
@Table(name = "audit_log", schema = "auth_schema")
public class AuditLog {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    public enum ActionType {
        CREATION_COMPTE,
        ACTIVATION_COMPTE,
        CONNEXION_REUSSIE,
        ECHEC_CONNEXION,
        COMPTE_BLOQUE,
        COMPTE_DEBLOQUE,
        CHANGEMENT_MOT_DE_PASSE,
        DEMANDE_REINITIALISATION_MDP,
        OTP_ENVOYE,
        OTP_VALIDE,
        OTP_EXPIRE,
        OTP_ECHEC,
        OTP_MAX_ATTEINT,
        MODIFICATION_PROFIL,
        ACCES_DOSSIER_MEDECIN,
        ACCES_DOSSIER_URGENCE_BRIS_DE_GLACE,
        SIGNALEMENT_ACCES_ABUSIF
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 40, nullable = false)
    private ActionType actionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "telephone_tente", length = 20)
    private String telephoneTente;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Boolean success = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTelephoneTente() { return telephoneTente; }
    public void setTelephoneTente(String telephoneTente) { this.telephoneTente = telephoneTente; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
