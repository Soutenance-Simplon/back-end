package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.AntecedentMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AntecedentMedicalRepository extends JpaRepository<AntecedentMedical, UUID> {
    List<AntecedentMedical> findByDossierMedicalId(UUID dossierMedicalId);
}
