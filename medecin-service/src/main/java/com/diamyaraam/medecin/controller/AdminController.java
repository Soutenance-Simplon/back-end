package com.diamyaraam.medecin.controller;

import com.diamyaraam.medecin.entity.OnmsReference;
import com.diamyaraam.medecin.service.MedecinService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/onms")
public class AdminController {

    private final MedecinService medecinService;

    public AdminController(MedecinService medecinService) {
        this.medecinService = medecinService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OnmsReference>>> getAllOnms() {
        List<OnmsReference> list = medecinService.getAllOnmsReferences();
        return ResponseEntity.ok(ApiResponse.success("Liste des médecins ONMS", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OnmsReference>> addOnms(@RequestBody OnmsReference reference) {
        OnmsReference saved = medecinService.addOnmsReference(reference);
        return ResponseEntity.ok(ApiResponse.success("Médecin ONMS ajouté avec succès", saved));
    }

    @PutMapping("/{numeroOrdre}")
    public ResponseEntity<ApiResponse<OnmsReference>> updateOnms(
            @PathVariable String numeroOrdre,
            @RequestBody OnmsReference reference) {
        OnmsReference updated = medecinService.updateOnmsReference(numeroOrdre, reference);
        return ResponseEntity.ok(ApiResponse.success("Médecin ONMS mis à jour", updated));
    }

    @DeleteMapping("/{numeroOrdre}")
    public ResponseEntity<ApiResponse<Void>> deleteOnms(@PathVariable String numeroOrdre) {
        medecinService.deleteOnmsReference(numeroOrdre);
        return ResponseEntity.ok(ApiResponse.success("Médecin radié de l'ONMS", null));
    }
}
