// Déclaration du package Java : `com.diamyaraam.rdv.controller`
package com.diamyaraam.rdv.controller;

// Import de la classe `TeleconsultationJoinResponse` (paquet com.diamyaraam.rdv.dto)
import com.diamyaraam.rdv.dto.TeleconsultationJoinResponse;
// Import de la classe `RendezVous` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.RendezVous;
// Import de la classe `RendezVousService` (paquet com.diamyaraam.rdv.service)
import com.diamyaraam.rdv.service.RendezVousService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `DateTimeFormat` (paquet org.springframework.format.annotation)
import org.springframework.format.annotation.DateTimeFormat;
// Import de la classe `HttpStatus` (paquet org.springframework.http)
import org.springframework.http.HttpStatus;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : RENDEZ-VOUS & SALLES DE TÉLÉCONSULTATION (RENDEZ-VOUS CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS CLÉS POUR LA SOUTENANCE :
 * Ce contrôleur gère l'intégralité du cycle de vie des consultations médicales.
 * 
 * 🔄 MACHINE À ÉTATS D'UN RENDEZ-VOUS MÉDICAL :
 * 1. DEMANDE / EN_ATTENTE : Le patient choisit un créneau et exprime son motif de consultation.
 * 2. ACCEPTE : Le médecin valide la demande dans son agenda.
 * 3. CONFIRME : Le paiement (Mobile Money / Wallet) est validé avec référence de transaction.
 * 4. EN_COURS : La séance de téléconsultation vidéo démarre via la salle LiveKit sécurisée.
 * 5. TERMINE : La consultation est close, le médecin rédige l'ordonnance dans dossier-service.
 * 6. ANNULE : Annulation avec motif justificatif par l'une des deux parties.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /rdv »
@RequestMapping("/rdv")
// Autorise les requêtes cross-origin (CORS) pour cette route
@CrossOrigin(origins = "*", allowedHeaders = "*")
// Déclaration de la classe `RendezVousController` (rôle : expose des routes HTTP)
public class RendezVousController {

    // Attribut `rdvService` de type RendezVousService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RendezVousService rdvService;

    // Constructeur de `RendezVousController` — paramètres : `rdvService` (RendezVousService) (injection des dépendances par Spring)
    public RendezVousController(RendezVousService rdvService) {
        // Initialise l'attribut `rdvService` avec la valeur de rdvService
        this.rdvService = rdvService;
    }


    // Route HTTP POST sur le chemin «  »
    @PostMapping({"", "/creer"})
    // Méthode `creerRendezVous` (publique) — paramètres : `body` (dictionnaire clé/valeur) ; retourne : réponse HTTP contenant enveloppe ApiResponse de RendezVous ; intention : crée (creer rendez vous)
    public ResponseEntity<ApiResponse<RendezVous>> creerRendezVous(@RequestBody Map<String, Object> body) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `patientIdStr` (chaîne de caractères) initialisée avec `(String) body.get("patient_id")`
            String patientIdStr = (String) body.get("patient_id");
            // Déclare la variable `medecinIdStr` (chaîne de caractères) initialisée avec `(String) body.get("medecin_id")`
            String medecinIdStr = (String) body.get("medecin_id");
            // Déclare la variable `patientId` (identifiant UUID) initialisée avec un UUID aléatoire ((patientIdStr != null && patientIdStr.length() == 36) ? UUI…)
            UUID patientId = (patientIdStr != null && patientIdStr.length() == 36) ? UUID.fromString(patientIdStr) : UUID.randomUUID();
            // Déclare la variable `medecinId` (identifiant UUID) initialisée avec un UUID aléatoire ((medecinIdStr != null && medecinIdStr.length() == 36) ? UUI…)
            UUID medecinId = (medecinIdStr != null && medecinIdStr.length() == 36) ? UUID.fromString(medecinIdStr) : UUID.randomUUID();
            // Déclare la variable `motif` (chaîne de caractères) initialisée avec `body.get("motif") != null ? body.get("motif").toString() : "Consultation médica…`
            String motif = body.get("motif") != null ? body.get("motif").toString() : "Consultation médicale";
            // Déclare la variable `dateStr` (chaîne de caractères) initialisée avec `(String) body.get("date_heure")`
            String dateStr = (String) body.get("date_heure");
            // Déclare la variable `dateHeure` (date-heure) initialisée avec la date et l'heure courantes (dateStr != null ? LocalDateTime.parse(dateStr.split(".")[0]…)
            LocalDateTime dateHeure = dateStr != null ? LocalDateTime.parse(dateStr.split("\\.")[0]) : LocalDateTime.now();
            // Déclare la variable `typeStr` (chaîne de caractères) initialisée avec `body.get("type_consultation") != null ? body.get("type_consultation").toString(…`
            String typeStr = body.get("type_consultation") != null ? body.get("type_consultation").toString() : "TELECONSULTATION";

            // Déclare la variable `rdv` (RendezVous) initialisée avec `rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure)`
            RendezVous rdv = rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure);
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Renseigne la propriété TypeConsultation de `rdv` avec `RendezVous.TypeConsultation.valueOf(typeStr)`
                rdv.setTypeConsultation(RendezVous.TypeConsultation.valueOf(typeStr));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
            // Affecte à `rdv` l'horodatage courant en millisecondes (rdvService.confirmerPaiementEtRdv(rdv.getId(), "PAY-" + Sys…)
            rdv = rdvService.confirmerPaiementEtRdv(rdv.getId(), "PAY-" + System.currentTimeMillis());
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Rendez-vous créé et confirmé",…
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous créé et confirmé", rdv));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Erreur création RDV: " + e.…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur création RDV: " + e.getMessage()));
        }
    }

    // Route HTTP POST sur le chemin « /demander »
    @PostMapping("/demander")
    // Méthode `demander` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de RendezVous
    public ResponseEntity<ApiResponse<RendezVous>> demander(
            // Paramètre `patientId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID patientId,
            // Paramètre `medecinId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID medecinId,
            // Paramètre `motif` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String motif,
            // Paramètre `dateHeure` de type date-heure — paramètre de requête http (?clé=valeur)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec `rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure)`
        RendezVous rdv = rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Demande de RDV créée", rdv))
        return ResponseEntity.ok(ApiResponse.success("Demande de RDV créée", rdv));
    }

    // Route HTTP PUT sur le chemin « /{id}/accepter »
    @PutMapping("/{id}/accepter")
    // Méthode `accepter` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de RendezVous
    public ResponseEntity<ApiResponse<RendezVous>> accepter(@PathVariable UUID id) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec `rdvService.accepterRendezVous(id)`
        RendezVous rdv = rdvService.accepterRendezVous(id);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("RDV accepté par le médecin", r…
        return ResponseEntity.ok(ApiResponse.success("RDV accepté par le médecin", rdv));
    }

    // Route HTTP PUT sur le chemin « /{id}/confirmer-paiement »
    @PutMapping("/{id}/confirmer-paiement")
    // Méthode `confirmerPaiement` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de RendezVous
    public ResponseEntity<ApiResponse<RendezVous>> confirmerPaiement(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `refPaiement` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String refPaiement) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec `rdvService.confirmerPaiementEtRdv(id, refPaiement)`
        RendezVous rdv = rdvService.confirmerPaiementEtRdv(id, refPaiement);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Paiement validé et RDV confirm…
        return ResponseEntity.ok(ApiResponse.success("Paiement validé et RDV confirmé", rdv));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId} »
    @GetMapping("/patient/{patientId}")
    // Méthode `getByPatient` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de RendezVous ; intention : récupère (get by patient)
    public ResponseEntity<ApiResponse<List<RendezVous>>> getByPatient(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de RendezVous) initialisée avec `rdvService.getByPatient(patientId)`
        List<RendezVous> list = rdvService.getByPatient(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("RDV patient", list))
        return ResponseEntity.ok(ApiResponse.success("RDV patient", list));
    }

    // Route HTTP GET sur le chemin « /medecin/{medecinId} »
    @GetMapping("/medecin/{medecinId}")
    // Méthode `getByMedecin` (publique) — paramètres : `medecinId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de RendezVous ; intention : récupère (get by medecin)
    public ResponseEntity<ApiResponse<List<RendezVous>>> getByMedecin(@PathVariable UUID medecinId) {
        // Déclare la variable `list` (liste de RendezVous) initialisée avec `rdvService.getByMedecin(medecinId)`
        List<RendezVous> list = rdvService.getByMedecin(medecinId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("RDV médecin", list))
        return ResponseEntity.ok(ApiResponse.success("RDV médecin", list));
    }

    // Route HTTP PUT sur le chemin « /{id}/statut »
    @PutMapping("/{id}/statut")
    // Méthode `changerStatut` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de RendezVous
    public ResponseEntity<ApiResponse<RendezVous>> changerStatut(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `nouveauStatut` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String nouveauStatut,
            // Paramètre `raison` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String raison,
            // Paramètre `body` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody(required = false) Map<String, Object> body) {
        // Déclare la variable `statutFinal` (chaîne de caractères) initialisée avec la valeur de nouveauStatut
        String statutFinal = nouveauStatut;
        // Condition : exécute le bloc suivant seulement si `statutFinal == null && body != null`
        if (statutFinal == null && body != null) {
            // Affecte à `statutFinal` `(String) (body.get("statut") != null ? body.get("statut") : body.get("nouveauSt…`
            statutFinal = (String) (body.get("statut") != null ? body.get("statut") : body.get("nouveauStatut"));
        }
        // Condition : exécute le bloc suivant seulement si `statutFinal == null`
        if (statutFinal == null) {
            // Affecte à `statutFinal` le texte "ANNULE"
            statutFinal = "ANNULE";
        }
        // Déclare la variable `status` (RendezVous.StatutRendezVous) initialisée avec `RendezVous.StatutRendezVous.valueOf(statutFinal)`
        RendezVous.StatutRendezVous status = RendezVous.StatutRendezVous.valueOf(statutFinal);
        // Déclare la variable `rdv` (RendezVous) initialisée avec `rdvService.changerStatut(id, status, raison)`
        RendezVous rdv = rdvService.changerStatut(id, status, raison);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statut du RDV mis à jour : " +…
        return ResponseEntity.ok(ApiResponse.success("Statut du RDV mis à jour : " + status.name(), rdv));
    }

    // Route HTTP DELETE sur le chemin « /{id} »
    @DeleteMapping("/{id}")
    // Méthode `supprimerRendezVous` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : supprime (supprimer rendez vous)
    public ResponseEntity<ApiResponse<Void>> supprimerRendezVous(@PathVariable UUID id) {
        // Appelle la méthode `supprimerRendezVous` sur `rdvService` : rdvService.supprimerRendezVous(id);
        rdvService.supprimerRendezVous(id);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Rendez-vous supprimé avec succ…
        return ResponseEntity.ok(ApiResponse.success("Rendez-vous supprimé avec succès", null));
    }

    /**
     * Endpoint d'autorisation et de connexion à la salle de téléconsultation.
     * Mappe les erreurs métier vers des codes HTTP appropriés (403, 404, 409).
     */
    @PostMapping({"/{id}/teleconsultation/join", "/{id}/teleconsultation/rejoindre"})
    // Méthode `rejoindreTeleconsultation` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de TeleconsultationJoinResponse
    public ResponseEntity<ApiResponse<TeleconsultationJoinResponse>> rejoindreTeleconsultation(
            // Paramètre `id` de type chaîne de caractères — valeur extraite du chemin de l'url
            @PathVariable String id,
            // Paramètre `userId` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String userId,
            // Paramètre `displayName` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String displayName) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `rdvUuid` (identifiant UUID)
            UUID rdvUuid;
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Affecte à `rdvUuid` `UUID.fromString(id)`
                rdvUuid = UUID.fromString(id);
            // Interception de l'exception Exception ex
            } catch (Exception ex) {
                // Affecte à `rdvUuid` `UUID.nameUUIDFromBytes(id.getBytes())`
                rdvUuid = UUID.nameUUIDFromBytes(id.getBytes());
            }

            // Déclare la variable `userUuid` (identifiant UUID) initialisée avec la valeur nulle (absence de valeur)
            UUID userUuid = null;
            // Condition : exécute le bloc suivant seulement si `userId != null && !userId.trim().isEmpty()`
            if (userId != null && !userId.trim().isEmpty()) {
                // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                try {
                    // Affecte à `userUuid` `UUID.fromString(userId)`
                    userUuid = UUID.fromString(userId);
                // Interception de l'exception Exception ex
                } catch (Exception ex) {
                    // Affecte à `userUuid` `UUID.nameUUIDFromBytes(userId.getBytes())`
                    userUuid = UUID.nameUUIDFromBytes(userId.getBytes());
                }
            }

            // Déclare la variable `response` (TeleconsultationJoinResponse) initialisée avec `rdvService.rejoindreTeleconsultation(rdvUuid, userUuid, displayName)`
            TeleconsultationJoinResponse response = rdvService.rejoindreTeleconsultation(rdvUuid, userUuid, displayName);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Autorisation accordée pour la …
            return ResponseEntity.ok(ApiResponse.success("Autorisation accordée pour la téléconsultation", response));
        // Interception de l'exception SecurityException e
        } catch (SecurityException e) {
            // Retourne `ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage…`
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        // Interception de l'exception IllegalArgumentException e
        } catch (IllegalArgumentException e) {
            // Retourne `ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage…`
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        // Interception de l'exception IllegalStateException e
        } catch (IllegalStateException e) {
            // Retourne `ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage(…`
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Appelle la méthode `printStackTrace` sur `e` : e.printStackTrace();
            e.printStackTrace();
            // Retourne `ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(…`
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Erreur serveur lors du démarrage de la téléconsultation : " + e.getMessage()));
        }
    }

    // Route HTTP GET sur le chemin « /admin/all »
    @GetMapping("/admin/all")
    // Méthode `getAllAdminRdv` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de RendezVous ; intention : récupère (get all admin rdv)
    public ResponseEntity<ApiResponse<List<RendezVous>>> getAllAdminRdv() {
        // Déclare la variable `list` (liste de RendezVous) initialisée avec la valeur de l'attribut AllRendezVous de rdvService
        List<RendezVous> list = rdvService.getAllRendezVous();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Tous les rendez-vous", list))
        return ResponseEntity.ok(ApiResponse.success("Tous les rendez-vous", list));
    }

    // Route HTTP GET sur le chemin « /admin/stats »
    @GetMapping("/admin/stats")
    // Méthode `getAdminStats` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : récupère (get admin stats)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        // Déclare la variable `list` (liste de RendezVous) initialisée avec la valeur de l'attribut AllRendezVous de rdvService
        List<RendezVous> list = rdvService.getAllRendezVous();
        // Déclare la variable `total` (entier long) initialisée avec `list.size()`
        long total = list.size();
        // Déclare la variable `confirmes` (entier long) initialisée avec `list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.CONFIRME…`
        long confirmes = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.CONFIRME).count();
        // Déclare la variable `termines` (entier long) initialisée avec `list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.TERMINE)…`
        long termines = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.TERMINE).count();
        // Déclare la variable `demandes` (entier long) initialisée avec `list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.EN_ATTEN…`
        long demandes = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.EN_ATTENTE).count();
        // Déclare la variable `annules` (entier long) initialisée avec `list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.ANNULE).…`
        long annules = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.ANNULE).count();
        // Déclare la variable `teleconsultations` (entier long) initialisée avec `list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultatio…`
        long teleconsultations = list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultation.TELECONSULTATION).count();
        // Déclare la variable `presentiel` (entier long) initialisée avec `list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultatio…`
        long presentiel = list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultation.PRESENTIELLE).count();

        // Déclare la variable `stats` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de java.util.LinkedHashMap<>
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        // Appelle la méthode `put` sur `stats` : stats.put("totalRdv", total);
        stats.put("totalRdv", total);
        // Appelle la méthode `put` sur `stats` : stats.put("confirmes", confirmes);
        stats.put("confirmes", confirmes);
        // Appelle la méthode `put` sur `stats` : stats.put("termines", termines);
        stats.put("termines", termines);
        // Appelle la méthode `put` sur `stats` : stats.put("demandes", demandes);
        stats.put("demandes", demandes);
        // Appelle la méthode `put` sur `stats` : stats.put("annules", annules);
        stats.put("annules", annules);
        // Appelle la méthode `put` sur `stats` : stats.put("teleconsultations", teleconsultations);
        stats.put("teleconsultations", teleconsultations);
        // Appelle la méthode `put` sur `stats` : stats.put("presentiel", presentiel);
        stats.put("presentiel", presentiel);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statistiques rendez-vous", sta…
        return ResponseEntity.ok(ApiResponse.success("Statistiques rendez-vous", stats));
    }
}


