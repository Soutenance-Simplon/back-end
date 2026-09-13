package com.diamyaraam.patient.controller;

import com.diamyaraam.patient.entity.Patient;
import com.diamyaraam.patient.service.PatientService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.diamyaraam.patient.entity.MembreFamille;
import com.diamyaraam.patient.dto.MembreFamilleDto;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Patient>> getByUserId(@PathVariable UUID userId) {
        Patient p = patientService.getOrCreateProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profil patient", p));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<Patient>> getByIdOrUserId(@PathVariable String patientId) {
        try {
            UUID id = UUID.fromString(patientId);
            Patient p = patientService.getPatientByIdOrUserId(id);
            return ResponseEntity.ok(ApiResponse.success("Profil patient", p));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Identifiant invalide : " + patientId));
        }
    }

    @PutMapping(value = {"/user/{userId}/emergency-contact", "/{userId}/emergency-contact"})
    public ResponseEntity<ApiResponse<Patient>> updateEmergencyContact(
            @PathVariable UUID userId,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String lien,
            @RequestBody(required = false) Map<String, Object> body) {

        String effectiveNom = nom;
        String effectiveTelephone = telephone;
        String effectiveLien = lien;

        if (body != null) {
            if (effectiveNom == null && body.containsKey("nom")) {
                effectiveNom = String.valueOf(body.get("nom"));
            }
            if (effectiveNom == null && body.containsKey("contactUrgenceNom")) {
                effectiveNom = String.valueOf(body.get("contactUrgenceNom"));
            }
            if (effectiveNom == null && body.containsKey("personne_contact")) {
                effectiveNom = String.valueOf(body.get("personne_contact"));
            }

            if (effectiveTelephone == null && body.containsKey("telephone")) {
                effectiveTelephone = String.valueOf(body.get("telephone"));
            }
            if (effectiveTelephone == null && body.containsKey("contactUrgenceTelephone")) {
                effectiveTelephone = String.valueOf(body.get("contactUrgenceTelephone"));
            }
            if (effectiveTelephone == null && body.containsKey("telephone_contact")) {
                effectiveTelephone = String.valueOf(body.get("telephone_contact"));
            }

            if (effectiveLien == null && body.containsKey("lien")) {
                effectiveLien = String.valueOf(body.get("lien"));
            }
            if (effectiveLien == null && body.containsKey("contactUrgenceLien")) {
                effectiveLien = String.valueOf(body.get("contactUrgenceLien"));
            }
            if (effectiveLien == null && body.containsKey("lien_parente_contact")) {
                effectiveLien = String.valueOf(body.get("lien_parente_contact"));
            }
        }

        Patient p = patientService.updateEmergencyContact(userId, effectiveNom, effectiveTelephone, effectiveLien);
        return ResponseEntity.ok(ApiResponse.success("Contact d'urgence mis à jour avec succès", p));
    }

    @GetMapping("/user/{userId}/famille")
    public ResponseEntity<ApiResponse<List<MembreFamille>>> getMembresFamille(@PathVariable UUID userId) {
        List<MembreFamille> liste = patientService.getMembresFamille(userId);
        return ResponseEntity.ok(ApiResponse.success("Membres de la famille récupérés", liste));
    }

    @PostMapping("/user/{userId}/famille")
    public ResponseEntity<ApiResponse<MembreFamille>> ajouterMembreFamille(
            @PathVariable UUID userId,
            @RequestBody MembreFamilleDto dto) {
        MembreFamille mf = patientService.ajouterMembreFamille(userId, dto);
        return ResponseEntity.ok(ApiResponse.success("Membre de la famille ajouté", mf));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<Patient>>> getAllAdminPatients() {
        List<Patient> list = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success("Tous les patients", list));
    }
}
