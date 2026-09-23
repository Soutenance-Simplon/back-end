package com.diamyaraam.notification.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification", schema = "notification_schema")
public class Notification {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    // UUID reference to User in auth-service
    @Column(name = "destinataire_id", nullable = false)
    private UUID destinataireId;

    public enum TypeNotification {
        DEMANDE_RDV, RDV_ACCEPTE, RDV_CONFIRME, RDV_ANNULE,
        RDV_RAPPEL_24H, RDV_RAPPEL_1H, PATIENT_ABSENT,
        ACCES_DOSSIER_DEMANDE, ACCES_DOSSIER_ACCORDE,
        ACCES_DOSSIER_CONSULTE, ACCES_DOSSIER_URGENCE, SIGNALEMENT_ACCES_ABUSIF,
        NOUVELLE_PRESCRIPTION, OTP_ENVOYE, COMPTE_BLOQUE, CHANGEMENT_STATUT
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private TypeNotification type;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "rendez_vous_id")
    private UUID rendezVousId;

    @Column(nullable = false)
    private Boolean lue = false;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    public enum Canal { IN_APP, SMS, EMAIL }

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Canal canal = Canal.IN_APP;

    @Column(name = "envoyee", nullable = false)
    private Boolean envoyee = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Notification() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDestinataireId() { return destinataireId; }
    public void setDestinataireId(UUID destinataireId) { this.destinataireId = destinataireId; }

    public TypeNotification getType() { return type; }
    public void setType(TypeNotification type) { this.type = type; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UUID getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(UUID rendezVousId) { this.rendezVousId = rendezVousId; }

    public Boolean getLue() { return lue; }
    public void setLue(Boolean lue) { this.lue = lue; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public Boolean getEnvoyee() { return envoyee; }
    public void setEnvoyee(Boolean envoyee) { this.envoyee = envoyee; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
