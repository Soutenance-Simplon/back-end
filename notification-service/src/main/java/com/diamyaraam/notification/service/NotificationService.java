// Déclaration du package Java : `com.diamyaraam.notification.service`
package com.diamyaraam.notification.service;

// Import de la classe `Notification` (paquet com.diamyaraam.notification.entity)
import com.diamyaraam.notification.entity.Notification;
// Import de la classe `NotificationRepository` (paquet com.diamyaraam.notification.repository)
import com.diamyaraam.notification.repository.NotificationRepository;
// Import de la classe `RealtimePublisher` (paquet com.diamyaraam.notification.util)
import com.diamyaraam.notification.util.RealtimePublisher;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Composant de la couche métier (service Spring)
@Service
// Déclaration de la classe `NotificationService` (rôle : porte la logique métier)
public class NotificationService {

    // Attribut `notificationRepository` de type NotificationRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final NotificationRepository notificationRepository;
    // Attribut `realtimePublisher` de type RealtimePublisher — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RealtimePublisher realtimePublisher;

    // Constructeur de `NotificationService` — paramètres : `notificationRepository` (NotificationRepository), `realtimePublisher` (RealtimePublisher) (injection des dépendances par Spring)
    public NotificationService(NotificationRepository notificationRepository, RealtimePublisher realtimePublisher) {
        // Initialise l'attribut `notificationRepository` avec la valeur de notificationRepository
        this.notificationRepository = notificationRepository;
        // Initialise l'attribut `realtimePublisher` avec la valeur de realtimePublisher
        this.realtimePublisher = realtimePublisher;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `sendNotification` (publique) — paramètres : `destinataireId` (identifiant UUID), `type` (Notification.TypeNotification), `titre` (chaîne de caractères), `message` (chaîne de caractères), `rdvId` (identifiant UUID), `canal` (Notification.Canal) ; retourne : Notification ; intention : envoie (send notification)
    public Notification sendNotification(UUID destinataireId, Notification.TypeNotification type, String titre, String message, UUID rdvId, Notification.Canal canal) {
        // Déclare la variable `notif` (Notification) initialisée avec une nouvelle instance de Notification
        Notification notif = new Notification();
        // Renseigne la propriété DestinataireId de `notif` avec la valeur de destinataireId
        notif.setDestinataireId(destinataireId);
        // Renseigne la propriété Type de `notif` avec la valeur de type
        notif.setType(type);
        // Renseigne la propriété Titre de `notif` avec la valeur de titre
        notif.setTitre(titre);
        // Renseigne la propriété Message de `notif` avec la valeur de message
        notif.setMessage(message);
        // Renseigne la propriété RendezVousId de `notif` avec la valeur de rdvId
        notif.setRendezVousId(rdvId);
        // Renseigne la propriété Canal de `notif` avec `canal != null ? canal : Notification.Canal.IN_APP`
        notif.setCanal(canal != null ? canal : Notification.Canal.IN_APP);
        // Renseigne la propriété Envoyee de `notif` avec le booléen vrai
        notif.setEnvoyee(true);
        // Déclare la variable `saved` (Notification) initialisée avec l'enregistrement en base de notif via notificationRepository
        Notification saved = notificationRepository.save(notif);

        // 📡 Diffusion temps réel via WebSocket
        try {
            // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
            realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                // Paire clé/valeur : clé « id » associée à un UUID aléatoire (saved.getId() != null ? saved.getId().toString() : UUID.ran…)
                "id", saved.getId() != null ? saved.getId().toString() : UUID.randomUUID().toString(),
                // Paire clé/valeur : clé « userId » associée à `destinataireId != null ? destinataireId.toString() : ""`
                "userId", destinataireId != null ? destinataireId.toString() : "",
                // Paire clé/valeur : clé « type » associée à `type != null ? type.name() : "IN_APP"`
                "type", type != null ? type.name() : "IN_APP",
                // Paire clé/valeur : clé « titre » associée à `titre != null ? titre : "Notification"`
                "titre", titre != null ? titre : "Notification",
                // Paire clé/valeur : clé « corps » associée à `message != null ? message : ""`
                "corps", message != null ? message : "",
                // Paire clé/valeur : clé « message » associée à `message != null ? message : ""`
                "message", message != null ? message : "",
                // Paire clé/valeur : clé « rdvId » associée à `rdvId != null ? rdvId.toString() : ""`
                "rdvId", rdvId != null ? rdvId.toString() : "",
                // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                "dateEnvoi", LocalDateTime.now().toString(),
                // Paire clé/valeur : clé « lue » associée à le booléen faux
                "lue", false
            ));
        // Interception de l'exception Exception ignored
        } catch (Exception ignored) {}

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getUserNotifications` (publique) — paramètres : `destinataireId` (identifiant UUID) ; retourne : liste de Notification ; intention : récupère (get user notifications)
    public List<Notification> getUserNotifications(UUID destinataireId) {
        // Retourne le résultat de la requête findByDestinataireIdOrderByCreatedAtDesc exécutée via notificationRepository
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(destinataireId);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `markAsRead` (publique) — paramètres : `notificationId` (identifiant UUID) ; retourne : aucune valeur
    public void markAsRead(UUID notificationId) {
        // Appelle la méthode `findById` sur `notificationRepository` : notificationRepository.findById(notificationId).ifPresent(n -> {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            // Renseigne la propriété Lue de `n` avec le booléen vrai
            n.setLue(true);
            // Renseigne la propriété DateLecture de `n` avec la date et l'heure courantes (LocalDateTime.now())
            n.setDateLecture(LocalDateTime.now());
            // Enregistre n en base de données via notificationRepository
            notificationRepository.save(n);

            // 📡 Informer le client en temps réel
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NOTIFICATION_READ", Map…
                realtimePublisher.publish("/topic/notifications", "NOTIFICATION_READ", Map.of(
                    // Paire clé/valeur : clé « id » associée à `notificationId.toString(`
                    "id", notificationId.toString(),
                    // Paire clé/valeur : clé « userId » associée à `n.getDestinataireId() != null ? n.getDestinataireId().toString() : ""`
                    "userId", n.getDestinataireId() != null ? n.getDestinataireId().toString() : ""
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        });
    }

    // Méthode `getUnreadCount` (publique) — paramètres : `destinataireId` (identifiant UUID) ; retourne : entier long ; intention : récupère (get unread count)
    public long getUnreadCount(UUID destinataireId) {
        // Retourne le résultat de la requête countByDestinataireIdAndLueFalse exécutée via notificationRepository
        return notificationRepository.countByDestinataireIdAndLueFalse(destinataireId);
    }
}
