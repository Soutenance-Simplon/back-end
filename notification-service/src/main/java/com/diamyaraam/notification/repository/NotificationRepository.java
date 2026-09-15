package com.diamyaraam.notification.repository;

import com.diamyaraam.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(UUID destinataireId);
    List<Notification> findByDestinataireIdAndLueFalseOrderByCreatedAtDesc(UUID destinataireId);
    long countByDestinataireIdAndLueFalse(UUID destinataireId);
}
