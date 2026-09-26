package com.diamyaraam.dossier.repository;

import com.diamyaraam.dossier.entity.CartePhysique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartePhysiqueRepository extends JpaRepository<CartePhysique, UUID> {
    
    Optional<CartePhysique> findByQrTokenSecurise(String qrTokenSecurise);
    
    Optional<CartePhysique> findByNumeroSerie(String numeroSerie);
    
    List<CartePhysique> findByPatientId(UUID patientId);
    
    // Trouver la carte active d'un patient
    Optional<CartePhysique> findByPatientIdAndStatut(UUID patientId, CartePhysique.StatutCarte statut);
}
