package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.Consultation;
import com.diamyaraam.dossier.entity.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    List<Consultation> findByDossierMedical(DossierMedical dossierMedical);
}
