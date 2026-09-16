package com.diamyaraam.patient.controller;

import com.diamyaraam.patient.entity.Patient;
import com.diamyaraam.patient.service.PatientService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @PutMapping("/user/{userId}/emergency-contact")
    public ResponseEntity<ApiResponse<Patient>> updateEmergencyContact(
            @PathVariable UUID userId,
            @RequestParam String nom,
            @RequestParam String telephone,
            @RequestParam String lien) {
        Patient p = patientService.updateEmergencyContact(userId, nom, telephone, lien);
        return ResponseEntity.ok(ApiResponse.success("Contact d'urgence mis à jour", p));
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
}
