package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, UUID> {
    Optional<DossierMedical> findByPatientId(UUID patientId);
    Optional<DossierMedical> findByCodeQrSecurise(String codeQrSecurise);
}
