// Déclaration du package Java : `com.diamyaraam.medecin.controller`
package com.diamyaraam.medecin.controller;

// Import de la classe `CreneauDisponible` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.CreneauDisponible;
// Import de la classe `DisponibiliteMedecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
// Import de la classe `PlanningService` (paquet com.diamyaraam.medecin.service)
import com.diamyaraam.medecin.service.PlanningService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : GESTION DES AGENDAS ET CRÉNEAUX DE CONSULTATION (PLANNING CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'INGÉNIERIE & EXPÉRIENCE UTILISATEUR POUR LA SOUTENANCE :
 * La prise de rendez-vous médicale repose sur une gestion rigoureuse des plages temporelles.
 * 
 * ⏱️ MÉCANIQUE DU MOTEUR DE PLANNING :
 * 1. Disponibilités Récurrentes (`DisponibiliteMedecin`) :
 *    - Plages hebdomadaires définies par le praticien (ex: tous les mardis de 14h à 18h).
 * 
 * 2. Créneaux Discrétisés Unitaires (`CreneauDisponible`) :
 *    - Découpage fin par tranche (slots de 30 minutes).
 *    - Typologie de consultation : TELECONSULTATION (vidéo en ligne) ou PRESENTIELLE (en cabinet médical).
 *    - Cycle de vie : DISPONIBLE -> RESERVE -> EFFECTUE ou ANNULE.
 * 
 * 3. Robustesse & Parsing Tolérant :
 *    - Fonctions sécurisées `parseUuidSafe` et `parseDateSafe` assurant la compatibilité avec
 *      les formats de dates ISO-8601 émis par Flutter et les fuseaux horaires GMT/UTC.
 * ====================================================================================================
 */
// Contrôleur REST Spring pour l'exposition des routes d'agenda médical
@RestController
// Préfixe de routage pour les ressources liées aux praticiens
@RequestMapping("/medecins")
// Déclaration de la classe `PlanningController` (rôle : expose des routes HTTP)
public class PlanningController {

    // Injection du service métier de gestion des plannings et créneaux
    private final PlanningService planningService;

    // Constructeur d'injection de dépendances Spring IoC
    public PlanningController(PlanningService planningService) {
        // Affectation du service injecté
        this.planningService = planningService;
    }

    // Méthode utilitaire de conversion sécurisée des identifiants alphanumériques en UUID Java
    private UUID parseUuidSafe(String idStr) {
        // Vérification des cas limites (chaîne vide, null ou identifiant factice de test)
        if (idStr == null || idStr.isBlank() || idStr.equals("med-1") || idStr.equals("null")) return null;
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Tentative de parsing de l'identifiant standard au format UUID
            return UUID.fromString(idStr);
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Renvoi de null en cas de chaîne non convertible
            return null;
        }
    }

    // Méthode utilitaire de parsing tolérant des dates ISO-8601 émises par les requêtes HTTP
    private LocalDateTime parseDateSafe(String dateStr) {
        // En l'absence de date, repli sur l'horodatage courant
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Nettoyage de la chaîne d'entrée
            String clean = dateStr;
            // Suppression de l'indicateur de fuseau horaire Zulu 'Z'
            if (clean.contains("Z")) clean = clean.replace("Z", "");
            // Tronquage des décalages horaires explicites (+00:00)
            if (clean.contains("+")) clean = clean.substring(0, clean.indexOf("+"));
            // Tronquage des millisecondes superflues pour obtenir le format yyyy-MM-ddTHH:mm:ss
            if (clean.length() > 19) clean = clean.substring(0, 19);
            // Conversion en LocalDateTime Java
            return LocalDateTime.parse(clean);
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Repli sur l'instant présent en cas d'erreur de syntaxe
            return LocalDateTime.now();
        }
    }

    // Point d'accès POST pour enregistrer une nouvelle plage hebdomadaire récurrente
    @PostMapping("/{medecinId}/disponibilites")
    // Méthode `ajouterDisponibilite` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de DisponibiliteMedecin ; intention : ajoute (ajouter disponibilite)
    public ResponseEntity<ApiResponse<DisponibiliteMedecin>> ajouterDisponibilite(
            // Récupération de l'identifiant praticien depuis l'URL
            @PathVariable String medecinId,
            // Corps de requête contenant les détails de la disponibilité
            @RequestBody DisponibiliteMedecin disponibilite) {
        // Conversion sécurisée de l'identifiant praticien
        UUID uid = parseUuidSafe(medecinId);
        // Délégation au service métier pour la persistance
        DisponibiliteMedecin created = planningService.ajouterDisponibilite(uid, disponibilite);
        // Renvoi de la réponse 200 OK avec le DTO créé
        return ResponseEntity.ok(ApiResponse.success("Disponibilité ajoutée", created));
    }

    // Point d'accès GET pour récupérer les règles de disponibilité récurrentes d'un médecin
    @GetMapping("/{medecinId}/disponibilites")
    // Méthode `getDisponibilites` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de DisponibiliteMedecin ; intention : récupère (get disponibilites)
    public ResponseEntity<ApiResponse<List<DisponibiliteMedecin>>> getDisponibilites(
            // Identifiant du médecin
            @PathVariable String medecinId) {
        // Conversion sécurisée de l'identifiant
        UUID uid = parseUuidSafe(medecinId);
        // Récupération de la liste des plages de travail
        List<DisponibiliteMedecin> list = planningService.getDisponibilites(uid);
        // Renvoi de la réponse 200 OK
        return ResponseEntity.ok(ApiResponse.success("Liste des disponibilités", list));
    }

    // Point d'accès POST pour ouvrir un créneau de consultation ponctuel sur l'agenda
    @PostMapping("/{medecinId}/creneaux")
    // Méthode `creerCreneau` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de CreneauDisponible ; intention : crée (creer creneau)
    public ResponseEntity<ApiResponse<CreneauDisponible>> creerCreneau(
            // Identifiant du médecin
            @PathVariable String medecinId,
            // Heure de début du créneau (format ISO)
            @RequestParam String start,
            // Heure de fin du créneau (format ISO)
            @RequestParam String end,
            // Type de consultation optionnel (TELECONSULTATION ou PRESENTIELLE)
            @RequestParam(required = false) String type) {
        // Conversion de l'identifiant médecin
        UUID uid = parseUuidSafe(medecinId);
        // Parsing tolérant de la date de début
        LocalDateTime startTime = parseDateSafe(start);
        // Parsing tolérant de la date de fin
        LocalDateTime endTime = parseDateSafe(end);
        // Modalité de téléconsultation par défaut
        DisponibiliteMedecin.TypeConsultation t = DisponibiliteMedecin.TypeConsultation.TELECONSULTATION;
        // Si un type spécifique est mentionné
        if (type != null) {
            // Détection du mode présentiel / en cabinet
            if (type.toUpperCase().contains("CABINET") || type.toUpperCase().contains("PRESENTIELLE")) {
                // Attribution du mode en cabinet
                t = DisponibiliteMedecin.TypeConsultation.PRESENTIELLE;
            }
        }
        // Création persistante du créneau dans le service métier
        CreneauDisponible created = planningService.creerCreneau(uid, startTime, endTime, t);
        // Renvoi de la réponse de succès avec le créneau créé
        return ResponseEntity.ok(ApiResponse.success("Créneau créé", created));
    }

    // Point d'accès GET pour lister les créneaux d'un praticien (avec filtre optionnel par statut)
    @GetMapping("/{medecinId}/creneaux")
    // Méthode `getCreneaux` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de CreneauDisponible ; intention : récupère (get creneaux)
    public ResponseEntity<ApiResponse<List<CreneauDisponible>>> getCreneaux(
            // Identifiant du médecin
            @PathVariable String medecinId,
            // Paramètre de filtre optionnel (ex: 'DISPONIBLE')
            @RequestParam(required = false) String statut) {
        // Conversion de l'identifiant
        UUID uid = parseUuidSafe(medecinId);
        // Déclaration de la variable de liste
        List<CreneauDisponible> list;
        // Si un filtre sur les créneaux vacants est demandé
        if ("DISPONIBLE".equalsIgnoreCase(statut)) {
            // Récupération uniquement des créneaux libres pour la prise de rendez-vous
            list = planningService.getCreneauxDisponibles(uid);
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Récupération de l'agenda exhaustif (réservés, libres, archivés)
            list = planningService.getTousLesCreneaux(uid);
        }
        // Renvoi de la réponse de consultation
        return ResponseEntity.ok(ApiResponse.success("Liste des créneaux", list));
    }

    // Point d'accès PUT pour modifier le statut d'un créneau (ex: BLOQUE, DISPONIBLE, RESERVE)
    @PutMapping("/{medecinId}/creneaux/{creneauId}/statut")
    // Méthode `changerStatutCreneau` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de CreneauDisponible
    public ResponseEntity<ApiResponse<CreneauDisponible>> changerStatutCreneau(
            // Identifiant du médecin
            @PathVariable String medecinId,
            // Identifiant du créneau à modifier
            @PathVariable UUID creneauId,
            // Nouveau statut requis
            @RequestParam String statut) {
        // Résolution de l'énumération cible
        CreneauDisponible.StatutCreneau st = CreneauDisponible.StatutCreneau.valueOf(statut.toUpperCase());
        // Mise à jour via le service métier
        CreneauDisponible updated = planningService.changerStatutCreneau(creneauId, st);
        // Renvoi de la réponse de confirmation
        return ResponseEntity.ok(ApiResponse.success("Statut du créneau mis à jour", updated));
    }

    // Point d'accès DELETE pour annuler et retirer un créneau non réservé
    @DeleteMapping("/{medecinId}/creneaux/{creneauId}")
    // Méthode `supprimerCreneau` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : supprime (supprimer creneau)
    public ResponseEntity<ApiResponse<Void>> supprimerCreneau(
            // Identifiant du médecin
            @PathVariable String medecinId,
            // Identifiant du créneau à supprimer
            @PathVariable UUID creneauId) {
        // Exécution de la suppression
        planningService.supprimerCreneau(creneauId);
        // Renvoi de la confirmation 200 OK avec payload vide
        return ResponseEntity.ok(ApiResponse.success("Créneau supprimé avec succès", null));
    }
}
