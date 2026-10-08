// Déclaration du package Java : `com.diamyaraam.dossier.repository`
package com.diamyaraam.dossier.repository;

// Import de la classe `DossierMedical` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.DossierMedical;
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
// Déclaration de l'interface `DossierMedicalRepository` : repository Spring Data JPA de l'entité `DossierMedical` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, UUID> {
    // Méthode abstraite (contrat) `findByPatientId` — paramètres : `patientId` (identifiant UUID) ; retourne : valeur optionnelle de DossierMedical ; intention : recherche par (find by patient id)
    Optional<DossierMedical> findByPatientId(UUID patientId);
    // Méthode abstraite (contrat) `findByCodeQrSecurise` — paramètres : `codeQrSecurise` (chaîne de caractères) ; retourne : valeur optionnelle de DossierMedical ; intention : recherche par (find by code qr securise)
    Optional<DossierMedical> findByCodeQrSecurise(String codeQrSecurise);
}
