// Déclaration du package Java : `com.diamyaraam.medecin.controller`
package com.diamyaraam.medecin.controller;

// Import de la classe `OnmsReference` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.OnmsReference;
// Import de la classe `MedecinService` (paquet com.diamyaraam.medecin.service)
import com.diamyaraam.medecin.service.MedecinService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `List` (paquet java.util)
import java.util.List;

// Contrôleur REST : les valeurs retournées sont sérialisées en JSON
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /admin/onms »
@RequestMapping("/admin/onms")
// Déclaration de la classe `AdminController` (rôle : expose des routes HTTP)
public class AdminController {

    // Attribut `medecinService` de type MedecinService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final MedecinService medecinService;

    // Constructeur de `AdminController` — paramètres : `medecinService` (MedecinService) (injection des dépendances par Spring)
    public AdminController(MedecinService medecinService) {
        // Initialise l'attribut `medecinService` avec la valeur de medecinService
        this.medecinService = medecinService;
    }

    // Route HTTP GET
    @GetMapping
    // Méthode `getAllOnms` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de OnmsReference ; intention : récupère (get all onms)
    public ResponseEntity<ApiResponse<List<OnmsReference>>> getAllOnms() {
        // Déclare la variable `list` (liste de OnmsReference) initialisée avec la valeur de l'attribut AllOnmsReferences de medecinService
        List<OnmsReference> list = medecinService.getAllOnmsReferences();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des médecins ONMS", list…
        return ResponseEntity.ok(ApiResponse.success("Liste des médecins ONMS", list));
    }

    // Route HTTP POST
    @PostMapping
    // Méthode `addOnms` (publique) — paramètres : `reference` (OnmsReference) ; retourne : réponse HTTP contenant enveloppe ApiResponse de OnmsReference ; intention : ajoute (add onms)
    public ResponseEntity<ApiResponse<OnmsReference>> addOnms(@RequestBody OnmsReference reference) {
        // Déclare la variable `saved` (OnmsReference) initialisée avec `medecinService.addOnmsReference(reference)`
        OnmsReference saved = medecinService.addOnmsReference(reference);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Médecin ONMS ajouté avec succè…
        return ResponseEntity.ok(ApiResponse.success("Médecin ONMS ajouté avec succès", saved));
    }

    // Route HTTP PUT sur le chemin « /{numeroOrdre} »
    @PutMapping("/{numeroOrdre}")
    // Méthode `updateOnms` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de OnmsReference ; intention : met à jour (update onms)
    public ResponseEntity<ApiResponse<OnmsReference>> updateOnms(
            // Paramètre `numeroOrdre` de type chaîne de caractères — valeur extraite du chemin de l'url
            @PathVariable String numeroOrdre,
            // Paramètre `reference` de type OnmsReference — corps json de la requête désérialisé en objet java
            @RequestBody OnmsReference reference) {
        // Déclare la variable `updated` (OnmsReference) initialisée avec `medecinService.updateOnmsReference(numeroOrdre, reference)`
        OnmsReference updated = medecinService.updateOnmsReference(numeroOrdre, reference);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Médecin ONMS mis à jour", upda…
        return ResponseEntity.ok(ApiResponse.success("Médecin ONMS mis à jour", updated));
    }

    // Route HTTP DELETE sur le chemin « /{numeroOrdre} »
    @DeleteMapping("/{numeroOrdre}")
    // Méthode `deleteOnms` (publique) — paramètres : `numeroOrdre` (chaîne de caractères) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : supprime (delete onms)
    public ResponseEntity<ApiResponse<Void>> deleteOnms(@PathVariable String numeroOrdre) {
        // Supprime des données en base via medecinService : medecinService.deleteOnmsReference(numeroOrdre);
        medecinService.deleteOnmsReference(numeroOrdre);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Médecin radié de l'ONMS", null…
        return ResponseEntity.ok(ApiResponse.success("Médecin radié de l'ONMS", null));
    }
}
