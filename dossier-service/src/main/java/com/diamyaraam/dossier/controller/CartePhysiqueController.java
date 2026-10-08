// Déclaration du package Java : `com.diamyaraam.dossier.controller`
package com.diamyaraam.dossier.controller;

// Import de la classe `CartePhysique` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.CartePhysique;
// Import de la classe `CartePhysiqueService` (paquet com.diamyaraam.dossier.service)
import com.diamyaraam.dossier.service.CartePhysiqueService;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : CARTES MÉDICALES PHYSIQUES & BADGES QR (CARTE PHYSIQUE CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 INNOVATION PHYGITALE (PHYSIQUE + NUMÉRIQUE) POUR LA SOUTENANCE :
 * Pourquoi intégrer des cartes physiques dans une plateforme mobile de télémédecine ?
 * 
 * 💳 ENJEUX D'ACCESSIBILITÉ & SANTÉ PUBLIQUE AU SÉNÉGAL :
 * 1. Rupture Numérique & Téléphones Déchargés :
 *    En cas de crise ou d'accident de la circulation, la victime peut avoir un téléphone éteint,
 *    cassé ou inaccessible. La carte physique plastique portée dans le portefeuille garantit l'accès aux soins.
 * 
 * 2. Liaison Sécurisée (Appairage NFC/QR) :
 *    Un patient achète ou reçoit une carte vierge et l'associe à son dossier via un scan initial (`/lier`).
 * 
 * 3. Révocation Immédiate en Cas de Perte ou Vol (`/perdue`) :
 *    Si la carte est égarée, le patient la désactive en un clic depuis l'application mobile,
 *    invalidant instantanément le jeton QR pour préserver son secret médical.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /api/cartes »
@RequestMapping("/api/cartes")
// Déclaration de la classe `CartePhysiqueController` (rôle : expose des routes HTTP)
public class CartePhysiqueController {

    // Attribut `cartePhysiqueService` de type CartePhysiqueService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final CartePhysiqueService cartePhysiqueService;

    // Constructeur de `CartePhysiqueController` — paramètres : `cartePhysiqueService` (CartePhysiqueService) (injection des dépendances par Spring)
    public CartePhysiqueController(CartePhysiqueService cartePhysiqueService) {
        // Initialise l'attribut `cartePhysiqueService` avec la valeur de cartePhysiqueService
        this.cartePhysiqueService = cartePhysiqueService;
    }

    /**
     * ENDPOINT : Appairage et association d'une nouvelle carte physique au profil du patient
     * POST /api/cartes/lier
     * 
     * @param request JSON contenant 'qrToken' et 'patientId'
     * @return L'entité CartePhysique activée et liée
     */
    @PostMapping("/lier")
    // Méthode `lierCarte` (publique) — paramètres : `request` (dictionnaire clé/valeur) ; retourne : réponse HTTP contenant ? ; intention : lie (lier carte)
    public ResponseEntity<?> lierCarte(@RequestBody Map<String, String> request) {

        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `qrTokenSecurise` (chaîne de caractères) initialisée avec `request.get("qrToken")`
            String qrTokenSecurise = request.get("qrToken");
            // Déclare la variable `patientId` (identifiant UUID) initialisée avec `UUID.fromString(request.get("patientId"))`
            UUID patientId = UUID.fromString(request.get("patientId"));

            // Déclare la variable `carteLiee` (CartePhysique) initialisée avec `cartePhysiqueService.lierCarteAuPatient(qrTokenSecurise, patientId)`
            CartePhysique carteLiee = cartePhysiqueService.lierCarteAuPatient(qrTokenSecurise, patientId);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(carteLiee)
            return ResponseEntity.ok(carteLiee);
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))`
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint appelé par les secouristes/médecins quand ils scannent la carte EN CAS D'URGENCE.
     * Retourne l'ID du patient pour qu'ils puissent récupérer son dossier médical.
     */
    @GetMapping("/scan/{qrToken}")
    // Méthode `scannerCarteUrgence` (publique) — paramètres : `qrToken` (chaîne de caractères) ; retourne : réponse HTTP contenant ?
    public ResponseEntity<?> scannerCarteUrgence(@PathVariable String qrToken) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `patientId` (identifiant UUID) initialisée avec `cartePhysiqueService.getPatientIdByScannerCarte(qrToken)`
            UUID patientId = cartePhysiqueService.getPatientIdByScannerCarte(qrToken);
            // Ici, vous pourriez faire appel au DossierMedicalService pour renvoyer directement les infos d'urgence (Groupe Sanguin, etc.)
            return ResponseEntity.ok(Map.of("patientId", patientId.toString()));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))`
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint pour déclarer une carte perdue depuis l'application.
     */
    @PostMapping("/perdue/{patientId}")
    // Méthode `declarerPerdue` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant ? ; intention : déclare (declarer perdue)
    public ResponseEntity<?> declarerPerdue(@PathVariable UUID patientId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `cartePerdue` (CartePhysique) initialisée avec `cartePhysiqueService.declarerCartePerdue(patientId)`
            CartePhysique cartePerdue = cartePhysiqueService.declarerCartePerdue(patientId);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(cartePerdue)
            return ResponseEntity.ok(cartePerdue);
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))`
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint appelé par l'application Flutter toutes les 10 minutes pour obtenir un QR dynamique
     * pour l'affichage de la carte virtuelle.
     */
    @GetMapping("/generer-jeton/{patientId}")
    // Méthode `genererJetonDynamique` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant ? ; intention : génère (generer jeton dynamique)
    public ResponseEntity<?> genererJetonDynamique(@PathVariable UUID patientId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `jeton` (chaîne de caractères) initialisée avec `cartePhysiqueService.genererJetonDynamique(patientId)`
            String jeton = cartePhysiqueService.genererJetonDynamique(patientId);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(Map.of("jetonDynamique", jeton))
            return ResponseEntity.ok(Map.of("jetonDynamique", jeton));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))`
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
