package com.diamyaraam.rdv.repository;

import com.diamyaraam.rdv.entity.RendezVous;
import com.diamyaraam.rdv.entity.RendezVous.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, UUID> {
    List<RendezVous> findByPatientId(UUID patientId);
    List<RendezVous> findByMedecinId(UUID medecinId);
    List<RendezVous> findByMedecinIdAndStatut(UUID medecinId, StatutRendezVous statut);

    /** Utilisé par TeleconsultationReminderService : rappels 10 min avant */
    List<RendezVous> findByTypeConsultationAndStatutAndDateHeureSouhaiteeBetween(
            RendezVous.TypeConsultation typeConsultation,
            StatutRendezVous statut,
            LocalDateTime debut,
            LocalDateTime fin
    );
}

