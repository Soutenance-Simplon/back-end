package com.diamyaraam.medecin.service;

import com.diamyaraam.medecin.entity.CreneauDisponible;
import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
import com.diamyaraam.medecin.entity.Medecin;
import com.diamyaraam.medecin.repository.CreneauDisponibleRepository;
import com.diamyaraam.medecin.repository.DisponibiliteMedecinRepository;
import com.diamyaraam.medecin.repository.MedecinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PlanningService {

    private final MedecinRepository medecinRepository;
    private final DisponibiliteMedecinRepository disponibiliteRepository;
    private final CreneauDisponibleRepository creneauRepository;

    public PlanningService(
            MedecinRepository medecinRepository,
            DisponibiliteMedecinRepository disponibiliteRepository,
            CreneauDisponibleRepository creneauRepository) {
        this.medecinRepository = medecinRepository;
        this.disponibiliteRepository = disponibiliteRepository;
        this.creneauRepository = creneauRepository;
    }

    private Medecin resolveMedecin(UUID medecinId) {
        if (medecinId == null) {
            return medecinRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Aucun médecin disponible en base."));
        }
        return medecinRepository.findById(medecinId)
                .or(() -> medecinRepository.findByUserId(medecinId))
                .or(() -> medecinRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable."));
    }

    @Transactional
    public DisponibiliteMedecin ajouterDisponibilite(UUID medecinId, DisponibiliteMedecin disponibilite) {
        Medecin m = resolveMedecin(medecinId);
        disponibilite.setMedecin(m);
        return disponibiliteRepository.save(disponibilite);
    }

    public List<DisponibiliteMedecin> getDisponibilites(UUID medecinId) {
        Medecin m = resolveMedecin(medecinId);
        return disponibiliteRepository.findByMedecinId(m.getId());
    }

    @Transactional
    public CreneauDisponible creerCreneau(UUID medecinId, LocalDateTime start, LocalDateTime end, DisponibiliteMedecin.TypeConsultation type) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Les dates de début et de fin du créneau sont obligatoires.");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("L'heure de fin doit être strictement postérieure à l'heure de début.");
        }

        Medecin m = resolveMedecin(medecinId);

        // Vérification de collision / chevauchement de créneaux
        List<CreneauDisponible> conflits = creneauRepository.findOverlappingCreneaux(m.getId(), start, end);
        if (!conflits.isEmpty()) {
            CreneauDisponible conflit = conflits.get(0);
            String hDebut = String.format("%02d:%02d", conflit.getDateHeureDebut().getHour(), conflit.getDateHeureDebut().getMinute());
            String hFin = String.format("%02d:%02d", conflit.getDateHeureFin().getHour(), conflit.getDateHeureFin().getMinute());
            throw new IllegalStateException("Collision de créneau détectée : un créneau existe déjà de " + hDebut + " à " + hFin + ".");
        }

        CreneauDisponible c = new CreneauDisponible();
        c.setMedecin(m);
        c.setDateHeureDebut(start);
        c.setDateHeureFin(end);
        c.setTypeConsultation(type != null ? type : DisponibiliteMedecin.TypeConsultation.TELECONSULTATION);
        c.setStatut(CreneauDisponible.StatutCreneau.DISPONIBLE);
        return creneauRepository.save(c);
    }


    public List<CreneauDisponible> getCreneauxDisponibles(UUID medecinId) {
        Medecin m = resolveMedecin(medecinId);
        return creneauRepository.findByMedecinIdAndStatutOrderByDateHeureDebutAsc(m.getId(), CreneauDisponible.StatutCreneau.DISPONIBLE);
    }

    public List<CreneauDisponible> getTousLesCreneaux(UUID medecinId) {
        Medecin m = resolveMedecin(medecinId);
        return creneauRepository.findByMedecinIdOrderByDateHeureDebutAsc(m.getId());
    }

    @Transactional
    public CreneauDisponible changerStatutCreneau(UUID creneauId, CreneauDisponible.StatutCreneau nouveauStatut) {
        CreneauDisponible creneau = creneauRepository.findById(creneauId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable avec ID: " + creneauId));
        creneau.setStatut(nouveauStatut);
        return creneauRepository.save(creneau);
    }

    @Transactional
    public void supprimerCreneau(UUID creneauId) {
        creneauRepository.deleteById(creneauId);
    }
}
