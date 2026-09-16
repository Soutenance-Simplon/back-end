package com.diamyaraam.medecin.repository;

import com.diamyaraam.medecin.entity.Medecin;
import com.diamyaraam.medecin.entity.Medecin.StatutMedecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, UUID> {

    Optional<Medecin> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    List<Medecin> findByStatutMedecinAndIsVerifiedTrue(StatutMedecin status);

    @Query("SELECT m FROM Medecin m WHERE m.isVerified = true AND m.statutMedecin = 'ACTIF' " +
           "AND (:specialite IS NULL OR LOWER(m.onmsReference.specialite) LIKE LOWER(CONCAT('%', :specialite, '%'))) " +
           "AND (:region IS NULL OR LOWER(m.onmsReference.region) LIKE LOWER(CONCAT('%', :region, '%')))")
    List<Medecin> searchMedecins(@Param("specialite") String specialite, @Param("region") String region);
}
