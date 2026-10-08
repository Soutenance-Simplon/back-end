// Déclaration du package Java : `com.diamyaraam.medecin.controller`
package com.diamyaraam.medecin.controller;

// Import de la classe `MedecinService` (paquet com.diamyaraam.medecin.service)
import com.diamyaraam.medecin.service.MedecinService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `MedecinDto` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.MedecinDto;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : ANNUAIRE & CERTIFICATION DES MÉDECINS (MEDECIN CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 ARGUMENTS MAJEURS POUR LA SOUTENANCE :
 * 1. Conformité Ordinale & Déontologie Médicale :
 *    - Intègre le contrôle en temps réel du numéro d'inscription à l'Ordre National des Médecins
 *      du Sénégal (ONDMS). Un compte praticien ne peut exercer la télémédecine sans validation ordinale.
 * 
 * 2. Moteur de Recherche Clinique Multi-Critères :
 *    - Permet aux patients de trouver instantanément des spécialistes proches de chez eux
 *      ou disponibles immédiatement en téléconsultation vidéo.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /medecins »
@RequestMapping("/medecins")
// Déclaration de la classe `MedecinController` (rôle : expose des routes HTTP)
public class MedecinController {

    // Attribut `medecinService` de type MedecinService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final MedecinService medecinService;

    // Constructeur de `MedecinController` — paramètres : `medecinService` (MedecinService) (injection des dépendances par Spring)
    public MedecinController(MedecinService medecinService) {
        // Initialise l'attribut `medecinService` avec la valeur de medecinService
        this.medecinService = medecinService;
    }

    /**
     * ENDPOINT : Vérification ordinale et inscription officielle d'un médecin
     * POST /medecins/verify-onms?userId=...&numeroOrdre=...
     * 
     * @param userId Identifiant du compte utilisateur
     * @param numeroOrdre Numéro de licence officielle délivré par l'ONDMS
     * @return DTO du médecin certifié et mis à jour
     */
    @PostMapping("/verify-onms")
    // Méthode `verifyOnms` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de MedecinDto ; intention : vérifie (verify onms)
    public ResponseEntity<ApiResponse<MedecinDto>> verifyOnms(
            // Paramètre `userId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID userId,
            // Paramètre `numeroOrdre` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String numeroOrdre) {

        // Déclare la variable `dto` (MedecinDto) initialisée avec `medecinService.verifyAndRegisterMedecin(userId, numeroOrdre)`
        MedecinDto dto = medecinService.verifyAndRegisterMedecin(userId, numeroOrdre);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Vérification ONMS réussie", dt…
        return ResponseEntity.ok(ApiResponse.success("Vérification ONMS réussie", dto));
    }

    // Route HTTP GET sur le chemin « /onms/lookup/{numeroOrdre} »
    @GetMapping("/onms/lookup/{numeroOrdre}")
    // Méthode `lookupOnms` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de com.diamyaraam.medecin.entity.OnmsReference
    public ResponseEntity<ApiResponse<com.diamyaraam.medecin.entity.OnmsReference>> lookupOnms(
            // Paramètre `numeroOrdre` de type chaîne de caractères — valeur extraite du chemin de l'url
            @PathVariable String numeroOrdre) {
        // Instruction : com.diamyaraam.medecin.entity.OnmsReference ref = medecinService.lookupOnms(numeroOr…
        com.diamyaraam.medecin.entity.OnmsReference ref = medecinService.lookupOnms(numeroOrdre);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Données ONMS récupérées avec s…
        return ResponseEntity.ok(ApiResponse.success("Données ONMS récupérées avec succès", ref));
    }

    // Route HTTP GET sur le chemin « /specialites »
    @GetMapping("/specialites")
    // Méthode `getSpecialites` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de java.util.Map<String, Object> ; intention : récupère (get specialites)
    public ResponseEntity<ApiResponse<List<java.util.Map<String, Object>>>> getSpecialites() {
        // Déclare la variable `list` (liste de java.util.Map<String, Object>) initialisée avec la valeur de l'attribut Specialites de medecinService
        List<java.util.Map<String, Object>> list = medecinService.getSpecialites();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Spécialités disponibles", list…
        return ResponseEntity.ok(ApiResponse.success("Spécialités disponibles", list));
    }

    // Route HTTP GET sur le chemin « /search »
    @GetMapping("/search")
    // Méthode `search` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de MedecinDto
    public ResponseEntity<ApiResponse<List<MedecinDto>>> search(
            // Paramètre `specialite` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String specialite,
            // Paramètre `region` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String region,
            // Paramètre `search` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String search) {
        // Déclare la variable `list` (liste de MedecinDto) initialisée avec `medecinService.searchMedecins(specialite, region, search)`
        List<MedecinDto> list = medecinService.searchMedecins(specialite, region, search);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Médecins trouvés", list))
        return ResponseEntity.ok(ApiResponse.success("Médecins trouvés", list));
    }

    // Route HTTP GET sur le chemin « /user/{userId} »
    @GetMapping("/user/{userId}")
    // Méthode `getByUserId` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de MedecinDto ; intention : récupère (get by user id)
    public ResponseEntity<ApiResponse<MedecinDto>> getByUserId(@PathVariable UUID userId) {
        // Déclare la variable `dto` (MedecinDto) initialisée avec `medecinService.getMedecinByUserId(userId)`
        MedecinDto dto = medecinService.getMedecinByUserId(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Profil médecin", dto))
        return ResponseEntity.ok(ApiResponse.success("Profil médecin", dto));
    }

    // Route HTTP GET sur le chemin « /admin/all »
    @GetMapping("/admin/all")
    // Méthode `getAllAdminMedecins` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de MedecinDto ; intention : récupère (get all admin medecins)
    public ResponseEntity<ApiResponse<List<MedecinDto>>> getAllAdminMedecins() {
        // Déclare la variable `list` (liste de MedecinDto) initialisée avec la valeur de l'attribut AllPlatformMedecins de medecinService
        List<MedecinDto> list = medecinService.getAllPlatformMedecins();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Tous les médecins de la platef…
        return ResponseEntity.ok(ApiResponse.success("Tous les médecins de la plateforme", list));
    }

    // Route HTTP PUT sur le chemin « /admin/{id}/status »
    @PutMapping("/admin/{id}/status")
    // Méthode `updateMedecinStatus` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de MedecinDto ; intention : met à jour (update medecin status)
    public ResponseEntity<ApiResponse<MedecinDto>> updateMedecinStatus(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `statut` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String statut,
            // Paramètre `isVerified` de type booléen — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) Boolean isVerified) {
        // Déclare la variable `updated` (MedecinDto) initialisée avec `medecinService.updateMedecinStatut(id, statut, isVerified)`
        MedecinDto updated = medecinService.updateMedecinStatut(id, statut, isVerified);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statut médecin mis à jour avec…
        return ResponseEntity.ok(ApiResponse.success("Statut médecin mis à jour avec succès", updated));
    }
}
