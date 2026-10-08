// Déclaration du package Java : `com.diamyaraam.patient.controller`
package com.diamyaraam.patient.controller;

// Import de la classe `Patient` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.Patient;
// Import de la classe `PatientService` (paquet com.diamyaraam.patient.service)
import com.diamyaraam.patient.service.PatientService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Import de la classe `MembreFamille` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.MembreFamille;
// Import de la classe `MembreFamilleDto` (paquet com.diamyaraam.patient.dto)
import com.diamyaraam.patient.dto.MembreFamilleDto;

// Contrôleur REST : les valeurs retournées sont sérialisées en JSON
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /patients »
@RequestMapping("/patients")
// Déclaration de la classe `PatientController` (rôle : expose des routes HTTP)
public class PatientController {

    // Attribut `patientService` de type PatientService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final PatientService patientService;

    // Constructeur de `PatientController` — paramètres : `patientService` (PatientService) (injection des dépendances par Spring)
    public PatientController(PatientService patientService) {
        // Initialise l'attribut `patientService` avec la valeur de patientService
        this.patientService = patientService;
    }

    // Route HTTP GET sur le chemin « /user/{userId} »
    @GetMapping("/user/{userId}")
    // Méthode `getByUserId` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Patient ; intention : récupère (get by user id)
    public ResponseEntity<ApiResponse<Patient>> getByUserId(@PathVariable UUID userId) {
        // Déclare la variable `p` (Patient) initialisée avec `patientService.getOrCreateProfile(userId)`
        Patient p = patientService.getOrCreateProfile(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Profil patient", p))
        return ResponseEntity.ok(ApiResponse.success("Profil patient", p));
    }

    // Route HTTP GET sur le chemin « /{patientId} »
    @GetMapping("/{patientId}")
    // Méthode `getByIdOrUserId` (publique) — paramètres : `patientId` (chaîne de caractères) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Patient ; intention : récupère (get by id or user id)
    public ResponseEntity<ApiResponse<Patient>> getByIdOrUserId(@PathVariable String patientId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `id` (identifiant UUID) initialisée avec `UUID.fromString(patientId)`
            UUID id = UUID.fromString(patientId);
            // Déclare la variable `p` (Patient) initialisée avec `patientService.getPatientByIdOrUserId(id)`
            Patient p = patientService.getPatientByIdOrUserId(id);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Profil patient", p))
            return ResponseEntity.ok(ApiResponse.success("Profil patient", p));
        // Interception de l'exception IllegalArgumentException e
        } catch (IllegalArgumentException e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Identifiant invalide : " + …`
            return ResponseEntity.badRequest().body(ApiResponse.error("Identifiant invalide : " + patientId));
        }
    }

