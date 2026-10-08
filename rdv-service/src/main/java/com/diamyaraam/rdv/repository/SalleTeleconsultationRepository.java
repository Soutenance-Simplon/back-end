// Déclaration du package Java : `com.diamyaraam.rdv.repository`
package com.diamyaraam.rdv.repository;

// Import de la classe `SalleTeleconsultation` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.SalleTeleconsultation;
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
// Déclaration de l'interface `SalleTeleconsultationRepository` : repository Spring Data JPA de l'entité `SalleTeleconsultation` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface SalleTeleconsultationRepository extends JpaRepository<SalleTeleconsultation, UUID> {
    // Méthode abstraite (contrat) `findByRendezVousId` — paramètres : `rendezVousId` (identifiant UUID) ; retourne : valeur optionnelle de SalleTeleconsultation ; intention : recherche par (find by rendez vous id)
    Optional<SalleTeleconsultation> findByRendezVousId(UUID rendezVousId);
    // Méthode abstraite (contrat) `findByTokenPatient` — paramètres : `tokenPatient` (chaîne de caractères) ; retourne : valeur optionnelle de SalleTeleconsultation ; intention : recherche par (find by token patient)
    Optional<SalleTeleconsultation> findByTokenPatient(String tokenPatient);
    // Méthode abstraite (contrat) `findByTokenMedecin` — paramètres : `tokenMedecin` (chaîne de caractères) ; retourne : valeur optionnelle de SalleTeleconsultation ; intention : recherche par (find by token medecin)
    Optional<SalleTeleconsultation> findByTokenMedecin(String tokenMedecin);
}
