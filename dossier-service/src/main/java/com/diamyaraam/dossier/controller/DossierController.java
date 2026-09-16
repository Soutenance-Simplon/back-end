package com.diamyaraam.dossier.controller;

import com.diamyaraam.dossier.entity.*;
import com.diamyaraam.dossier.service.DossierMedicalService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dossiers")
public class DossierController {

    private final DossierMedicalService dossierMedicalService;

    public DossierController(DossierMedicalService dossierMedicalService) {
        this.dossierMedicalService = dossierMedicalService;
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<DossierMedical>> getByPatientId(@PathVariable UUID patientId) {
        DossierMedical dm = dossierMedicalService.getOrCreateDossier(patientId);
        return ResponseEntity.ok(ApiResponse.success("Dossier médical", dm));
    }

    // Allergies
    @PostMapping("/patient/{patientId}/allergies")
    public ResponseEntity<ApiResponse<Allergie>> addAllergie(@PathVariable UUID patientId, @RequestBody Allergie allergie) {
        Allergie created = dossierMedicalService.addAllergie(patientId, allergie);
        return ResponseEntity.ok(ApiResponse.success("Allergie ajoutée", created));
    }

    @GetMapping("/patient/{patientId}/allergies")
    public ResponseEntity<ApiResponse<List<Allergie>>> getAllergies(@PathVariable UUID patientId) {
        List<Allergie> list = dossierMedicalService.getAllergies(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des allergies", list));
    }

    // Antécédents
    @PostMapping("/patient/{patientId}/antecedents")
    public ResponseEntity<ApiResponse<AntecedentMedical>> addAntecedent(@PathVariable UUID patientId, @RequestBody AntecedentMedical antecedent) {
        AntecedentMedical created = dossierMedicalService.addAntecedent(patientId, antecedent);
        return ResponseEntity.ok(ApiResponse.success("Antécédent ajouté", created));
    }

    @GetMapping("/patient/{patientId}/antecedents")
    public ResponseEntity<ApiResponse<List<AntecedentMedical>>> getAntecedents(@PathVariable UUID patientId) {
        List<AntecedentMedical> list = dossierMedicalService.getAntecedents(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des antécédents", list));
    }

    // Maladies chroniques
    @PostMapping("/patient/{patientId}/maladies")
    public ResponseEntity<ApiResponse<MaladieCronique>> addMaladie(@PathVariable UUID patientId, @RequestBody MaladieCronique maladie) {
        MaladieCronique created = dossierMedicalService.addMaladie(patientId, maladie);
        return ResponseEntity.ok(ApiResponse.success("Maladie chronique ajoutée", created));
    }

    @GetMapping("/patient/{patientId}/maladies")
    public ResponseEntity<ApiResponse<List<MaladieCronique>>> getMaladies(@PathVariable UUID patientId) {
        List<MaladieCronique> list = dossierMedicalService.getMaladies(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des maladies chroniques", list));
    }

    // Prescriptions
    @PostMapping("/patient/{patientId}/prescriptions")
    public ResponseEntity<ApiResponse<Prescription>> createPrescription(@PathVariable UUID patientId, @RequestBody Prescription prescription) {
        Prescription created = dossierMedicalService.createPrescription(patientId, prescription);
        return ResponseEntity.ok(ApiResponse.success("Prescription créée", created));
    }

    @GetMapping("/patient/{patientId}/prescriptions")
    public ResponseEntity<ApiResponse<List<Prescription>>> getPrescriptions(@PathVariable UUID patientId) {
        List<Prescription> list = dossierMedicalService.getPrescriptions(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des prescriptions", list));
    }

    // Consultations
    @PostMapping("/patient/{patientId}/consultations")
    public ResponseEntity<ApiResponse<Consultation>> addConsultation(@PathVariable UUID patientId, @RequestBody Consultation consultation) {
        Consultation created = dossierMedicalService.addConsultation(patientId, consultation);
        return ResponseEntity.ok(ApiResponse.success("Consultation enregistrée", created));
    }

    @GetMapping("/patient/{patientId}/consultations")
    public ResponseEntity<ApiResponse<List<Consultation>>> getConsultations(@PathVariable UUID patientId) {
        List<Consultation> list = dossierMedicalService.getConsultations(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des consultations", list));
    }

    // Hospitalisations
    @PostMapping("/patient/{patientId}/hospitalisations")
    public ResponseEntity<ApiResponse<Hospitalisation>> addHospitalisation(@PathVariable UUID patientId, @RequestBody Hospitalisation hospitalisation) {
        Hospitalisation created = dossierMedicalService.addHospitalisation(patientId, hospitalisation);
        return ResponseEntity.ok(ApiResponse.success("Hospitalisation ajoutée", created));
    }

    @GetMapping("/patient/{patientId}/hospitalisations")
    public ResponseEntity<ApiResponse<List<Hospitalisation>>> getHospitalisations(@PathVariable UUID patientId) {
        List<Hospitalisation> list = dossierMedicalService.getHospitalisations(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des hospitalisations", list));
    }

    // Vaccinations
    @PostMapping("/patient/{patientId}/vaccinations")
    public ResponseEntity<ApiResponse<Vaccination>> addVaccination(@PathVariable UUID patientId, @RequestBody Vaccination vaccination) {
        Vaccination created = dossierMedicalService.addVaccination(patientId, vaccination);
        return ResponseEntity.ok(ApiResponse.success("Vaccination ajoutée", created));
    }

    @GetMapping("/patient/{patientId}/vaccinations")
    public ResponseEntity<ApiResponse<List<Vaccination>>> getVaccinations(@PathVariable UUID patientId) {
        List<Vaccination> list = dossierMedicalService.getVaccinations(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des vaccinations", list));
    }

    // Documents Médicaux
    @PostMapping("/patient/{patientId}/documents")
    public ResponseEntity<ApiResponse<DocumentMedical>> addDocument(@PathVariable UUID patientId, @RequestBody DocumentMedical document) {
        DocumentMedical created = dossierMedicalService.addDocument(patientId, document);
        return ResponseEntity.ok(ApiResponse.success("Document médical ajouté", created));
    }

    @GetMapping("/patient/{patientId}/documents")
    public ResponseEntity<ApiResponse<List<DocumentMedical>>> getDocuments(@PathVariable UUID patientId) {
        List<DocumentMedical> list = dossierMedicalService.getDocuments(patientId);
        return ResponseEntity.ok(ApiResponse.success("Liste des documents médicaux", list));
    }
}
