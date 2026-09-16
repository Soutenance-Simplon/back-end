package com.diamyaraam.notification.service;

import com.diamyaraam.notification.entity.Notification;
import com.diamyaraam.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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
        return notificationRepository.save(notif);
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
        });
    }

    public long getUnreadCount(UUID destinataireId) {
        return notificationRepository.countByDestinataireIdAndLueFalse(destinataireId);
    }
}
