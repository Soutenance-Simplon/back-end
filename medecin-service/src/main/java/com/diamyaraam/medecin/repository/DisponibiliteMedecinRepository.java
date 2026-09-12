package com.diamyaraam.medecin.repository;

import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
import com.diamyaraam.medecin.entity.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisponibiliteMedecinRepository extends JpaRepository<DisponibiliteMedecin, UUID> {
    List<DisponibiliteMedecin> findByMedecinAndActifTrue(Medecin medecin);

    @Query("SELECT d FROM DisponibiliteMedecin d WHERE d.medecin.id = :medecinId")
    List<DisponibiliteMedecin> findByMedecinId(@Param("medecinId") UUID medecinId);
}
