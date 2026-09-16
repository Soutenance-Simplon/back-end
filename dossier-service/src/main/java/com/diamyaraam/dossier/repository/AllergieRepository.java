package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.Allergie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AllergieRepository extends JpaRepository<Allergie, UUID> {
    List<Allergie> findByDossierMedicalId(UUID dossierMedicalId);
    List<Allergie> findByDossierMedicalIdAndEstCritiqueTrue(UUID dossierMedicalId);
}
