// Déclaration du package Java : `com.diamyaraam.medecin.service`
package com.diamyaraam.medecin.service;

// Import de la classe `CreneauDisponible` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.CreneauDisponible;
// Import de la classe `DisponibiliteMedecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
// Import de la classe `Medecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.Medecin;
// Import de la classe `CreneauDisponibleRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.CreneauDisponibleRepository;
// Import de la classe `DisponibiliteMedecinRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.DisponibiliteMedecinRepository;
// Import de la classe `MedecinRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.MedecinRepository;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Composant de la couche métier (service Spring)
@Service
// Déclaration de la classe `PlanningService` (rôle : porte la logique métier)
public class PlanningService {

    // Attribut `medecinRepository` de type MedecinRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final MedecinRepository medecinRepository;
    // Attribut `disponibiliteRepository` de type DisponibiliteMedecinRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final DisponibiliteMedecinRepository disponibiliteRepository;
    // Attribut `creneauRepository` de type CreneauDisponibleRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final CreneauDisponibleRepository creneauRepository;

    // Constructeur de `PlanningService`
    public PlanningService(
            // Paramètre `medecinRepository` de type MedecinRepository
            MedecinRepository medecinRepository,
            // Paramètre `disponibiliteRepository` de type DisponibiliteMedecinRepository
            DisponibiliteMedecinRepository disponibiliteRepository,
            // Paramètre `creneauRepository` de type CreneauDisponibleRepository
            CreneauDisponibleRepository creneauRepository) {
        // Initialise l'attribut `medecinRepository` avec la valeur de medecinRepository
        this.medecinRepository = medecinRepository;
        // Initialise l'attribut `disponibiliteRepository` avec la valeur de disponibiliteRepository
        this.disponibiliteRepository = disponibiliteRepository;
        // Initialise l'attribut `creneauRepository` avec la valeur de creneauRepository
        this.creneauRepository = creneauRepository;
    }

