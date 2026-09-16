package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.DocumentMedical;
import com.diamyaraam.dossier.entity.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, UUID> {
    List<DocumentMedical> findByDossierMedical(DossierMedical dossierMedical);
}
