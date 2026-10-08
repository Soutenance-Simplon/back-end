// Déclaration du package Java : `com.diamyaraam.patient.repository`
package com.diamyaraam.patient.repository;

// Import de la classe `PermissionAcces` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.PermissionAcces;
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
// Déclaration de l'interface `PermissionAccesRepository` : repository Spring Data JPA de l'entité `PermissionAcces` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface PermissionAccesRepository extends JpaRepository<PermissionAcces, UUID> {
    // Méthode abstraite (contrat) `findByDossierMedicalId` — paramètres : `dossierMedicalId` (identifiant UUID) ; retourne : liste de PermissionAcces ; intention : recherche par (find by dossier medical id)
    List<PermissionAcces> findByDossierMedicalId(UUID dossierMedicalId);
    // Méthode abstraite (contrat) `findByUtilisateurAutoriseId` — paramètres : `utilisateurAutoriseId` (identifiant UUID) ; retourne : liste de PermissionAcces ; intention : recherche par (find by utilisateur autorise id)
    List<PermissionAcces> findByUtilisateurAutoriseId(UUID utilisateurAutoriseId);
}