    // Route HTTP PUT sur le chemin « /user/{userId}/emergency-contact »
    @PutMapping(value = {"/user/{userId}/emergency-contact", "/{userId}/emergency-contact"})
    // Méthode `updateEmergencyContact` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Patient ; intention : met à jour (update emergency contact)
    public ResponseEntity<ApiResponse<Patient>> updateEmergencyContact(
            // Paramètre `userId` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID userId,
            // Paramètre `nom` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String nom,
            // Paramètre `telephone` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String telephone,
            // Paramètre `lien` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String lien,
            // Paramètre `body` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody(required = false) Map<String, Object> body) {

        // Déclare la variable `effectiveNom` (chaîne de caractères) initialisée avec la valeur de nom
        String effectiveNom = nom;
        // Déclare la variable `effectiveTelephone` (chaîne de caractères) initialisée avec la valeur de telephone
        String effectiveTelephone = telephone;
        // Déclare la variable `effectiveLien` (chaîne de caractères) initialisée avec la valeur de lien
        String effectiveLien = lien;

        // Condition : exécute le bloc suivant seulement si `body != null`
        if (body != null) {
            // Condition : exécute le bloc suivant seulement si `effectiveNom == null && body.containsKey("nom")`
            if (effectiveNom == null && body.containsKey("nom")) {
                // Affecte à `effectiveNom` `String.valueOf(body.get("nom"))`
                effectiveNom = String.valueOf(body.get("nom"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveNom == null && body.containsKey("contactUrgenceNom")`
            if (effectiveNom == null && body.containsKey("contactUrgenceNom")) {
                // Affecte à `effectiveNom` `String.valueOf(body.get("contactUrgenceNom"))`
                effectiveNom = String.valueOf(body.get("contactUrgenceNom"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveNom == null && body.containsKey("personne_contact")`
            if (effectiveNom == null && body.containsKey("personne_contact")) {
                // Affecte à `effectiveNom` `String.valueOf(body.get("personne_contact"))`
                effectiveNom = String.valueOf(body.get("personne_contact"));
            }

            // Condition : exécute le bloc suivant seulement si `effectiveTelephone == null && body.containsKey("telephone")`
            if (effectiveTelephone == null && body.containsKey("telephone")) {
                // Affecte à `effectiveTelephone` `String.valueOf(body.get("telephone"))`
                effectiveTelephone = String.valueOf(body.get("telephone"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveTelephone == null && body.containsKey("contactUrgenceTelephone")`
            if (effectiveTelephone == null && body.containsKey("contactUrgenceTelephone")) {
                // Affecte à `effectiveTelephone` `String.valueOf(body.get("contactUrgenceTelephone"))`
                effectiveTelephone = String.valueOf(body.get("contactUrgenceTelephone"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveTelephone == null && body.containsKey("telephone_contact")`
            if (effectiveTelephone == null && body.containsKey("telephone_contact")) {
                // Affecte à `effectiveTelephone` `String.valueOf(body.get("telephone_contact"))`
                effectiveTelephone = String.valueOf(body.get("telephone_contact"));
            }

            // Condition : exécute le bloc suivant seulement si `effectiveLien == null && body.containsKey("lien")`
            if (effectiveLien == null && body.containsKey("lien")) {
                // Affecte à `effectiveLien` `String.valueOf(body.get("lien"))`
                effectiveLien = String.valueOf(body.get("lien"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveLien == null && body.containsKey("contactUrgenceLien")`
            if (effectiveLien == null && body.containsKey("contactUrgenceLien")) {
                // Affecte à `effectiveLien` `String.valueOf(body.get("contactUrgenceLien"))`
                effectiveLien = String.valueOf(body.get("contactUrgenceLien"));
            }
            // Condition : exécute le bloc suivant seulement si `effectiveLien == null && body.containsKey("lien_parente_contact")`
            if (effectiveLien == null && body.containsKey("lien_parente_contact")) {
                // Affecte à `effectiveLien` `String.valueOf(body.get("lien_parente_contact"))`
                effectiveLien = String.valueOf(body.get("lien_parente_contact"));
            }
        }

        // Déclare la variable `p` (Patient) initialisée avec `patientService.updateEmergencyContact(userId, effectiveNom, effectiveTelephone,…`
        Patient p = patientService.updateEmergencyContact(userId, effectiveNom, effectiveTelephone, effectiveLien);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Contact d'urgence mis à jour a…
        return ResponseEntity.ok(ApiResponse.success("Contact d'urgence mis à jour avec succès", p));
    }

    // Route HTTP GET sur le chemin « /user/{userId}/famille »
    @GetMapping("/user/{userId}/famille")
    // Méthode `getMembresFamille` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de MembreFamille ; intention : récupère (get membres famille)
    public ResponseEntity<ApiResponse<List<MembreFamille>>> getMembresFamille(@PathVariable UUID userId) {
        // Déclare la variable `liste` (liste de MembreFamille) initialisée avec `patientService.getMembresFamille(userId)`
        List<MembreFamille> liste = patientService.getMembresFamille(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Membres de la famille récupéré…
        return ResponseEntity.ok(ApiResponse.success("Membres de la famille récupérés", liste));
    }

    // Route HTTP POST sur le chemin « /user/{userId}/famille »
    @PostMapping("/user/{userId}/famille")
    // Méthode `ajouterMembreFamille` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de MembreFamille ; intention : ajoute (ajouter membre famille)
    public ResponseEntity<ApiResponse<MembreFamille>> ajouterMembreFamille(
            // Paramètre `userId` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID userId,
            // Paramètre `dto` de type MembreFamilleDto — corps json de la requête désérialisé en objet java
            @RequestBody MembreFamilleDto dto) {
        // Déclare la variable `mf` (MembreFamille) initialisée avec `patientService.ajouterMembreFamille(userId, dto)`
        MembreFamille mf = patientService.ajouterMembreFamille(userId, dto);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Membre de la famille ajouté", …
        return ResponseEntity.ok(ApiResponse.success("Membre de la famille ajouté", mf));
    }

    // Route HTTP GET sur le chemin « /admin/all »
    @GetMapping("/admin/all")
    // Méthode `getAllAdminPatients` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Patient ; intention : récupère (get all admin patients)
    public ResponseEntity<ApiResponse<List<Patient>>> getAllAdminPatients() {
        // Déclare la variable `list` (liste de Patient) initialisée avec la valeur de l'attribut AllPatients de patientService
        List<Patient> list = patientService.getAllPatients();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Tous les patients", list))
        return ResponseEntity.ok(ApiResponse.success("Tous les patients", list));
    }
}
