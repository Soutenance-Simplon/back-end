package com.diamyaraam.dossier.controller;

import com.diamyaraam.dossier.entity.CartePhysique;
import com.diamyaraam.dossier.service.CartePhysiqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cartes")
public class CartePhysiqueController {

    private final CartePhysiqueService cartePhysiqueService;

    public CartePhysiqueController(CartePhysiqueService cartePhysiqueService) {
        this.cartePhysiqueService = cartePhysiqueService;
    }

    /**
     * Endpoint appelé par l'application Flutter quand on scanne une NOUVELLE carte pour l'associer au patient.
     */
    @PostMapping("/lier")
    public ResponseEntity<?> lierCarte(@RequestBody Map<String, String> request) {
        try {
            String qrTokenSecurise = request.get("qrToken");
            UUID patientId = UUID.fromString(request.get("patientId"));

            CartePhysique carteLiee = cartePhysiqueService.lierCarteAuPatient(qrTokenSecurise, patientId);
            return ResponseEntity.ok(carteLiee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint appelé par les secouristes/médecins quand ils scannent la carte EN CAS D'URGENCE.
     * Retourne l'ID du patient pour qu'ils puissent récupérer son dossier médical.
     */
    @GetMapping("/scan/{qrToken}")
    public ResponseEntity<?> scannerCarteUrgence(@PathVariable String qrToken) {
        try {
            UUID patientId = cartePhysiqueService.getPatientIdByScannerCarte(qrToken);
            // Ici, vous pourriez faire appel au DossierMedicalService pour renvoyer directement les infos d'urgence (Groupe Sanguin, etc.)
            return ResponseEntity.ok(Map.of("patientId", patientId.toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour déclarer une carte perdue depuis l'application.
     */
    @PostMapping("/perdue/{patientId}")
    public ResponseEntity<?> declarerPerdue(@PathVariable UUID patientId) {
        try {
            CartePhysique cartePerdue = cartePhysiqueService.declarerCartePerdue(patientId);
            return ResponseEntity.ok(cartePerdue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint appelé par l'application Flutter toutes les 10 minutes pour obtenir un QR dynamique
     * pour l'affichage de la carte virtuelle.
     */
    @GetMapping("/generer-jeton/{patientId}")
    public ResponseEntity<?> genererJetonDynamique(@PathVariable UUID patientId) {
        try {
            String jeton = cartePhysiqueService.genererJetonDynamique(patientId);
            return ResponseEntity.ok(Map.of("jetonDynamique", jeton));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