    // Méthode `resolveMedecin` (privée) — paramètres : `medecinId` (identifiant UUID) ; retourne : Medecin ; intention : résout (resolve medecin)
    private Medecin resolveMedecin(UUID medecinId) {
        // Condition : exécute le bloc suivant seulement si `medecinId == null`
        if (medecinId == null) {
            // Retourne le résultat de la requête findAll exécutée via medecinRepository
            return medecinRepository.findAll().stream().findFirst()
                    // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Aucun médecin…`
                    .orElseThrow(() -> new IllegalArgumentException("Aucun médecin disponible en base."));
        }
        // Retourne le résultat de la requête findById exécutée via medecinRepository
        return medecinRepository.findById(medecinId)
                // Enchaînement : appelle `or(() -> medecinRepository.findByUserId(medecinId))`
                .or(() -> medecinRepository.findByUserId(medecinId))
                // Enchaînement : appelle `or(() -> medecinRepository.findAll().stream().findFi…`
                .or(() -> medecinRepository.findAll().stream().findFirst())
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Médecin intro…`
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable."));
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `ajouterDisponibilite` (publique) — paramètres : `medecinId` (identifiant UUID), `disponibilite` (DisponibiliteMedecin) ; retourne : DisponibiliteMedecin ; intention : ajoute (ajouter disponibilite)
    public DisponibiliteMedecin ajouterDisponibilite(UUID medecinId, DisponibiliteMedecin disponibilite) {
        // Déclare la variable `m` (Medecin) initialisée avec `resolveMedecin(medecinId)`
        Medecin m = resolveMedecin(medecinId);
        // Renseigne la propriété Medecin de `disponibilite` avec la valeur de m
        disponibilite.setMedecin(m);
        // Retourne l'enregistrement en base de disponibilite via disponibiliteRepository
        return disponibiliteRepository.save(disponibilite);
    }

    // Méthode `getDisponibilites` (publique) — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de DisponibiliteMedecin ; intention : récupère (get disponibilites)
    public List<DisponibiliteMedecin> getDisponibilites(UUID medecinId) {
        // Déclare la variable `m` (Medecin) initialisée avec `resolveMedecin(medecinId)`
        Medecin m = resolveMedecin(medecinId);
        // Retourne le résultat de la requête findByMedecinId exécutée via disponibiliteRepository
        return disponibiliteRepository.findByMedecinId(m.getId());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `creerCreneau` (publique) — paramètres : `medecinId` (identifiant UUID), `start` (date-heure), `end` (date-heure), `type` (DisponibiliteMedecin.TypeConsultation) ; retourne : CreneauDisponible ; intention : crée (creer creneau)
    public CreneauDisponible creerCreneau(UUID medecinId, LocalDateTime start, LocalDateTime end, DisponibiliteMedecin.TypeConsultation type) {
        // Condition : exécute le bloc suivant seulement si `start == null || end == null`
        if (start == null || end == null) {
            // Lève l'exception IllegalArgumentException avec le message « Les dates de début et de fin du créneau sont obligatoires. »
            throw new IllegalArgumentException("Les dates de début et de fin du créneau sont obligatoires.");
        }
        // Condition : exécute le bloc suivant seulement si `start.isBefore(LocalDateTime.now())`
        if (start.isBefore(LocalDateTime.now())) {
            // Lève l'exception IllegalArgumentException avec le message « Impossible de créer un créneau pour une date ou une heure passée. »
            throw new IllegalArgumentException("Impossible de créer un créneau pour une date ou une heure passée.");
        }
        // Condition : exécute le bloc suivant seulement si `!end.isAfter(start)`
        if (!end.isAfter(start)) {
            // Lève l'exception IllegalArgumentException avec le message « L'heure de fin doit être strictement postérieure à l'heure de début. »
            throw new IllegalArgumentException("L'heure de fin doit être strictement postérieure à l'heure de début.");
        }

        // Déclare la variable `m` (Medecin) initialisée avec `resolveMedecin(medecinId)`
        Medecin m = resolveMedecin(medecinId);

        // Vérification de collision / chevauchement de créneaux
        List<CreneauDisponible> conflits = creneauRepository.findOverlappingCreneaux(m.getId(), start, end);
        // Condition : exécute le bloc suivant seulement si `!conflits.isEmpty()`
        if (!conflits.isEmpty()) {
            // Déclare la variable `conflit` (CreneauDisponible) initialisée avec `conflits.get(0)`
            CreneauDisponible conflit = conflits.get(0);
            // Déclare la variable `hDebut` (chaîne de caractères) initialisée avec `String.format("%02d:%02d", conflit.getDateHeureDebut().getHour(), conflit.getDa…`
            String hDebut = String.format("%02d:%02d", conflit.getDateHeureDebut().getHour(), conflit.getDateHeureDebut().getMinute());
            // Déclare la variable `hFin` (chaîne de caractères) initialisée avec `String.format("%02d:%02d", conflit.getDateHeureFin().getHour(), conflit.getDate…`
            String hFin = String.format("%02d:%02d", conflit.getDateHeureFin().getHour(), conflit.getDateHeureFin().getMinute());
            // Lève l'exception IllegalStateException avec le message « Collision de créneau détectée : un créneau existe déjà de »
            throw new IllegalStateException("Collision de créneau détectée : un créneau existe déjà de " + hDebut + " à " + hFin + ".");
        }

        // Déclare la variable `c` (CreneauDisponible) initialisée avec une nouvelle instance de CreneauDisponible
        CreneauDisponible c = new CreneauDisponible();
        // Renseigne la propriété Medecin de `c` avec la valeur de m
        c.setMedecin(m);
        // Renseigne la propriété DateHeureDebut de `c` avec la valeur de start
        c.setDateHeureDebut(start);
        // Renseigne la propriété DateHeureFin de `c` avec la valeur de end
        c.setDateHeureFin(end);
        // Renseigne la propriété TypeConsultation de `c` avec `type != null ? type : DisponibiliteMedecin.TypeConsultation.TELECONSULTATION`
        c.setTypeConsultation(type != null ? type : DisponibiliteMedecin.TypeConsultation.TELECONSULTATION);
        // Renseigne la propriété Statut de `c` avec la valeur de CreneauDisponible.StatutCreneau.DISPONIBLE
        c.setStatut(CreneauDisponible.StatutCreneau.DISPONIBLE);
        // Retourne l'enregistrement en base de c via creneauRepository
        return creneauRepository.save(c);
    }


    // Méthode `getCreneauxDisponibles` (publique) — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de CreneauDisponible ; intention : récupère (get creneaux disponibles)
    public List<CreneauDisponible> getCreneauxDisponibles(UUID medecinId) {
        // Déclare la variable `m` (Medecin) initialisée avec `resolveMedecin(medecinId)`
        Medecin m = resolveMedecin(medecinId);
        // Retourne le résultat de la requête findByMedecinIdAndStatutOrderByDateHeureDebutAsc exécutée via creneauRepository
        return creneauRepository.findByMedecinIdAndStatutOrderByDateHeureDebutAsc(m.getId(), CreneauDisponible.StatutCreneau.DISPONIBLE)
                // Enchaînement : appelle `stream()`
                .stream()
                // Enchaînement : appelle `filter(c -> c.getDateHeureDebut().isAfter(LocalDateTime.…`
                .filter(c -> c.getDateHeureDebut().isAfter(LocalDateTime.now()))
                // Enchaînement : appelle `toList();`
                .toList();
    }

    // Méthode `getTousLesCreneaux` (publique) — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de CreneauDisponible ; intention : récupère (get tous les creneaux)
    public List<CreneauDisponible> getTousLesCreneaux(UUID medecinId) {
        // Déclare la variable `m` (Medecin) initialisée avec `resolveMedecin(medecinId)`
        Medecin m = resolveMedecin(medecinId);
        // Retourne le résultat de la requête findByMedecinIdOrderByDateHeureDebutAsc exécutée via creneauRepository
        return creneauRepository.findByMedecinIdOrderByDateHeureDebutAsc(m.getId());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `changerStatutCreneau` (publique) — paramètres : `creneauId` (identifiant UUID), `nouveauStatut` (CreneauDisponible.StatutCreneau) ; retourne : CreneauDisponible
    public CreneauDisponible changerStatutCreneau(UUID creneauId, CreneauDisponible.StatutCreneau nouveauStatut) {
        // Déclare la variable `creneau` (CreneauDisponible) initialisée avec le résultat de la requête findById exécutée via creneauRepository
        CreneauDisponible creneau = creneauRepository.findById(creneauId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Créneau intro…`
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable avec ID: " + creneauId));
        // Renseigne la propriété Statut de `creneau` avec la valeur de nouveauStatut
        creneau.setStatut(nouveauStatut);
        // Retourne l'enregistrement en base de creneau via creneauRepository
        return creneauRepository.save(creneau);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `supprimerCreneau` (publique) — paramètres : `creneauId` (identifiant UUID) ; retourne : aucune valeur ; intention : supprime (supprimer creneau)
    public void supprimerCreneau(UUID creneauId) {
        // Supprime des données en base via creneauRepository : creneauRepository.deleteById(creneauId);
        creneauRepository.deleteById(creneauId);
    }
}
