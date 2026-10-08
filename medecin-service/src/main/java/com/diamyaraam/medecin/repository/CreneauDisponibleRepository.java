// Déclaration du package Java : `com.diamyaraam.medecin.repository`
package com.diamyaraam.medecin.repository;

// Import de la classe `CreneauDisponible` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.CreneauDisponible;
// Import de la classe `StatutCreneau` (paquet com.diamyaraam.medecin.entity.CreneauDisponible)
import com.diamyaraam.medecin.entity.CreneauDisponible.StatutCreneau;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Query` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Query;
// Import de la classe `Param` (paquet org.springframework.data.repository.query)
import org.springframework.data.repository.query.Param;
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
// Déclaration de l'interface `CreneauDisponibleRepository` : repository Spring Data JPA de l'entité `CreneauDisponible` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface CreneauDisponibleRepository extends JpaRepository<CreneauDisponible, UUID> {

    // Requête JPQL/SQL personnalisée : SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId ORD…
    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId ORDER BY c.dateHeureDebut ASC")
    // Méthode abstraite (contrat) `findByMedecinIdOrderByDateHeureDebutAsc` — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de CreneauDisponible ; intention : recherche par (find by medecin id order by date heure debut asc)
    List<CreneauDisponible> findByMedecinIdOrderByDateHeureDebutAsc(@Param("medecinId") UUID medecinId);

    // Requête JPQL/SQL personnalisée : SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND…
    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.statut = :statut ORDER BY c.dateHeureDebut ASC")
    // Méthode abstraite (contrat) `findByMedecinIdAndStatutOrderByDateHeureDebutAsc` — paramètres : `medecinId` (identifiant UUID), `statut` (StatutCreneau) ; retourne : liste de CreneauDisponible ; intention : recherche par (find by medecin id and statut order by date heure debut asc)
    List<CreneauDisponible> findByMedecinIdAndStatutOrderByDateHeureDebutAsc(@Param("medecinId") UUID medecinId, @Param("statut") StatutCreneau statut);

    // Requête JPQL/SQL personnalisée : SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND…
    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.dateHeureDebut BETWEEN :start AND :end ORDER BY c.dateHeureDebut ASC")
    // Méthode abstraite (contrat) `findByMedecinIdAndDateHeureDebutBetween` — paramètres : `medecinId` (identifiant UUID), `start` (date-heure), `end` (date-heure) ; retourne : liste de CreneauDisponible ; intention : recherche par (find by medecin id and date heure debut between)
    List<CreneauDisponible> findByMedecinIdAndDateHeureDebutBetween(@Param("medecinId") UUID medecinId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Requête JPQL/SQL personnalisée : SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND…
    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.dateHeureDebut < :end AND c.dateHeureFin > :start ORDER BY c.dateHeureDebut ASC")
    // Méthode `findOverlappingCreneaux` ; retourne : liste de CreneauDisponible ; intention : recherche (find overlapping creneaux)
    List<CreneauDisponible> findOverlappingCreneaux(
            // Paramètre `medecinId` de type identifiant UUID — lie le paramètre java au paramètre nommé de la requête
            @Param("medecinId") UUID medecinId,
            // Paramètre `start` de type date-heure — lie le paramètre java au paramètre nommé de la requête
            @Param("start") LocalDateTime start,
            // Paramètre `end` de type date-heure — lie le paramètre java au paramètre nommé de la requête
            @Param("end") LocalDateTime end);
}

