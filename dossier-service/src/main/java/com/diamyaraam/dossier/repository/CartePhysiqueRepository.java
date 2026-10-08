// Déclaration du package Java : `com.diamyaraam.dossier.repository`
package com.diamyaraam.dossier.repository;

// Import de la classe `CartePhysique` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.CartePhysique;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `CartePhysiqueRepository` : repository Spring Data JPA de l'entité `CartePhysique` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface CartePhysiqueRepository extends JpaRepository<CartePhysique, UUID> {
    
    // Méthode abstraite (contrat) `findByQrTokenSecurise` — paramètres : `qrTokenSecurise` (chaîne de caractères) ; retourne : valeur optionnelle de CartePhysique ; intention : recherche par (find by qr token securise)
    Optional<CartePhysique> findByQrTokenSecurise(String qrTokenSecurise);
    
    // Méthode abstraite (contrat) `findByNumeroSerie` — paramètres : `numeroSerie` (chaîne de caractères) ; retourne : valeur optionnelle de CartePhysique ; intention : recherche par (find by numero serie)
    Optional<CartePhysique> findByNumeroSerie(String numeroSerie);
    
    // Méthode abstraite (contrat) `findByPatientId` — paramètres : `patientId` (identifiant UUID) ; retourne : liste de CartePhysique ; intention : recherche par (find by patient id)
    List<CartePhysique> findByPatientId(UUID patientId);
    
    // Trouver la carte active d'un patient
    Optional<CartePhysique> findByPatientIdAndStatut(UUID patientId, CartePhysique.StatutCarte statut);
}
