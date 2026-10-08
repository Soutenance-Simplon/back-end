// Déclaration du package Java : `com.diamyaraam.dossier.repository`
package com.diamyaraam.dossier.repository;

// Import de la classe `MaladieCronique` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.MaladieCronique;
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
// Déclaration de l'interface `MaladieCroniqueRepository` : repository Spring Data JPA de l'entité `MaladieCronique` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface MaladieCroniqueRepository extends JpaRepository<MaladieCronique, UUID> {
    // Méthode abstraite (contrat) `findByDossierMedicalId` — paramètres : `dossierMedicalId` (identifiant UUID) ; retourne : liste de MaladieCronique ; intention : recherche par (find by dossier medical id)
    List<MaladieCronique> findByDossierMedicalId(UUID dossierMedicalId);
    // Méthode abstraite (contrat) `findByDossierMedicalIdAndArchiveFalse` — paramètres : `dossierMedicalId` (identifiant UUID) ; retourne : liste de MaladieCronique ; intention : recherche par (find by dossier medical id and archive false)
    List<MaladieCronique> findByDossierMedicalIdAndArchiveFalse(UUID dossierMedicalId);
}
