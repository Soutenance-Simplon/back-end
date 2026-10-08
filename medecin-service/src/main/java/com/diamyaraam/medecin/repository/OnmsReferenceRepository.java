// Déclaration du package Java : `com.diamyaraam.medecin.repository`
package com.diamyaraam.medecin.repository;

// Import de la classe `OnmsReference` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.OnmsReference;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `OnmsReferenceRepository` : repository Spring Data JPA de l'entité `OnmsReference` (clé primaire `Long`) — les opérations CRUD sont générées automatiquement
public interface OnmsReferenceRepository extends JpaRepository<OnmsReference, Long> {
    // Méthode abstraite (contrat) `findByNumeroOrdre` — paramètres : `numeroOrdre` (chaîne de caractères) ; retourne : valeur optionnelle de OnmsReference ; intention : recherche par (find by numero ordre)
    Optional<OnmsReference> findByNumeroOrdre(String numeroOrdre);
    // Méthode abstraite (contrat) `existsByNumeroOrdre` — paramètres : `numeroOrdre` (chaîne de caractères) ; retourne : booléen ; intention : teste l'existence de (exists by numero ordre)
    boolean existsByNumeroOrdre(String numeroOrdre);
}
