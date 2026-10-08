// Déclaration du package Java : `com.diamyaraam.medecin.repository`
package com.diamyaraam.medecin.repository;

// Import de la classe `DisponibiliteMedecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
// Import de la classe `Medecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.Medecin;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Query` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Query;
// Import de la classe `Param` (paquet org.springframework.data.repository.query)
import org.springframework.data.repository.query.Param;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `DisponibiliteMedecinRepository` : repository Spring Data JPA de l'entité `DisponibiliteMedecin` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface DisponibiliteMedecinRepository extends JpaRepository<DisponibiliteMedecin, UUID> {
    // Méthode abstraite (contrat) `findByMedecinAndActifTrue` — paramètres : `medecin` (Medecin) ; retourne : liste de DisponibiliteMedecin ; intention : recherche par (find by medecin and actif true)
    List<DisponibiliteMedecin> findByMedecinAndActifTrue(Medecin medecin);

    // Requête JPQL/SQL personnalisée : SELECT d FROM DisponibiliteMedecin d WHERE d.medecin.id = :medecinId
    @Query("SELECT d FROM DisponibiliteMedecin d WHERE d.medecin.id = :medecinId")
    // Méthode abstraite (contrat) `findByMedecinId` — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de DisponibiliteMedecin ; intention : recherche par (find by medecin id)
    List<DisponibiliteMedecin> findByMedecinId(@Param("medecinId") UUID medecinId);
}
