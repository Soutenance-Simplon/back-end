package com.diamyaraam.medecin.repository;

import com.diamyaraam.medecin.entity.CreneauDisponible;
import com.diamyaraam.medecin.entity.CreneauDisponible.StatutCreneau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreneauDisponibleRepository extends JpaRepository<CreneauDisponible, UUID> {

    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId ORDER BY c.dateHeureDebut ASC")
    List<CreneauDisponible> findByMedecinIdOrderByDateHeureDebutAsc(@Param("medecinId") UUID medecinId);

    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.statut = :statut ORDER BY c.dateHeureDebut ASC")
    List<CreneauDisponible> findByMedecinIdAndStatutOrderByDateHeureDebutAsc(@Param("medecinId") UUID medecinId, @Param("statut") StatutCreneau statut);

    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.dateHeureDebut BETWEEN :start AND :end ORDER BY c.dateHeureDebut ASC")
    List<CreneauDisponible> findByMedecinIdAndDateHeureDebutBetween(@Param("medecinId") UUID medecinId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT c FROM CreneauDisponible c WHERE c.medecin.id = :medecinId AND c.dateHeureDebut < :end AND c.dateHeureFin > :start ORDER BY c.dateHeureDebut ASC")
    List<CreneauDisponible> findOverlappingCreneaux(
            @Param("medecinId") UUID medecinId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

