// Déclaration du package Java : `com.diamyaraam.wallet.repository`
package com.diamyaraam.wallet.repository;

// Import de la classe `Portefeuille` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Portefeuille;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `PortefeuilleRepository` : repository Spring Data JPA de l'entité `Portefeuille` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface PortefeuilleRepository extends JpaRepository<Portefeuille, UUID> {
    // Méthode abstraite (contrat) `findByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : valeur optionnelle de Portefeuille ; intention : recherche par (find by user id)
    Optional<Portefeuille> findByUserId(UUID userId);
    // Méthode abstraite (contrat) `existsByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : booléen ; intention : teste l'existence de (exists by user id)
    boolean existsByUserId(UUID userId);
}
