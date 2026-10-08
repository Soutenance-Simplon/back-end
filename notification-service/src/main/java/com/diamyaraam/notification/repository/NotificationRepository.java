// Déclaration du package Java : `com.diamyaraam.notification.repository`
package com.diamyaraam.notification.repository;

// Import de la classe `Notification` (paquet com.diamyaraam.notification.entity)
import com.diamyaraam.notification.entity.Notification;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `NotificationRepository` : repository Spring Data JPA de l'entité `Notification` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // Méthode abstraite (contrat) `findByDestinataireIdOrderByCreatedAtDesc` — paramètres : `destinataireId` (identifiant UUID) ; retourne : liste de Notification ; intention : recherche par (find by destinataire id order by created at desc)
    List<Notification> findByDestinataireIdOrderByCreatedAtDesc(UUID destinataireId);
    // Méthode abstraite (contrat) `findByDestinataireIdAndLueFalseOrderByCreatedAtDesc` — paramètres : `destinataireId` (identifiant UUID) ; retourne : liste de Notification ; intention : recherche par (find by destinataire id and lue false order by created at desc)
    List<Notification> findByDestinataireIdAndLueFalseOrderByCreatedAtDesc(UUID destinataireId);
    // Méthode abstraite (contrat) `countByDestinataireIdAndLueFalse` — paramètres : `destinataireId` (identifiant UUID) ; retourne : entier long ; intention : compte (count by destinataire id and lue false)
    long countByDestinataireIdAndLueFalse(UUID destinataireId);
}
