package com.diamyaraam.rdv.repository;

import com.diamyaraam.rdv.entity.SalleTeleconsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalleTeleconsultationRepository extends JpaRepository<SalleTeleconsultation, UUID> {
    Optional<SalleTeleconsultation> findByRendezVousId(UUID rendezVousId);
    Optional<SalleTeleconsultation> findByTokenPatient(String tokenPatient);
    Optional<SalleTeleconsultation> findByTokenMedecin(String tokenMedecin);
}
