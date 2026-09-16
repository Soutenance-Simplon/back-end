package com.diamyaraam.dossier.service;

import com.diamyaraam.dossier.entity.*;
import com.diamyaraam.dossier.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DossierMedicalService {

    private final DossierMedicalRepository dossierMedicalRepository;
    private final AllergieRepository allergieRepository;
    private final AntecedentMedicalRepository antecedentRepository;
    private final MaladieCroniqueRepository maladieRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationRepository consultationRepository;
    private final HospitalisationRepository hospitalisationRepository;
    private final VaccinationRepository vaccinationRepository;
    private final DocumentMedicalRepository documentMedicalRepository;

    public DossierMedicalService(
            DossierMedicalRepository dossierMedicalRepository,
            AllergieRepository allergieRepository,
            AntecedentMedicalRepository antecedentRepository,
            MaladieCroniqueRepository maladieRepository,
            PrescriptionRepository prescriptionRepository,
            ConsultationRepository consultationRepository,
            HospitalisationRepository hospitalisationRepository,
            VaccinationRepository vaccinationRepository,
            DocumentMedicalRepository documentMedicalRepository) {
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.allergieRepository = allergieRepository;
        this.antecedentRepository = antecedentRepository;
        this.maladieRepository = maladieRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.consultationRepository = consultationRepository;
        this.hospitalisationRepository = hospitalisationRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.documentMedicalRepository = documentMedicalRepository;
    }

    @Transactional
    public DossierMedical getOrCreateDossier(UUID patientId) {
        return dossierMedicalRepository.findByPatientId(patientId).orElseGet(() -> {
            DossierMedical dm = new DossierMedical();
            dm.setPatientId(patientId);
            dm.setCodeQrSecurise("QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            return dossierMedicalRepository.save(dm);
        });
    }

    public DossierMedical getByPatientId(UUID patientId) {
        return dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier médical introuvable."));
    }

    @Transactional
    public Allergie addAllergie(UUID patientId, Allergie allergie) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        allergie.setDossierMedical(dossier);
        return allergieRepository.save(allergie);
    }

    public List<Allergie> getAllergies(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return allergieRepository.findByDossierMedicalId(dossier.getId());
    }

    @Transactional
    public AntecedentMedical addAntecedent(UUID patientId, AntecedentMedical antecedent) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        antecedent.setDossierMedical(dossier);
        return antecedentRepository.save(antecedent);
    }

    public List<AntecedentMedical> getAntecedents(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return antecedentRepository.findByDossierMedicalId(dossier.getId());
    }

    @Transactional
    public MaladieCronique addMaladie(UUID patientId, MaladieCronique maladie) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        maladie.setDossierMedical(dossier);
        return maladieRepository.save(maladie);
    }

    public List<MaladieCronique> getMaladies(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return maladieRepository.findByDossierMedicalIdAndArchiveFalse(dossier.getId());
    }

    @Transactional
    public Prescription createPrescription(UUID patientId, Prescription prescription) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        prescription.setDossierMedical(dossier);
        prescription.setNumeroPrescription("PRESC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getPrescriptions(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return prescriptionRepository.findByDossierMedicalId(dossier.getId());
    }

    // Consultations
    @Transactional
    public Consultation addConsultation(UUID patientId, Consultation consultation) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        consultation.setDossierMedical(dossier);
        return consultationRepository.save(consultation);
    }

    public List<Consultation> getConsultations(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return consultationRepository.findByDossierMedical(dossier);
    }

    // Hospitalisations
    @Transactional
    public Hospitalisation addHospitalisation(UUID patientId, Hospitalisation hospitalisation) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        hospitalisation.setDossierMedical(dossier);
        return hospitalisationRepository.save(hospitalisation);
    }

    public List<Hospitalisation> getHospitalisations(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return hospitalisationRepository.findByDossierMedical(dossier);
    }

    // Vaccinations
    @Transactional
    public Vaccination addVaccination(UUID patientId, Vaccination vaccination) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        vaccination.setDossierMedical(dossier);
        return vaccinationRepository.save(vaccination);
    }

    public List<Vaccination> getVaccinations(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return vaccinationRepository.findByDossierMedical(dossier);
    }

    // Documents médicaux
    @Transactional
    public DocumentMedical addDocument(UUID patientId, DocumentMedical document) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        document.setDossierMedical(dossier);
        return documentMedicalRepository.save(document);
    }

    public List<DocumentMedical> getDocuments(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return documentMedicalRepository.findByDossierMedical(dossier);
    }
}
