package com.diamyaraam.medecin.controller;

import com.diamyaraam.medecin.service.MedecinService;
import com.diamyaraam.shared.dto.ApiResponse;
import com.diamyaraam.shared.dto.MedecinDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/medecins")
public class MedecinController {

    private final MedecinService medecinService;

    public MedecinController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    @PostMapping("/verify-onms")
    public ResponseEntity<ApiResponse<MedecinDto>> verifyOnms(
            @RequestParam UUID userId,
            @RequestParam String numeroOrdre) {
        MedecinDto dto = medecinService.verifyAndRegisterMedecin(userId, numeroOrdre);
        return ResponseEntity.ok(ApiResponse.success("Vérification ONMS réussie", dto));
    }

    @GetMapping("/onms/lookup/{numeroOrdre}")
    public ResponseEntity<ApiResponse<com.diamyaraam.medecin.entity.OnmsReference>> lookupOnms(
            @PathVariable String numeroOrdre) {
        com.diamyaraam.medecin.entity.OnmsReference ref = medecinService.lookupOnms(numeroOrdre);
        return ResponseEntity.ok(ApiResponse.success("Données ONMS récupérées avec succès", ref));
    }

    @GetMapping("/specialites")
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getSpecialites() {
        List<java.util.Map<String, Object>> list = medecinService.getSpecialites();
        return ResponseEntity.ok(ApiResponse.success("Spécialités disponibles", list));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MedecinDto>>> search(
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search) {
        List<MedecinDto> list = medecinService.searchMedecins(specialite, region, search);
        return ResponseEntity.ok(ApiResponse.success("Médecins trouvés", list));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<MedecinDto>> getByUserId(@PathVariable UUID userId) {
        MedecinDto dto = medecinService.getMedecinByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Profil médecin", dto));
    }
}
