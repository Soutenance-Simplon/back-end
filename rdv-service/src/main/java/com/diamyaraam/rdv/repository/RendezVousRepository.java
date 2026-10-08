// Déclaration du package Java : `com.diamyaraam.rdv.repository`
package com.diamyaraam.rdv.repository;

// Import de la classe `RendezVous` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.RendezVous;
// Import de la classe `StatutRendezVous` (paquet com.diamyaraam.rdv.entity.RendezVous)
import com.diamyaraam.rdv.entity.RendezVous.StatutRendezVous;
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
// Déclaration de l'interface `RendezVousRepository` : repository Spring Data JPA de l'entité `RendezVous` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface RendezVousRepository extends JpaRepository<RendezVous, UUID> {
    // Méthode abstraite (contrat) `findByPatientId` — paramètres : `patientId` (identifiant UUID) ; retourne : liste de RendezVous ; intention : recherche par (find by patient id)
    List<RendezVous> findByPatientId(UUID patientId);
    // Méthode abstraite (contrat) `findByMedecinId` — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de RendezVous ; intention : recherche par (find by medecin id)
    List<RendezVous> findByMedecinId(UUID medecinId);
    // Méthode abstraite (contrat) `findByMedecinIdAndStatut` — paramètres : `medecinId` (identifiant UUID), `statut` (StatutRendezVous) ; retourne : liste de RendezVous ; intention : recherche par (find by medecin id and statut)
    List<RendezVous> findByMedecinIdAndStatut(UUID medecinId, StatutRendezVous statut);

    /** Utilisé par TeleconsultationReminderService : rappels 10 min avant */
    List<RendezVous> findByTypeConsultationAndStatutAndDateHeureSouhaiteeBetween(
            // Paramètre `typeConsultation` de type RendezVous.TypeConsultation
            RendezVous.TypeConsultation typeConsultation,
            // Paramètre `statut` de type StatutRendezVous
            StatutRendezVous statut,
            // Paramètre `debut` de type date-heure
            LocalDateTime debut,
            // Paramètre `fin` de type date-heure
            LocalDateTime fin
    );
}

