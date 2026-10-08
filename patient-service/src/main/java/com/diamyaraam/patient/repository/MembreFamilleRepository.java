// Déclaration du package Java : `com.diamyaraam.patient.repository`
package com.diamyaraam.patient.repository;

// Import de la classe `MembreFamille` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.MembreFamille;
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
// Déclaration de l'interface `MembreFamilleRepository` : repository Spring Data JPA de l'entité `MembreFamille` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface MembreFamilleRepository extends JpaRepository<MembreFamille, UUID> {
    // Méthode abstraite (contrat) `findByParentUserId` — paramètres : `parentUserId` (identifiant UUID) ; retourne : liste de MembreFamille ; intention : recherche par (find by parent user id)
    List<MembreFamille> findByParentUserId(UUID parentUserId);
}
