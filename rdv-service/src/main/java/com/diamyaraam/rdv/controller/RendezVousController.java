package com.diamyaraam.rdv.controller;

import com.diamyaraam.rdv.dto.TeleconsultationJoinResponse;
import com.diamyaraam.rdv.entity.RendezVous;
import com.diamyaraam.rdv.service.RendezVousService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rdv")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RendezVousController {

    private final RendezVousService rdvService;

    public RendezVousController(RendezVousService rdvService) {
        this.rdvService = rdvService;
    }

    @PostMapping({"", "/creer"})
    public ResponseEntity<ApiResponse<RendezVous>> creerRendezVous(@RequestBody Map<String, Object> body) {
        try {
            String patientIdStr = (String) body.get("patient_id");
            String medecinIdStr = (String) body.get("medecin_id");
            UUID patientId = (patientIdStr != null && patientIdStr.length() == 36) ? UUID.fromString(patientIdStr) : UUID.randomUUID();
            UUID medecinId = (medecinIdStr != null && medecinIdStr.length() == 36) ? UUID.fromString(medecinIdStr) : UUID.randomUUID();
            String motif = body.get("motif") != null ? body.get("motif").toString() : "Consultation médicale";
            String dateStr = (String) body.get("date_heure");
            LocalDateTime dateHeure = dateStr != null ? LocalDateTime.parse(dateStr.split("\\.")[0]) : LocalDateTime.now();
            String typeStr = body.get("type_consultation") != null ? body.get("type_consultation").toString() : "TELECONSULTATION";

            RendezVous rdv = rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure);
            try {
                rdv.setTypeConsultation(RendezVous.TypeConsultation.valueOf(typeStr));
            } catch (Exception ignored) {}
            rdv = rdvService.confirmerPaiementEtRdv(rdv.getId(), "PAY-" + System.currentTimeMillis());
            return ResponseEntity.ok(ApiResponse.success("Rendez-vous créé et confirmé", rdv));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur création RDV: " + e.getMessage()));
        }
    }

    @PostMapping("/demander")
    public ResponseEntity<ApiResponse<RendezVous>> demander(
            @RequestParam UUID patientId,
            @RequestParam UUID medecinId,
            @RequestParam String motif,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure) {
        RendezVous rdv = rdvService.demanderRendezVous(patientId, medecinId, motif, dateHeure);
        return ResponseEntity.ok(ApiResponse.success("Demande de RDV créée", rdv));
    }

    @PutMapping("/{id}/accepter")
    public ResponseEntity<ApiResponse<RendezVous>> accepter(@PathVariable UUID id) {
        RendezVous rdv = rdvService.accepterRendezVous(id);
        return ResponseEntity.ok(ApiResponse.success("RDV accepté par le médecin", rdv));
    }

    @PutMapping("/{id}/confirmer-paiement")
    public ResponseEntity<ApiResponse<RendezVous>> confirmerPaiement(
            @PathVariable UUID id,
            @RequestParam String refPaiement) {
        RendezVous rdv = rdvService.confirmerPaiementEtRdv(id, refPaiement);
        return ResponseEntity.ok(ApiResponse.success("Paiement validé et RDV confirmé", rdv));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<RendezVous>>> getByPatient(@PathVariable UUID patientId) {
        List<RendezVous> list = rdvService.getByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("RDV patient", list));
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<ApiResponse<List<RendezVous>>> getByMedecin(@PathVariable UUID medecinId) {
        List<RendezVous> list = rdvService.getByMedecin(medecinId);
        return ResponseEntity.ok(ApiResponse.success("RDV médecin", list));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<ApiResponse<RendezVous>> changerStatut(
            @PathVariable UUID id,
            @RequestParam(required = false) String nouveauStatut,
            @RequestParam(required = false) String raison,
            @RequestBody(required = false) Map<String, Object> body) {
        String statutFinal = nouveauStatut;
        if (statutFinal == null && body != null) {
            statutFinal = (String) (body.get("statut") != null ? body.get("statut") : body.get("nouveauStatut"));
        }
        if (statutFinal == null) {
            statutFinal = "ANNULE";
        }
        RendezVous.StatutRendezVous status = RendezVous.StatutRendezVous.valueOf(statutFinal);
        RendezVous rdv = rdvService.changerStatut(id, status, raison);
        return ResponseEntity.ok(ApiResponse.success("Statut du RDV mis à jour : " + status.name(), rdv));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> supprimerRendezVous(@PathVariable UUID id) {
        rdvService.supprimerRendezVous(id);
        return ResponseEntity.ok(ApiResponse.success("Rendez-vous supprimé avec succès", null));
    }

    /**
     * Endpoint d'autorisation et de connexion à la salle de téléconsultation.
     * Mappe les erreurs métier vers des codes HTTP appropriés (403, 404, 409).
     */
    @PostMapping({"/{id}/teleconsultation/join", "/{id}/teleconsultation/rejoindre"})
    public ResponseEntity<ApiResponse<TeleconsultationJoinResponse>> rejoindreTeleconsultation(
            @PathVariable String id,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String displayName) {
        try {
            UUID rdvUuid;
            try {
                rdvUuid = UUID.fromString(id);
            } catch (Exception ex) {
                rdvUuid = UUID.nameUUIDFromBytes(id.getBytes());
            }

            UUID userUuid = null;
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    userUuid = UUID.fromString(userId);
                } catch (Exception ex) {
                    userUuid = UUID.nameUUIDFromBytes(userId.getBytes());
                }
            }

            TeleconsultationJoinResponse response = rdvService.rejoindreTeleconsultation(rdvUuid, userUuid, displayName);
            return ResponseEntity.ok(ApiResponse.success("Autorisation accordée pour la téléconsultation", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Erreur serveur lors du démarrage de la téléconsultation : " + e.getMessage()));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<RendezVous>>> getAllAdminRdv() {
        List<RendezVous> list = rdvService.getAllRendezVous();
        return ResponseEntity.ok(ApiResponse.success("Tous les rendez-vous", list));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        List<RendezVous> list = rdvService.getAllRendezVous();
        long total = list.size();
        long confirmes = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.CONFIRME).count();
        long termines = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.TERMINE).count();
        long demandes = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.EN_ATTENTE).count();
        long annules = list.stream().filter(r -> r.getStatut() == RendezVous.StatutRendezVous.ANNULE).count();
        long teleconsultations = list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultation.TELECONSULTATION).count();
        long presentiel = list.stream().filter(r -> r.getTypeConsultation() == RendezVous.TypeConsultation.PRESENTIELLE).count();

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalRdv", total);
        stats.put("confirmes", confirmes);
        stats.put("termines", termines);
        stats.put("demandes", demandes);
        stats.put("annules", annules);
        stats.put("teleconsultations", teleconsultations);
        stats.put("presentiel", presentiel);

        return ResponseEntity.ok(ApiResponse.success("Statistiques rendez-vous", stats));
    }
}


