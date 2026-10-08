// Déclaration du package Java : `com.diamyaraam.rdv.service`
package com.diamyaraam.rdv.service;

// Import de la classe `RendezVous` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.RendezVous;
// Import de la classe `RendezVousRepository` (paquet com.diamyaraam.rdv.repository)
import com.diamyaraam.rdv.repository.RendezVousRepository;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `SimpMessagingTemplate` (paquet org.springframework.messaging.simp)
import org.springframework.messaging.simp.SimpMessagingTemplate;
// Import de la classe `Scheduled` (paquet org.springframework.scheduling.annotation)
import org.springframework.scheduling.annotation.Scheduled;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * Service de rappels automatiques de téléconsultation.
 *
 * Règle : 10 minutes avant l'heure du RDV, envoie une notification aux 2 parties
 * pour les prévenir que la salle s'ouvrira dans 5 minutes.
 * (La salle devient accessible 5 minutes avant l'heure du RDV)
 */
@Service
// Déclaration de la classe `TeleconsultationReminderService` (rôle : porte la logique métier)
public class TeleconsultationReminderService {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(TeleconsultationReminderService.class)`
    private static final Logger log = LoggerFactory.getLogger(TeleconsultationReminderService.class);

    // Attribut `rendezVousRepository` de type RendezVousRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RendezVousRepository rendezVousRepository;
    // Attribut `messagingTemplate` de type SimpMessagingTemplate — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final SimpMessagingTemplate messagingTemplate;

    // Constructeur de `TeleconsultationReminderService`
    public TeleconsultationReminderService(
            // Paramètre `rendezVousRepository` de type RendezVousRepository
            RendezVousRepository rendezVousRepository,
            // Paramètre `messagingTemplate` de type SimpMessagingTemplate
            SimpMessagingTemplate messagingTemplate) {
        // Initialise l'attribut `rendezVousRepository` avec la valeur de rendezVousRepository
        this.rendezVousRepository = rendezVousRepository;
        // Initialise l'attribut `messagingTemplate` avec la valeur de messagingTemplate
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Tâche planifiée qui s'exécute toutes les minutes.
     * Cherche les téléconsultations CONFIRMÉES dont l'heure est dans exactement
     * 9 à 11 minutes (fenêtre de 2 min pour éviter les doublons).
     */
    @Scheduled(fixedDelay = 60_000) // toutes les 60 secondes
    // Méthode `envoyerRappelsTeleconsultation` (publique) — sans paramètre ; retourne : aucune valeur
    public void envoyerRappelsTeleconsultation() {
        // Déclare la variable `maintenant` (date-heure) initialisée avec la date et l'heure courantes (LocalDateTime.now())
        final LocalDateTime maintenant = LocalDateTime.now();
        // Fenêtre : RDV dans 9 à 11 minutes → on envoie le rappel "dans 5 minutes la salle ouvre"
        final LocalDateTime fenetreDebut = maintenant.plusMinutes(9);
        // Déclare la variable `fenetreFin` (date-heure) initialisée avec `maintenant.plusMinutes(11)`
        final LocalDateTime fenetreFin   = maintenant.plusMinutes(11);

        // Déclare la variable `rdvsProches` (liste de RendezVous)
        List<RendezVous> rdvsProches;
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Affecte à `rdvsProches` la valeur de rendezVousRepository
            rdvsProches = rendezVousRepository
                    // Enchaînement : appelle `findByTypeConsultationAndStatutAndDateHeureSouhaiteeBetween(`
                    .findByTypeConsultationAndStatutAndDateHeureSouhaiteeBetween(
                            // Argument/valeur : la valeur de RendezVous.TypeConsultation.TELECONSULTATION
                            RendezVous.TypeConsultation.TELECONSULTATION,
                            // Argument/valeur : la valeur de RendezVous.StatutRendezVous.CONFIRME
                            RendezVous.StatutRendezVous.CONFIRME,
                            // Argument/valeur : la valeur de fenetreDebut
                            fenetreDebut,
                            // Argument/valeur : la valeur de fenetreFin
                            fenetreFin
                    );
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Écrit un message de débogage dans les journaux : "Rappel scheduler: erreur requête → {}", e.getMessage());
            log.debug("Rappel scheduler: erreur requête → {}", e.getMessage());
            // Retourne (fin de la méthode sans valeur)
            return;
        }

        // Boucle `for` : RendezVous rdv : rdvsProches)
        for (RendezVous rdv : rdvsProches) {
            // Écrit un message informatif dans les journaux : "📢 Rappel téléconsultation dans ~10 min → RDV {}", rdv.getId());
            log.info("📢 Rappel téléconsultation dans ~10 min → RDV {}", rdv.getId());
            // Appelle la méthode locale `envoyerRappelAux2Parties` : envoyerRappelAux2Parties(rdv);
            envoyerRappelAux2Parties(rdv);
        }
    }

    // Méthode `envoyerRappelAux2Parties` (privée) — paramètres : `rdv` (RendezVous) ; retourne : aucune valeur
    private void envoyerRappelAux2Parties(RendezVous rdv) {
        // Déclare la variable `messageCommun` (chaîne de caractères) initialisée avec le texte "Votre téléconsultation commence dans 10 minutes. La salle de visio s…
        final String messageCommun = "Votre téléconsultation commence dans 10 minutes. La salle de visio s'ouvrira dans 5 minutes — préparez-vous !";

        // Notification au PATIENT
        envoyerNotification(
                // Argument/valeur : `rdv.getPatientId(`
                rdv.getPatientId(),
                // Paire clé/valeur : clé « RAPPEL_TELECONSULTATION » associée à 
                "RAPPEL_TELECONSULTATION",
                // Paire clé/valeur : clé «  Rappel — Téléconsultation imminente » associée à 
                " Rappel — Téléconsultation imminente",
                // Argument/valeur : la valeur de messageCommun
                messageCommun,
                // Argument/valeur : `rdv.getId(`
                rdv.getId()
        );

        // Notification au MÉDECIN
        envoyerNotification(
                // Argument/valeur : `rdv.getMedecinId(`
                rdv.getMedecinId(),
                // Paire clé/valeur : clé « RAPPEL_TELECONSULTATION » associée à 
                "RAPPEL_TELECONSULTATION",
                // Paire clé/valeur : clé «  Rappel — Téléconsultation imminente » associée à 
                " Rappel — Téléconsultation imminente",
                // Argument/valeur : la valeur de messageCommun
                messageCommun,
                // Argument/valeur : `rdv.getId(`
                rdv.getId()
        );
    }

    // Méthode `envoyerNotification` (privée) — paramètres : `userId` (identifiant UUID), `type` (chaîne de caractères), `titre` (chaîne de caractères), `corps` (chaîne de caractères), `rdvId` (identifiant UUID) ; retourne : aucune valeur
    private void envoyerNotification(UUID userId, String type, String titre, String corps, UUID rdvId) {
        // Condition : exécute le bloc suivant seulement si `userId == null) return;`
        if (userId == null) return;
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `notif` (dictionnaire clé/valeur) initialisée avec `Map.of(`
            Map<String, Object> notif = Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id",        "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `userId.toString(`
                    "userId",    userId.toString(),
                    // Paire clé/valeur : clé « type » associée à la valeur de type
                    "type",      type,
                    // Paire clé/valeur : clé « titre » associée à la valeur de titre
                    "titre",     titre,
                    // Paire clé/valeur : clé « corps » associée à la valeur de corps
                    "corps",     corps,
                    // Paire clé/valeur : clé « message » associée à la valeur de corps
                    "message",   corps,
                    // Paire clé/valeur : clé « rdvId » associée à `rdvId != null ? rdvId.toString() : ""`
                    "rdvId",     rdvId != null ? rdvId.toString() : "",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue",       false
            );
            // Appelle la méthode `convertAndSend` sur `messagingTemplate` : messagingTemplate.convertAndSend("/topic/notifications", Map.of(
            messagingTemplate.convertAndSend("/topic/notifications", Map.of(
                    // Paire clé/valeur : clé « destination » associée à le texte "/topic/notifications"
                    "destination", "/topic/notifications",
                    // Paire clé/valeur : clé « type » associée à le texte "NEW_NOTIFICATION"
                    "type",        "NEW_NOTIFICATION",
                    // Paire clé/valeur : clé « data » associée à la valeur de notif
                    "data",        notif
            ));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Écrit un message de débogage dans les journaux : "Erreur envoi rappel WebSocket: {}", e.getMessage());
            log.debug("Erreur envoi rappel WebSocket: {}", e.getMessage());
        }
    }
}
