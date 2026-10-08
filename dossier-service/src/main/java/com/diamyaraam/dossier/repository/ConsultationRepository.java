// Déclaration du package Java : `com.diamyaraam.dossier.repository`
package com.diamyaraam.dossier.repository;

// Import de la classe `Consultation` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.Consultation;
// Import de la classe `DossierMedical` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.DossierMedical;
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
// Déclaration de l'interface `ConsultationRepository` : repository Spring Data JPA de l'entité `Consultation` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    // Méthode abstraite (contrat) `findByDossierMedical` — paramètres : `dossierMedical` (DossierMedical) ; retourne : liste de Consultation ; intention : recherche par (find by dossier medical)
    List<Consultation> findByDossierMedical(DossierMedical dossierMedical);
}
