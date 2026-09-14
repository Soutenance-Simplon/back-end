package com.diamyaraam.rdv.dto;

import com.diamyaraam.rdv.entity.RendezVous;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message WebSocket envoyé en push à tous les clients abonnés à /topic/rdv-updates.
 * Permet au patient et au médecin d'être notifiés en temps réel des créations et changements de statut.
 */
public class RdvUpdateEvent {

    private UUID rdvId;
    private UUID patientId;
    private UUID medecinId;
    private String nouveauStatut;
    private String timestamp;
    private String type; // "STATUT_CHANGE" | "CREATE" | "CANCELLED"
    private String motif;
    private String dateHeure;
    private String typeConsultation;
    private Double montant;
    private Boolean paiementValide;

    public RdvUpdateEvent() {}

    public RdvUpdateEvent(RendezVous rdv, String type) {
        this.rdvId = rdv.getId();
        this.patientId = rdv.getPatientId();
        this.medecinId = rdv.getMedecinId();
        this.nouveauStatut = rdv.getStatut() != null ? rdv.getStatut().name() : null;
        this.timestamp = LocalDateTime.now().toString();
        this.type = type;
        this.motif = rdv.getMotif();
        LocalDateTime dh = rdv.getDateHeureConfirmee() != null ? rdv.getDateHeureConfirmee() : rdv.getDateHeureSouhaitee();
        this.dateHeure = dh != null ? dh.toString() : LocalDateTime.now().toString();
        this.typeConsultation = rdv.getTypeConsultation() != null ? rdv.getTypeConsultation().name() : "PRESENTIELLE";
        this.montant = 15000.0;
        this.paiementValide = rdv.getPaiementValide();
    }

    public UUID getRdvId() { return rdvId; }
    public void setRdvId(UUID rdvId) { this.rdvId = rdvId; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getMedecinId() { return medecinId; }
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    public String getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getDateHeure() { return dateHeure; }
    public void setDateHeure(String dateHeure) { this.dateHeure = dateHeure; }

    public String getTypeConsultation() { return typeConsultation; }
    public void setTypeConsultation(String typeConsultation) { this.typeConsultation = typeConsultation; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Boolean getPaiementValide() { return paiementValide; }
    public void setPaiementValide(Boolean paiementValide) { this.paiementValide = paiementValide; }
}
