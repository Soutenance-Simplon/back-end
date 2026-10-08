// Déclaration du package Java : `com.diamyaraam.wallet.repository`
package com.diamyaraam.wallet.repository;

// Import de la classe `AuditFinancier` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.AuditFinancier;
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
// Déclaration de l'interface `AuditFinancierRepository` : repository Spring Data JPA de l'entité `AuditFinancier` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface AuditFinancierRepository extends JpaRepository<AuditFinancier, UUID> {
    // Méthode abstraite (contrat) `findByUserIdOrderByCreatedAtDesc` — paramètres : `userId` (identifiant UUID) ; retourne : liste de AuditFinancier ; intention : recherche par (find by user id order by created at desc)
    List<AuditFinancier> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
