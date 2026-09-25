package com.diamyaraam.rdv.service;

import com.diamyaraam.rdv.entity.RendezVous;
import com.diamyaraam.rdv.repository.RendezVousRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de rappels automatiques de téléconsultation.
 *
 * Règle : 10 minutes avant l'heure du RDV, envoie une notification aux 2 parties
 * pour les prévenir que la salle s'ouvrira dans 5 minutes.
 * (La salle devient accessible 5 minutes avant l'heure du RDV)
 */
@Service
public class TeleconsultationReminderService {

    private static final Logger log = LoggerFactory.getLogger(TeleconsultationReminderService.class);

    private final RendezVousRepository rendezVousRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TeleconsultationReminderService(
            RendezVousRepository rendezVousRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.rendezVousRepository = rendezVousRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Tâche planifiée qui s'exécute toutes les minutes.
     * Cherche les téléconsultations CONFIRMÉES dont l'heure est dans exactement
     * 9 à 11 minutes (fenêtre de 2 min pour éviter les doublons).
     */
    @Scheduled(fixedDelay = 60_000) // toutes les 60 secondes
    public void envoyerRappelsTeleconsultation() {
        final LocalDateTime maintenant = LocalDateTime.now();
        // Fenêtre : RDV dans 9 à 11 minutes → on envoie le rappel "dans 5 minutes la salle ouvre"
        final LocalDateTime fenetreDebut = maintenant.plusMinutes(9);
        final LocalDateTime fenetreFin   = maintenant.plusMinutes(11);

        List<RendezVous> rdvsProches;
        try {
            rdvsProches = rendezVousRepository
                    .findByTypeConsultationAndStatutAndDateHeureSouhaiteeBetween(
                            RendezVous.TypeConsultation.TELECONSULTATION,
                            RendezVous.StatutRendezVous.CONFIRME,
                            fenetreDebut,
                            fenetreFin
                    );
        } catch (Exception e) {
            log.debug("Rappel scheduler: erreur requête → {}", e.getMessage());
            return;
        }

        for (RendezVous rdv : rdvsProches) {
            log.info("📢 Rappel téléconsultation dans ~10 min → RDV {}", rdv.getId());
            envoyerRappelAux2Parties(rdv);
        }
    }

    private void envoyerRappelAux2Parties(RendezVous rdv) {
        final String messageCommun = "Votre téléconsultation commence dans 10 minutes. La salle de visio s'ouvrira dans 5 minutes — préparez-vous !";

        // Notification au PATIENT
        envoyerNotification(
                rdv.getPatientId(),
                "RAPPEL_TELECONSULTATION",
                "⏰ Rappel — Téléconsultation imminente",
                messageCommun,
                rdv.getId()
        );

        // Notification au MÉDECIN
        envoyerNotification(
                rdv.getMedecinId(),
                "RAPPEL_TELECONSULTATION",
                "⏰ Rappel — Téléconsultation imminente",
                messageCommun,
                rdv.getId()
        );
    }

    private void envoyerNotification(UUID userId, String type, String titre, String corps, UUID rdvId) {
        if (userId == null) return;
        try {
            Map<String, Object> notif = Map.of(
                    "id",        "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId",    userId.toString(),
                    "type",      type,
                    "titre",     titre,
                    "corps",     corps,
                    "message",   corps,
                    "rdvId",     rdvId != null ? rdvId.toString() : "",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue",       false
            );
            messagingTemplate.convertAndSend("/topic/notifications", Map.of(
                    "destination", "/topic/notifications",
                    "type",        "NEW_NOTIFICATION",
                    "data",        notif
            ));
        } catch (Exception e) {
            log.debug("Erreur envoi rappel WebSocket: {}", e.getMessage());
        }
    }
}
