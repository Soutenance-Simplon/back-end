package com.diamyaraam.medecin.controller;

import com.diamyaraam.medecin.entity.CreneauDisponible;
import com.diamyaraam.medecin.entity.DisponibiliteMedecin;
import com.diamyaraam.medecin.service.PlanningService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medecins")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    private UUID parseUuidSafe(String idStr) {
        if (idStr == null || idStr.isBlank() || idStr.equals("med-1") || idStr.equals("null")) return null;
        try {
            return UUID.fromString(idStr);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.now();
        try {
            String clean = dateStr;
            if (clean.contains("Z")) clean = clean.replace("Z", "");
            if (clean.contains("+")) clean = clean.substring(0, clean.indexOf("+"));
            if (clean.length() > 19) clean = clean.substring(0, 19);
            return LocalDateTime.parse(clean);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    @PostMapping("/{medecinId}/disponibilites")
    public ResponseEntity<ApiResponse<DisponibiliteMedecin>> ajouterDisponibilite(
            @PathVariable String medecinId,
            @RequestBody DisponibiliteMedecin disponibilite) {
        UUID uid = parseUuidSafe(medecinId);
        DisponibiliteMedecin created = planningService.ajouterDisponibilite(uid, disponibilite);
        return ResponseEntity.ok(ApiResponse.success("Disponibilité ajoutée", created));
    }

    @GetMapping("/{medecinId}/disponibilites")
    public ResponseEntity<ApiResponse<List<DisponibiliteMedecin>>> getDisponibilites(@PathVariable String medecinId) {
        UUID uid = parseUuidSafe(medecinId);
        List<DisponibiliteMedecin> list = planningService.getDisponibilites(uid);
        return ResponseEntity.ok(ApiResponse.success("Liste des disponibilités", list));
    }

    @PostMapping("/{medecinId}/creneaux")
    public ResponseEntity<ApiResponse<CreneauDisponible>> creerCreneau(
            @PathVariable String medecinId,
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String type) {
        UUID uid = parseUuidSafe(medecinId);
        LocalDateTime startTime = parseDateSafe(start);
        LocalDateTime endTime = parseDateSafe(end);
        DisponibiliteMedecin.TypeConsultation t = DisponibiliteMedecin.TypeConsultation.TELECONSULTATION;
        if (type != null) {
            if (type.toUpperCase().contains("CABINET") || type.toUpperCase().contains("PRESENTIELLE")) {
                t = DisponibiliteMedecin.TypeConsultation.PRESENTIELLE;
            }
        }
        CreneauDisponible created = planningService.creerCreneau(uid, startTime, endTime, t);
        return ResponseEntity.ok(ApiResponse.success("Créneau créé", created));
    }

    @GetMapping("/{medecinId}/creneaux")
    public ResponseEntity<ApiResponse<List<CreneauDisponible>>> getCreneaux(
            @PathVariable String medecinId,
            @RequestParam(required = false) String statut) {
        UUID uid = parseUuidSafe(medecinId);
        List<CreneauDisponible> list;
        if ("DISPONIBLE".equalsIgnoreCase(statut)) {
            list = planningService.getCreneauxDisponibles(uid);
        } else {
            list = planningService.getTousLesCreneaux(uid);
        }
        return ResponseEntity.ok(ApiResponse.success("Liste des créneaux", list));
    }

    @PutMapping("/{medecinId}/creneaux/{creneauId}/statut")
    public ResponseEntity<ApiResponse<CreneauDisponible>> changerStatutCreneau(
            @PathVariable String medecinId,
            @PathVariable UUID creneauId,
            @RequestParam String statut) {
        CreneauDisponible.StatutCreneau st = CreneauDisponible.StatutCreneau.valueOf(statut.toUpperCase());
        CreneauDisponible updated = planningService.changerStatutCreneau(creneauId, st);
        return ResponseEntity.ok(ApiResponse.success("Statut du créneau mis à jour", updated));
    }

    @DeleteMapping("/{medecinId}/creneaux/{creneauId}")
    public ResponseEntity<ApiResponse<Void>> supprimerCreneau(
            @PathVariable String medecinId,
            @PathVariable UUID creneauId) {
        planningService.supprimerCreneau(creneauId);
        return ResponseEntity.ok(ApiResponse.success("Créneau supprimé avec succès", null));
    }
}
