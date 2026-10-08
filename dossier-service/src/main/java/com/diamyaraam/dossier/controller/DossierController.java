// Déclaration du package Java : `com.diamyaraam.dossier.controller`
package com.diamyaraam.dossier.controller;

// Import de toutes les classes du paquet `com.diamyaraam.dossier.entity`
import com.diamyaraam.dossier.entity.*;
// Import de la classe `DossierMedicalService` (paquet com.diamyaraam.dossier.service)
import com.diamyaraam.dossier.service.DossierMedicalService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Contrôleur REST : les valeurs retournées sont sérialisées en JSON
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /dossiers »
@RequestMapping("/dossiers")
// Déclaration de la classe `DossierController` (rôle : expose des routes HTTP)
public class DossierController {

    // Attribut `dossierMedicalService` de type DossierMedicalService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final DossierMedicalService dossierMedicalService;

    // Constructeur de `DossierController` — paramètres : `dossierMedicalService` (DossierMedicalService) (injection des dépendances par Spring)
    public DossierController(DossierMedicalService dossierMedicalService) {
        // Initialise l'attribut `dossierMedicalService` avec la valeur de dossierMedicalService
        this.dossierMedicalService = dossierMedicalService;
    }

    // Route HTTP GET sur le chemin « /patient/{patientId} »
    @GetMapping("/patient/{patientId}")
    // Méthode `getByPatientId` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de DossierMedical ; intention : récupère (get by patient id)
    public ResponseEntity<ApiResponse<DossierMedical>> getByPatientId(@PathVariable UUID patientId) {
        // Déclare la variable `dm` (DossierMedical) initialisée avec `dossierMedicalService.getOrCreateDossier(patientId)`
        DossierMedical dm = dossierMedicalService.getOrCreateDossier(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Dossier médical", dm))
        return ResponseEntity.ok(ApiResponse.success("Dossier médical", dm));
    }

    // Allergies
    @PostMapping("/patient/{patientId}/allergies")
    // Méthode `addAllergie` (publique) — paramètres : `patientId` (identifiant UUID), `allergie` (Allergie) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Allergie ; intention : ajoute (add allergie)
    public ResponseEntity<ApiResponse<Allergie>> addAllergie(@PathVariable UUID patientId, @RequestBody Allergie allergie) {
        // Déclare la variable `created` (Allergie) initialisée avec `dossierMedicalService.addAllergie(patientId, allergie)`
        Allergie created = dossierMedicalService.addAllergie(patientId, allergie);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Allergie ajoutée", created))
        return ResponseEntity.ok(ApiResponse.success("Allergie ajoutée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/allergies »
    @GetMapping("/patient/{patientId}/allergies")
    // Méthode `getAllergies` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Allergie ; intention : récupère (get allergies)
    public ResponseEntity<ApiResponse<List<Allergie>>> getAllergies(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de Allergie) initialisée avec `dossierMedicalService.getAllergies(patientId)`
        List<Allergie> list = dossierMedicalService.getAllergies(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des allergies", list))
        return ResponseEntity.ok(ApiResponse.success("Liste des allergies", list));
    }

    // Antécédents
    @PostMapping("/patient/{patientId}/antecedents")
    // Méthode `addAntecedent` (publique) — paramètres : `patientId` (identifiant UUID), `antecedent` (AntecedentMedical) ; retourne : réponse HTTP contenant enveloppe ApiResponse de AntecedentMedical ; intention : ajoute (add antecedent)
    public ResponseEntity<ApiResponse<AntecedentMedical>> addAntecedent(@PathVariable UUID patientId, @RequestBody AntecedentMedical antecedent) {
        // Déclare la variable `created` (AntecedentMedical) initialisée avec `dossierMedicalService.addAntecedent(patientId, antecedent)`
        AntecedentMedical created = dossierMedicalService.addAntecedent(patientId, antecedent);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Antécédent ajouté", created))
        return ResponseEntity.ok(ApiResponse.success("Antécédent ajouté", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/antecedents »
    @GetMapping("/patient/{patientId}/antecedents")
    // Méthode `getAntecedents` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de AntecedentMedical ; intention : récupère (get antecedents)
    public ResponseEntity<ApiResponse<List<AntecedentMedical>>> getAntecedents(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de AntecedentMedical) initialisée avec `dossierMedicalService.getAntecedents(patientId)`
        List<AntecedentMedical> list = dossierMedicalService.getAntecedents(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des antécédents", list))
        return ResponseEntity.ok(ApiResponse.success("Liste des antécédents", list));
    }

    // Maladies chroniques
    @PostMapping("/patient/{patientId}/maladies")
    // Méthode `addMaladie` (publique) — paramètres : `patientId` (identifiant UUID), `maladie` (MaladieCronique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de MaladieCronique ; intention : ajoute (add maladie)
    public ResponseEntity<ApiResponse<MaladieCronique>> addMaladie(@PathVariable UUID patientId, @RequestBody MaladieCronique maladie) {
        // Déclare la variable `created` (MaladieCronique) initialisée avec `dossierMedicalService.addMaladie(patientId, maladie)`
        MaladieCronique created = dossierMedicalService.addMaladie(patientId, maladie);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Maladie chronique ajoutée", cr…
        return ResponseEntity.ok(ApiResponse.success("Maladie chronique ajoutée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/maladies »
    @GetMapping("/patient/{patientId}/maladies")
    // Méthode `getMaladies` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de MaladieCronique ; intention : récupère (get maladies)
    public ResponseEntity<ApiResponse<List<MaladieCronique>>> getMaladies(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de MaladieCronique) initialisée avec `dossierMedicalService.getMaladies(patientId)`
        List<MaladieCronique> list = dossierMedicalService.getMaladies(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des maladies chroniques"…
        return ResponseEntity.ok(ApiResponse.success("Liste des maladies chroniques", list));
    }

    // Prescriptions
    @PostMapping("/patient/{patientId}/prescriptions")
    // Méthode `createPrescription` (publique) — paramètres : `patientId` (identifiant UUID), `prescription` (Prescription) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Prescription ; intention : crée (create prescription)
    public ResponseEntity<ApiResponse<Prescription>> createPrescription(@PathVariable UUID patientId, @RequestBody Prescription prescription) {
        // Déclare la variable `created` (Prescription) initialisée avec `dossierMedicalService.createPrescription(patientId, prescription)`
        Prescription created = dossierMedicalService.createPrescription(patientId, prescription);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Prescription créée", created))
        return ResponseEntity.ok(ApiResponse.success("Prescription créée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/prescriptions »
    @GetMapping("/patient/{patientId}/prescriptions")
    // Méthode `getPrescriptions` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Prescription ; intention : récupère (get prescriptions)
    public ResponseEntity<ApiResponse<List<Prescription>>> getPrescriptions(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de Prescription) initialisée avec `dossierMedicalService.getPrescriptions(patientId)`
        List<Prescription> list = dossierMedicalService.getPrescriptions(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des prescriptions", list…
        return ResponseEntity.ok(ApiResponse.success("Liste des prescriptions", list));
    }

    // Consultations
    @PostMapping("/patient/{patientId}/consultations")
    // Méthode `addConsultation` (publique) — paramètres : `patientId` (identifiant UUID), `consultation` (Consultation) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Consultation ; intention : ajoute (add consultation)
    public ResponseEntity<ApiResponse<Consultation>> addConsultation(@PathVariable UUID patientId, @RequestBody Consultation consultation) {
        // Déclare la variable `created` (Consultation) initialisée avec `dossierMedicalService.addConsultation(patientId, consultation)`
        Consultation created = dossierMedicalService.addConsultation(patientId, consultation);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Consultation enregistrée", cre…
        return ResponseEntity.ok(ApiResponse.success("Consultation enregistrée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/consultations »
    @GetMapping("/patient/{patientId}/consultations")
    // Méthode `getConsultations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Consultation ; intention : récupère (get consultations)
    public ResponseEntity<ApiResponse<List<Consultation>>> getConsultations(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de Consultation) initialisée avec `dossierMedicalService.getConsultations(patientId)`
        List<Consultation> list = dossierMedicalService.getConsultations(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des consultations", list…
        return ResponseEntity.ok(ApiResponse.success("Liste des consultations", list));
    }

    // Hospitalisations
    @PostMapping("/patient/{patientId}/hospitalisations")
    // Méthode `addHospitalisation` (publique) — paramètres : `patientId` (identifiant UUID), `hospitalisation` (Hospitalisation) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Hospitalisation ; intention : ajoute (add hospitalisation)
    public ResponseEntity<ApiResponse<Hospitalisation>> addHospitalisation(@PathVariable UUID patientId, @RequestBody Hospitalisation hospitalisation) {
        // Déclare la variable `created` (Hospitalisation) initialisée avec `dossierMedicalService.addHospitalisation(patientId, hospitalisation)`
        Hospitalisation created = dossierMedicalService.addHospitalisation(patientId, hospitalisation);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Hospitalisation ajoutée", crea…
        return ResponseEntity.ok(ApiResponse.success("Hospitalisation ajoutée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/hospitalisations »
    @GetMapping("/patient/{patientId}/hospitalisations")
    // Méthode `getHospitalisations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Hospitalisation ; intention : récupère (get hospitalisations)
    public ResponseEntity<ApiResponse<List<Hospitalisation>>> getHospitalisations(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de Hospitalisation) initialisée avec `dossierMedicalService.getHospitalisations(patientId)`
        List<Hospitalisation> list = dossierMedicalService.getHospitalisations(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des hospitalisations", l…
        return ResponseEntity.ok(ApiResponse.success("Liste des hospitalisations", list));
    }

    // Vaccinations
    @PostMapping("/patient/{patientId}/vaccinations")
    // Méthode `addVaccination` (publique) — paramètres : `patientId` (identifiant UUID), `vaccination` (Vaccination) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Vaccination ; intention : ajoute (add vaccination)
    public ResponseEntity<ApiResponse<Vaccination>> addVaccination(@PathVariable UUID patientId, @RequestBody Vaccination vaccination) {
        // Déclare la variable `created` (Vaccination) initialisée avec `dossierMedicalService.addVaccination(patientId, vaccination)`
        Vaccination created = dossierMedicalService.addVaccination(patientId, vaccination);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Vaccination ajoutée", created))
        return ResponseEntity.ok(ApiResponse.success("Vaccination ajoutée", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/vaccinations »
    @GetMapping("/patient/{patientId}/vaccinations")
    // Méthode `getVaccinations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Vaccination ; intention : récupère (get vaccinations)
    public ResponseEntity<ApiResponse<List<Vaccination>>> getVaccinations(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de Vaccination) initialisée avec `dossierMedicalService.getVaccinations(patientId)`
        List<Vaccination> list = dossierMedicalService.getVaccinations(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des vaccinations", list))
        return ResponseEntity.ok(ApiResponse.success("Liste des vaccinations", list));
    }

    // Documents Médicaux
    @PostMapping("/patient/{patientId}/documents")
    // Méthode `addDocument` (publique) — paramètres : `patientId` (identifiant UUID), `document` (DocumentMedical) ; retourne : réponse HTTP contenant enveloppe ApiResponse de DocumentMedical ; intention : ajoute (add document)
    public ResponseEntity<ApiResponse<DocumentMedical>> addDocument(@PathVariable UUID patientId, @RequestBody DocumentMedical document) {
        // Déclare la variable `created` (DocumentMedical) initialisée avec `dossierMedicalService.addDocument(patientId, document)`
        DocumentMedical created = dossierMedicalService.addDocument(patientId, document);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Document médical ajouté", crea…
        return ResponseEntity.ok(ApiResponse.success("Document médical ajouté", created));
    }

    // Route HTTP GET sur le chemin « /patient/{patientId}/documents »
    @GetMapping("/patient/{patientId}/documents")
    // Méthode `getDocuments` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de DocumentMedical ; intention : récupère (get documents)
    public ResponseEntity<ApiResponse<List<DocumentMedical>>> getDocuments(@PathVariable UUID patientId) {
        // Déclare la variable `list` (liste de DocumentMedical) initialisée avec `dossierMedicalService.getDocuments(patientId)`
        List<DocumentMedical> list = dossierMedicalService.getDocuments(patientId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des documents médicaux",…
        return ResponseEntity.ok(ApiResponse.success("Liste des documents médicaux", list));
    }
}
