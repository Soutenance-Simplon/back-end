package com.diamyaraam.notification.service;

import com.diamyaraam.notification.entity.Notification;
import com.diamyaraam.notification.repository.NotificationRepository;
import com.diamyaraam.notification.util.RealtimePublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RealtimePublisher realtimePublisher;

    public NotificationService(NotificationRepository notificationRepository, RealtimePublisher realtimePublisher) {
        this.notificationRepository = notificationRepository;
        this.realtimePublisher = realtimePublisher;
    }

    @Transactional
    public Notification sendNotification(UUID destinataireId, Notification.TypeNotification type, String titre, String message, UUID rdvId, Notification.Canal canal) {
        Notification notif = new Notification();
        notif.setDestinataireId(destinataireId);
        notif.setType(type);
        notif.setTitre(titre);
        notif.setMessage(message);
        notif.setRendezVousId(rdvId);
        notif.setCanal(canal != null ? canal : Notification.Canal.IN_APP);
        notif.setEnvoyee(true);
        Notification saved = notificationRepository.save(notif);

        // 📡 Diffusion temps réel via WebSocket
        try {
            realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                "id", saved.getId() != null ? saved.getId().toString() : UUID.randomUUID().toString(),
                "userId", destinataireId != null ? destinataireId.toString() : "",
                "type", type != null ? type.name() : "IN_APP",
                "titre", titre != null ? titre : "Notification",
                "corps", message != null ? message : "",
                "message", message != null ? message : "",
                "rdvId", rdvId != null ? rdvId.toString() : "",
                "dateEnvoi", LocalDateTime.now().toString(),
                "lue", false
            ));
        } catch (Exception ignored) {}

        return saved;
    }

    public List<Notification> getUserNotifications(UUID destinataireId) {
        return notificationRepository.findByDestinataireIdOrderByCreatedAtDesc(destinataireId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setLue(true);
            n.setDateLecture(LocalDateTime.now());
            notificationRepository.save(n);

            // 📡 Informer le client en temps réel
            try {
                realtimePublisher.publish("/topic/notifications", "NOTIFICATION_READ", Map.of(
                    "id", notificationId.toString(),
                    "userId", n.getDestinataireId() != null ? n.getDestinataireId().toString() : ""
                ));
            } catch (Exception ignored) {}
        });
    }

    public long getUnreadCount(UUID destinataireId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(destinataireId);
    }
}
