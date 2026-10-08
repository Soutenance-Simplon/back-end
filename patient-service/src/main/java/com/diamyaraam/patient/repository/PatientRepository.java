// Déclaration du package Java : `com.diamyaraam.patient.repository`
package com.diamyaraam.patient.repository;

// Import de la classe `Patient` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.Patient;
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
// Déclaration de l'interface `PatientRepository` : repository Spring Data JPA de l'entité `Patient` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    // Méthode abstraite (contrat) `findByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : valeur optionnelle de Patient ; intention : recherche par (find by user id)
    Optional<Patient> findByUserId(UUID userId);
    // Méthode abstraite (contrat) `existsByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : booléen ; intention : teste l'existence de (exists by user id)
    boolean existsByUserId(UUID userId);
}
