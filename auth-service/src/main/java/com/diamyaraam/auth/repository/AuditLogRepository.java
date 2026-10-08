// Déclaration du package Java : `com.diamyaraam.auth.repository`
package com.diamyaraam.auth.repository;

// Import de la classe `AuditLog` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.AuditLog;
// Import de la classe `ActionType` (paquet com.diamyaraam.auth.entity.AuditLog)
import com.diamyaraam.auth.entity.AuditLog.ActionType;
// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `AuditLogRepository` : repository Spring Data JPA de l'entité `AuditLog` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    // Méthode abstraite (contrat) `findByUserOrderByCreatedAtDesc` — paramètres : `user` (User) ; retourne : liste de AuditLog ; intention : recherche par (find by user order by created at desc)
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
    // Méthode abstraite (contrat) `findByUserAndActionType` — paramètres : `user` (User), `type` (ActionType) ; retourne : liste de AuditLog ; intention : recherche par (find by user and action type)
    List<AuditLog> findByUserAndActionType(User user, ActionType type);
    // Méthode abstraite (contrat) `countByUserAndActionTypeAndCreatedAtAfter` — paramètres : `user` (User), `type` (ActionType), `since` (date-heure) ; retourne : entier long ; intention : compte (count by user and action type and created at after)
    long countByUserAndActionTypeAndCreatedAtAfter(User user, ActionType type, LocalDateTime since);
    // Méthode abstraite (contrat) `findByTelephoneTente` — paramètres : `telephone` (chaîne de caractères) ; retourne : liste de AuditLog ; intention : recherche par (find by telephone tente)
    List<AuditLog> findByTelephoneTente(String telephone);

    // Requête JPQL/SQL personnalisée : SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u LEFT JOIN FETCH u.r…
    @org.springframework.data.jpa.repository.Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u LEFT JOIN FETCH u.role ORDER BY a.createdAt DESC")
    // Méthode abstraite (contrat) `findTop100AuditLogs` — sans paramètre ; retourne : liste de AuditLog ; intention : recherche (find top100 audit logs)
    List<AuditLog> findTop100AuditLogs();
}

