package com.diamyaraam.dossier.service;

import com.diamyaraam.dossier.entity.*;
import com.diamyaraam.dossier.repository.*;
import com.diamyaraam.dossier.util.RealtimePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final RealtimePublisher realtimePublisher;

    public DossierMedicalService(
            DossierMedicalRepository dossierMedicalRepository,
            AllergieRepository allergieRepository,
            AntecedentMedicalRepository antecedentRepository,
            MaladieCroniqueRepository maladieRepository,
            PrescriptionRepository prescriptionRepository,
            ConsultationRepository consultationRepository,
            HospitalisationRepository hospitalisationRepository,
            VaccinationRepository vaccinationRepository,
            DocumentMedicalRepository documentMedicalRepository,
            @Autowired(required = false) RealtimePublisher realtimePublisher) {
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.allergieRepository = allergieRepository;
        this.antecedentRepository = antecedentRepository;
        this.maladieRepository = maladieRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.consultationRepository = consultationRepository;
        this.hospitalisationRepository = hospitalisationRepository;
        this.vaccinationRepository = vaccinationRepository;
        this.documentMedicalRepository = documentMedicalRepository;
        this.realtimePublisher = realtimePublisher;
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
        Allergie saved = allergieRepository.save(allergie);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "ALLERGIE_AJOUTEE", Map.of(
                    "type", "ALLERGIE",
                    "patientId", patientId.toString(),
                    "allergie", saved.getNom() != null ? saved.getNom() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Mise à jour de votre dossier médical",
                    "corps", "Le médecin a enregistré une allergie dans votre dossier médical.",
                    "message", "Le médecin a enregistré une allergie dans votre dossier médical.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
    }

    public List<Allergie> getAllergies(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return allergieRepository.findByDossierMedicalId(dossier.getId());
    }

    @Transactional
    public AntecedentMedical addAntecedent(UUID patientId, AntecedentMedical antecedent) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        antecedent.setDossierMedical(dossier);
        AntecedentMedical saved = antecedentRepository.save(antecedent);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "ANTECEDENT_AJOUTE", Map.of(
                    "type", "ANTECEDENT",
                    "patientId", patientId.toString(),
                    "description", saved.getDescription() != null ? saved.getDescription() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Mise à jour de votre dossier médical",
                    "corps", "Le médecin a ajouté un antécédent médical à votre dossier.",
                    "message", "Le médecin a ajouté un antécédent médical à votre dossier.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
    }

    public List<AntecedentMedical> getAntecedents(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return antecedentRepository.findByDossierMedicalId(dossier.getId());
    }

    @Transactional
    public MaladieCronique addMaladie(UUID patientId, MaladieCronique maladie) {
        DossierMedical dossier = getOrCreateDossier(patientId);
        maladie.setDossierMedical(dossier);
        MaladieCronique saved = maladieRepository.save(maladie);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "MALADIE_AJOUTEE", Map.of(
                    "type", "MALADIE_CHRONIQUE",
                    "patientId", patientId.toString(),
                    "nom", saved.getNomMaladie() != null ? saved.getNomMaladie() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Mise à jour de votre dossier médical",
                    "corps", "Le médecin a enregistré une maladie chronique dans votre dossier.",
                    "message", "Le médecin a enregistré une maladie chronique dans votre dossier.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
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
        Prescription saved = prescriptionRepository.save(prescription);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "PRESCRIPTION_AJOUTEE", Map.of(
                    "type", "PRESCRIPTION",
                    "patientId", patientId.toString(),
                    "numeroPrescription", saved.getNumeroPrescription() != null ? saved.getNumeroPrescription() : "",
                    "medecinId", saved.getMedecinId() != null ? saved.getMedecinId().toString() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "ORDONNANCE",
                    "titre", "Nouvelle ordonnance médicale",
                    "corps", "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical.",
                    "message", "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
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
        Consultation saved = consultationRepository.save(consultation);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "CONSULTATION_AJOUTEE", Map.of(
                    "type", "CONSULTATION",
                    "patientId", patientId.toString(),
                    "diagnostic", saved.getDiagnostic() != null ? saved.getDiagnostic() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "CONSULTATION_TERMINEE",
                    "titre", "Compte-rendu de consultation",
                    "corps", "Une nouvelle note de consultation a été ajoutée à votre dossier.",
                    "message", "Une nouvelle note de consultation a été ajoutée à votre dossier.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
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
        Hospitalisation saved = hospitalisationRepository.save(hospitalisation);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "HOSPITALISATION_AJOUTEE", Map.of(
                    "type", "HOSPITALISATION",
                    "patientId", patientId.toString(),
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Mise à jour de votre dossier médical",
                    "corps", "Le médecin a enregistré une hospitalisation dans votre dossier médical.",
                    "message", "Le médecin a enregistré une hospitalisation dans votre dossier médical.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
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
        Vaccination saved = vaccinationRepository.save(vaccination);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "VACCINATION_AJOUTEE", Map.of(
                    "type", "VACCINATION",
                    "patientId", patientId.toString(),
                    "vaccin", saved.getNomVaccin() != null ? saved.getNomVaccin() : "",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Mise à jour de votre dossier médical",
                    "corps", "Le médecin a enregistré une vaccination dans votre dossier médical.",
                    "message", "Le médecin a enregistré une vaccination dans votre dossier médical.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
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
        DocumentMedical saved = documentMedicalRepository.save(document);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publish("/topic/dossier", "DOCUMENT_AJOUTE", Map.of(
                    "type", "DOCUMENT",
                    "patientId", patientId.toString(),
                    "nomFichier", saved.getTitre() != null ? saved.getTitre() : "Document",
                    "date", LocalDateTime.now().toString()
                ));
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    "userId", patientId.toString(),
                    "type", "DOSSIER_MIS_A_JOUR",
                    "titre", "Nouveau document médical",
                    "corps", "Le médecin a ajouté un document à votre dossier médical.",
                    "message", "Le médecin a ajouté un document à votre dossier médical.",
                    "dateEnvoi", LocalDateTime.now().toString(),
                    "lue", false
                ));
            } catch (Exception ignored) {}
        }

        return saved;
    }

    public List<DocumentMedical> getDocuments(UUID patientId) {
        DossierMedical dossier = getByPatientId(patientId);
        return documentMedicalRepository.findByDossierMedical(dossier);
    }
}
