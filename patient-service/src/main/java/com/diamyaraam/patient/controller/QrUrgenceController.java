package com.diamyaraam.patient.controller;

import com.diamyaraam.patient.dto.QrEmergencyResponseDto;
import com.diamyaraam.patient.entity.Patient;
import com.diamyaraam.patient.service.PatientService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/patients/qr-urgence")
public class QrUrgenceController {

    private final PatientService patientService;

    public QrUrgenceController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * Scan du QR Code d'urgence (RM081-RM085)
     * Le QR Code ne contient qu'un identifiant anonymisé (ex: qrToken = "QR-8A3B9F2C").
     *
     * Si isMedecin = false (Citoyen) -> uniquement nom, photo, contacts d'urgence.
     * Si isMedecin = true (Médecin authentifié) -> groupe sanguin, allergies, antécédents, traitements.
     */
    @GetMapping("/scan/{qrToken}")
    public ResponseEntity<ApiResponse<QrEmergencyResponseDto>> scanQrCode(
            @PathVariable String qrToken,
            @RequestParam(defaultValue = "false") boolean isMedecin) {

        Patient patient = patientService.getByQrToken(qrToken);

        QrEmergencyResponseDto dto = new QrEmergencyResponseDto();
        dto.setContactUrgenceNom(patient.getContactUrgenceNom());
        dto.setContactUrgenceTelephone(patient.getContactUrgenceTelephone());
        dto.setContactUrgenceLien(patient.getContactUrgenceLien());
        dto.setAdresse(patient.getAdresse());
        dto.setVille(patient.getVille());

        if (isMedecin) {
            dto.setViewMode("MEDECIN_AUTHENTIFIE");
            dto.setNom("Patient");
            dto.setPrenom(qrToken);
            dto.setGroupeSanguin(null);
            dto.setAllergies(Collections.emptyList());
            dto.setAntecedents(Collections.emptyList());
            dto.setMaladiesChroniques(Collections.emptyList());
            dto.setTraitementsEnCours(Collections.emptyList());
        } else {
            dto.setViewMode("CITOYEN_PUBLIC");
            dto.setNom("");
            dto.setPrenom("Patient");
            dto.setGroupeSanguin(null);
            dto.setAllergies(Collections.emptyList());
            dto.setAntecedents(Collections.emptyList());
            dto.setMaladiesChroniques(Collections.emptyList());
            dto.setTraitementsEnCours(Collections.emptyList());
        }

        return ResponseEntity.ok(
            ApiResponse.success("Fiche médicale d'urgence résolue avec succès (" + dto.getViewMode() + ")", dto)
        );
    }
}
