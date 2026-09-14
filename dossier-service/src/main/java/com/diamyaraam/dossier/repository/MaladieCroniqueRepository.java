package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.MaladieCronique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaladieCroniqueRepository extends JpaRepository<MaladieCronique, UUID> {
    List<MaladieCronique> findByDossierMedicalId(UUID dossierMedicalId);
    List<MaladieCronique> findByDossierMedicalIdAndArchiveFalse(UUID dossierMedicalId);
}
