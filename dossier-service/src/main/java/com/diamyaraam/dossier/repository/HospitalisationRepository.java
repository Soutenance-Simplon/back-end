package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.DossierMedical;
import com.diamyaraam.dossier.entity.Hospitalisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HospitalisationRepository extends JpaRepository<Hospitalisation, UUID> {
    List<Hospitalisation> findByDossierMedical(DossierMedical dossierMedical);
}
