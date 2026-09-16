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
            @RequestParam String nouveauStatut,
            @RequestParam(required = false) String raison) {
        RendezVous.StatutRendezVous status = RendezVous.StatutRendezVous.valueOf(nouveauStatut);
        RendezVous rdv = rdvService.changerStatut(id, status, raison);
        return ResponseEntity.ok(ApiResponse.success("Statut du RDV mis à jour : " + status.name(), rdv));
    }

    /**
     * Endpoint d'autorisation et de connexion à la salle de téléconsultation.
     * Mappe les erreurs métier vers des codes HTTP appropriés (403, 404, 409).
     */
    @PostMapping({"/{id}/teleconsultation/join", "/{id}/teleconsultation/rejoindre"})
    public ResponseEntity<ApiResponse<TeleconsultationJoinResponse>> rejoindreTeleconsultation(
            @PathVariable UUID id,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String displayName) {
        try {
            TeleconsultationJoinResponse response = rdvService.rejoindreTeleconsultation(id, userId, displayName);
            return ResponseEntity.ok(ApiResponse.success("Autorisation accordée pour la téléconsultation", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Erreur serveur lors du démarrage de la téléconsultation."));
        }
    }
}

