package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.DossierMedical;
import com.diamyaraam.dossier.entity.Vaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VaccinationRepository extends JpaRepository<Vaccination, UUID> {
    List<Vaccination> findByDossierMedical(DossierMedical dossierMedical);
}
