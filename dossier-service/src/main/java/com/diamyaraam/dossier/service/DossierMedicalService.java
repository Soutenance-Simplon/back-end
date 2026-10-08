// Déclaration du package Java : `com.diamyaraam.dossier.service`
package com.diamyaraam.dossier.service;

// Import de toutes les classes du paquet `com.diamyaraam.dossier.entity`
import com.diamyaraam.dossier.entity.*;
// Import de toutes les classes du paquet `com.diamyaraam.dossier.repository`
import com.diamyaraam.dossier.repository.*;
// Import de la classe `RealtimePublisher` (paquet com.diamyaraam.dossier.util)
import com.diamyaraam.dossier.util.RealtimePublisher;
// Import de la classe `Autowired` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Autowired;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ============================================================================
 * SERVICE MÉTIER DU DOSSIER MÉDICAL ÉLECTRONIQUE (EHR - DIAM-YARAAM)
 * ============================================================================
 * RÔLE ARCHITECTURAL (POINT CENTRAL POUR LE JURY DE SOUTENANCE) :
 * Ce composant gère l'historique médical complet et unifié d'un patient à travers
 * le temps et les différentes structures de soins.
 *
 * MODÉLISATION MÉDICALE CONFORME AUX NORMES DE SANTÉ :
 * 1. Structuration modulaire des rubriques cliniques :
 *    - Allergies (avec niveau de gravité et réaction clinique)
 *    - Antécédents médicaux / chirurgicaux / familiaux
 *    - Affections chroniques (diabète, HTA, drépanocytose, etc.)
 *    - Consultations, prescriptions et hospitalisations
 *    - Carnet vaccinal électronique
 * 2. Diffusion Temps Réel (STOMP / WebSocket) :
 *    - Chaque modification clinique est notifiée via le broker `/topic/dossier`
 *      permettant le rafraîchissement instantané des écrans praticiens et patients.
 * 3. Intégrité Transactionnelle :
 *    - Annoté avec `@Transactional` pour garantir l'atomicité des écritures en base
 *      et la conformité ACID exigée par le RGPD et la Loi 2008-12 (CDP Sénégal).
 * ============================================================================
 */
@Service
// Déclaration de la classe `DossierMedicalService` (rôle : porte la logique métier)
public class DossierMedicalService {

    // Attribut `dossierMedicalRepository` de type DossierMedicalRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final DossierMedicalRepository dossierMedicalRepository;
    // Attribut `allergieRepository` de type AllergieRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final AllergieRepository allergieRepository;
    // Attribut `antecedentRepository` de type AntecedentMedicalRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final AntecedentMedicalRepository antecedentRepository;
    // Attribut `maladieRepository` de type MaladieCroniqueRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final MaladieCroniqueRepository maladieRepository;
    // Attribut `prescriptionRepository` de type PrescriptionRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final PrescriptionRepository prescriptionRepository;
    // Attribut `consultationRepository` de type ConsultationRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final ConsultationRepository consultationRepository;
    // Attribut `hospitalisationRepository` de type HospitalisationRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final HospitalisationRepository hospitalisationRepository;
    // Attribut `vaccinationRepository` de type VaccinationRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final VaccinationRepository vaccinationRepository;
    // Attribut `documentMedicalRepository` de type DocumentMedicalRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final DocumentMedicalRepository documentMedicalRepository;
    // Attribut `realtimePublisher` de type RealtimePublisher — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RealtimePublisher realtimePublisher;

    // Constructeur de `DossierMedicalService`
    public DossierMedicalService(
            // Paramètre `dossierMedicalRepository` de type DossierMedicalRepository
            DossierMedicalRepository dossierMedicalRepository,
            // Paramètre `allergieRepository` de type AllergieRepository
            AllergieRepository allergieRepository,
            // Paramètre `antecedentRepository` de type AntecedentMedicalRepository
            AntecedentMedicalRepository antecedentRepository,
            // Paramètre `maladieRepository` de type MaladieCroniqueRepository
            MaladieCroniqueRepository maladieRepository,
            // Paramètre `prescriptionRepository` de type PrescriptionRepository
            PrescriptionRepository prescriptionRepository,
            // Paramètre `consultationRepository` de type ConsultationRepository
            ConsultationRepository consultationRepository,
            // Paramètre `hospitalisationRepository` de type HospitalisationRepository
            HospitalisationRepository hospitalisationRepository,
            // Paramètre `vaccinationRepository` de type VaccinationRepository
            VaccinationRepository vaccinationRepository,
            // Paramètre `documentMedicalRepository` de type DocumentMedicalRepository
            DocumentMedicalRepository documentMedicalRepository,
            // Paramètre `realtimePublisher` de type RealtimePublisher — injection automatique de dépendance par spring
            @Autowired(required = false) RealtimePublisher realtimePublisher) {
        // Initialise l'attribut `dossierMedicalRepository` avec la valeur de dossierMedicalRepository
        this.dossierMedicalRepository = dossierMedicalRepository;
        // Initialise l'attribut `allergieRepository` avec la valeur de allergieRepository
        this.allergieRepository = allergieRepository;
        // Initialise l'attribut `antecedentRepository` avec la valeur de antecedentRepository
        this.antecedentRepository = antecedentRepository;
        // Initialise l'attribut `maladieRepository` avec la valeur de maladieRepository
        this.maladieRepository = maladieRepository;
        // Initialise l'attribut `prescriptionRepository` avec la valeur de prescriptionRepository
        this.prescriptionRepository = prescriptionRepository;
        // Initialise l'attribut `consultationRepository` avec la valeur de consultationRepository
        this.consultationRepository = consultationRepository;
        // Initialise l'attribut `hospitalisationRepository` avec la valeur de hospitalisationRepository
        this.hospitalisationRepository = hospitalisationRepository;
        // Initialise l'attribut `vaccinationRepository` avec la valeur de vaccinationRepository
        this.vaccinationRepository = vaccinationRepository;
        // Initialise l'attribut `documentMedicalRepository` avec la valeur de documentMedicalRepository
        this.documentMedicalRepository = documentMedicalRepository;
        // Initialise l'attribut `realtimePublisher` avec la valeur de realtimePublisher
        this.realtimePublisher = realtimePublisher;
    }

    /**
     * Récupère le dossier médical existant ou en initialise un nouveau avec un identifiant
     * de QR code sécurisé unique si le patient consulte pour la première fois.
     *
     * @param patientId Identifiant UUID unique du patient
     * @return L'entité DossierMedical persistée
     */
    @Transactional
    // Méthode `getOrCreateDossier` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : DossierMedical ; intention : récupère ou crée (get or create dossier)
    public DossierMedical getOrCreateDossier(UUID patientId) {
        // Retourne le résultat de la requête findByPatientId exécutée via dossierMedicalRepository
        return dossierMedicalRepository.findByPatientId(patientId).orElseGet(() -> {
            // Déclare la variable `dm` (DossierMedical) initialisée avec une nouvelle instance de DossierMedical
            DossierMedical dm = new DossierMedical();
            // Renseigne la propriété PatientId de `dm` avec la valeur de patientId
            dm.setPatientId(patientId);
            // Renseigne la propriété CodeQrSecurise de `dm` avec un UUID aléatoire ("QR-" + UUID.randomUUID().toString().substring(0, 8).toUppe…)
            dm.setCodeQrSecurise("QR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            // Retourne l'enregistrement en base de dm via dossierMedicalRepository
            return dossierMedicalRepository.save(dm);
        });
    }

    // Méthode `getByPatientId` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : DossierMedical ; intention : récupère (get by patient id)
    public DossierMedical getByPatientId(UUID patientId) {
        // Retourne le résultat de la requête findByPatientId exécutée via dossierMedicalRepository
        return dossierMedicalRepository.findByPatientId(patientId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Dossier médic…`
                .orElseThrow(() -> new IllegalArgumentException("Dossier médical introuvable."));
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `addAllergie` (publique) — paramètres : `patientId` (identifiant UUID), `allergie` (Allergie) ; retourne : Allergie ; intention : ajoute (add allergie)
    public Allergie addAllergie(UUID patientId, Allergie allergie) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `allergie` avec la valeur de dossier
        allergie.setDossierMedical(dossier);
        // Déclare la variable `saved` (Allergie) initialisée avec l'enregistrement en base de allergie via allergieRepository
        Allergie saved = allergieRepository.save(allergie);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "ALLERGIE_AJOUTEE", Map.of(
                realtimePublisher.publish("/topic/dossier", "ALLERGIE_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "ALLERGIE"
                    "type", "ALLERGIE",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « allergie » associée à `saved.getNom() != null ? saved.getNom() : ""`
                    "allergie", saved.getNom() != null ? saved.getNom() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Mise à jour de votre dossier médical"
                    "titre", "Mise à jour de votre dossier médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a enregistré une allergie dans votre dossier médical."
                    "corps", "Le médecin a enregistré une allergie dans votre dossier médical.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a enregistré une allergie dans votre dossier médical."
                    "message", "Le médecin a enregistré une allergie dans votre dossier médical.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getAllergies` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de Allergie ; intention : récupère (get allergies)
    public List<Allergie> getAllergies(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedicalId exécutée via allergieRepository
        return allergieRepository.findByDossierMedicalId(dossier.getId());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `addAntecedent` (publique) — paramètres : `patientId` (identifiant UUID), `antecedent` (AntecedentMedical) ; retourne : AntecedentMedical ; intention : ajoute (add antecedent)
    public AntecedentMedical addAntecedent(UUID patientId, AntecedentMedical antecedent) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `antecedent` avec la valeur de dossier
        antecedent.setDossierMedical(dossier);
        // Déclare la variable `saved` (AntecedentMedical) initialisée avec l'enregistrement en base de antecedent via antecedentRepository
        AntecedentMedical saved = antecedentRepository.save(antecedent);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "ANTECEDENT_AJOUTE", Map.of(
                realtimePublisher.publish("/topic/dossier", "ANTECEDENT_AJOUTE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "ANTECEDENT"
                    "type", "ANTECEDENT",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « description » associée à `saved.getDescription() != null ? saved.getDescription() : ""`
                    "description", saved.getDescription() != null ? saved.getDescription() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Mise à jour de votre dossier médical"
                    "titre", "Mise à jour de votre dossier médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a ajouté un antécédent médical à votre dossier."
                    "corps", "Le médecin a ajouté un antécédent médical à votre dossier.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a ajouté un antécédent médical à votre dossier."
                    "message", "Le médecin a ajouté un antécédent médical à votre dossier.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getAntecedents` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de AntecedentMedical ; intention : récupère (get antecedents)
    public List<AntecedentMedical> getAntecedents(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedicalId exécutée via antecedentRepository
        return antecedentRepository.findByDossierMedicalId(dossier.getId());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `addMaladie` (publique) — paramètres : `patientId` (identifiant UUID), `maladie` (MaladieCronique) ; retourne : MaladieCronique ; intention : ajoute (add maladie)
    public MaladieCronique addMaladie(UUID patientId, MaladieCronique maladie) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `maladie` avec la valeur de dossier
        maladie.setDossierMedical(dossier);
        // Déclare la variable `saved` (MaladieCronique) initialisée avec l'enregistrement en base de maladie via maladieRepository
        MaladieCronique saved = maladieRepository.save(maladie);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "MALADIE_AJOUTEE", Map.of(
                realtimePublisher.publish("/topic/dossier", "MALADIE_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "MALADIE_CHRONIQUE"
                    "type", "MALADIE_CHRONIQUE",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « nom » associée à `saved.getNomMaladie() != null ? saved.getNomMaladie() : ""`
                    "nom", saved.getNomMaladie() != null ? saved.getNomMaladie() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Mise à jour de votre dossier médical"
                    "titre", "Mise à jour de votre dossier médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a enregistré une maladie chronique dans votre dossier."
                    "corps", "Le médecin a enregistré une maladie chronique dans votre dossier.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a enregistré une maladie chronique dans votre dossier."
                    "message", "Le médecin a enregistré une maladie chronique dans votre dossier.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getMaladies` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de MaladieCronique ; intention : récupère (get maladies)
    public List<MaladieCronique> getMaladies(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedicalIdAndArchiveFalse exécutée via maladieRepository
        return maladieRepository.findByDossierMedicalIdAndArchiveFalse(dossier.getId());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `createPrescription` (publique) — paramètres : `patientId` (identifiant UUID), `prescription` (Prescription) ; retourne : Prescription ; intention : crée (create prescription)
    public Prescription createPrescription(UUID patientId, Prescription prescription) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `prescription` avec la valeur de dossier
        prescription.setDossierMedical(dossier);
        // Renseigne la propriété NumeroPrescription de `prescription` avec un UUID aléatoire ("PRESC-" + UUID.randomUUID().toString().substring(0, 8).toU…)
        prescription.setNumeroPrescription("PRESC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Déclare la variable `saved` (Prescription) initialisée avec l'enregistrement en base de prescription via prescriptionRepository
        Prescription saved = prescriptionRepository.save(prescription);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "PRESCRIPTION_AJOUTEE", Map.of(
                realtimePublisher.publish("/topic/dossier", "PRESCRIPTION_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "PRESCRIPTION"
                    "type", "PRESCRIPTION",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « numeroPrescription » associée à `saved.getNumeroPrescription() != null ? saved.getNumeroPrescription() : ""`
                    "numeroPrescription", saved.getNumeroPrescription() != null ? saved.getNumeroPrescription() : "",
                    // Paire clé/valeur : clé « medecinId » associée à `saved.getMedecinId() != null ? saved.getMedecinId().toString() : ""`
                    "medecinId", saved.getMedecinId() != null ? saved.getMedecinId().toString() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "ORDONNANCE"
                    "type", "ORDONNANCE",
                    // Paire clé/valeur : clé « titre » associée à le texte "Nouvelle ordonnance médicale"
                    "titre", "Nouvelle ordonnance médicale",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical."
                    "corps", "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical."
                    "message", "Le médecin a ajouté une nouvelle ordonnance à votre dossier médical.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getPrescriptions` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de Prescription ; intention : récupère (get prescriptions)
    public List<Prescription> getPrescriptions(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedicalId exécutée via prescriptionRepository
        return prescriptionRepository.findByDossierMedicalId(dossier.getId());
    }

    // Consultations
    @Transactional
    // Méthode `addConsultation` (publique) — paramètres : `patientId` (identifiant UUID), `consultation` (Consultation) ; retourne : Consultation ; intention : ajoute (add consultation)
    public Consultation addConsultation(UUID patientId, Consultation consultation) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `consultation` avec la valeur de dossier
        consultation.setDossierMedical(dossier);
        // Déclare la variable `saved` (Consultation) initialisée avec l'enregistrement en base de consultation via consultationRepository
        Consultation saved = consultationRepository.save(consultation);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "CONSULTATION_AJOUTEE", Map.of(
                realtimePublisher.publish("/topic/dossier", "CONSULTATION_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "CONSULTATION"
                    "type", "CONSULTATION",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « diagnostic » associée à `saved.getDiagnostic() != null ? saved.getDiagnostic() : ""`
                    "diagnostic", saved.getDiagnostic() != null ? saved.getDiagnostic() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "CONSULTATION_TERMINEE"
                    "type", "CONSULTATION_TERMINEE",
                    // Paire clé/valeur : clé « titre » associée à le texte "Compte-rendu de consultation"
                    "titre", "Compte-rendu de consultation",
                    // Paire clé/valeur : clé « corps » associée à le texte "Une nouvelle note de consultation a été ajoutée à votre dossier."
                    "corps", "Une nouvelle note de consultation a été ajoutée à votre dossier.",
                    // Paire clé/valeur : clé « message » associée à le texte "Une nouvelle note de consultation a été ajoutée à votre dossier."
                    "message", "Une nouvelle note de consultation a été ajoutée à votre dossier.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getConsultations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de Consultation ; intention : récupère (get consultations)
    public List<Consultation> getConsultations(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedical exécutée via consultationRepository
        return consultationRepository.findByDossierMedical(dossier);
    }

    // Hospitalisations
    @Transactional
    // Méthode `addHospitalisation` (publique) — paramètres : `patientId` (identifiant UUID), `hospitalisation` (Hospitalisation) ; retourne : Hospitalisation ; intention : ajoute (add hospitalisation)
    public Hospitalisation addHospitalisation(UUID patientId, Hospitalisation hospitalisation) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `hospitalisation` avec la valeur de dossier
        hospitalisation.setDossierMedical(dossier);
        // Déclare la variable `saved` (Hospitalisation) initialisée avec l'enregistrement en base de hospitalisation via hospitalisationRepository
        Hospitalisation saved = hospitalisationRepository.save(hospitalisation);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "HOSPITALISATION_AJOUTEE", Map…
                realtimePublisher.publish("/topic/dossier", "HOSPITALISATION_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "HOSPITALISATION"
                    "type", "HOSPITALISATION",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Mise à jour de votre dossier médical"
                    "titre", "Mise à jour de votre dossier médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a enregistré une hospitalisation dans votre dossier médic…
                    "corps", "Le médecin a enregistré une hospitalisation dans votre dossier médical.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a enregistré une hospitalisation dans votre dossier médic…
                    "message", "Le médecin a enregistré une hospitalisation dans votre dossier médical.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getHospitalisations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de Hospitalisation ; intention : récupère (get hospitalisations)
    public List<Hospitalisation> getHospitalisations(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedical exécutée via hospitalisationRepository
        return hospitalisationRepository.findByDossierMedical(dossier);
    }

    // Vaccinations
    @Transactional
    // Méthode `addVaccination` (publique) — paramètres : `patientId` (identifiant UUID), `vaccination` (Vaccination) ; retourne : Vaccination ; intention : ajoute (add vaccination)
    public Vaccination addVaccination(UUID patientId, Vaccination vaccination) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `vaccination` avec la valeur de dossier
        vaccination.setDossierMedical(dossier);
        // Déclare la variable `saved` (Vaccination) initialisée avec l'enregistrement en base de vaccination via vaccinationRepository
        Vaccination saved = vaccinationRepository.save(vaccination);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "VACCINATION_AJOUTEE", Map.of(
                realtimePublisher.publish("/topic/dossier", "VACCINATION_AJOUTEE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "VACCINATION"
                    "type", "VACCINATION",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « vaccin » associée à `saved.getNomVaccin() != null ? saved.getNomVaccin() : ""`
                    "vaccin", saved.getNomVaccin() != null ? saved.getNomVaccin() : "",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Mise à jour de votre dossier médical"
                    "titre", "Mise à jour de votre dossier médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a enregistré une vaccination dans votre dossier médical."
                    "corps", "Le médecin a enregistré une vaccination dans votre dossier médical.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a enregistré une vaccination dans votre dossier médical."
                    "message", "Le médecin a enregistré une vaccination dans votre dossier médical.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getVaccinations` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de Vaccination ; intention : récupère (get vaccinations)
    public List<Vaccination> getVaccinations(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedical exécutée via vaccinationRepository
        return vaccinationRepository.findByDossierMedical(dossier);
    }

    // Documents médicaux
    @Transactional
    // Méthode `addDocument` (publique) — paramètres : `patientId` (identifiant UUID), `document` (DocumentMedical) ; retourne : DocumentMedical ; intention : ajoute (add document)
    public DocumentMedical addDocument(UUID patientId, DocumentMedical document) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getOrCreateDossier(patientId)`
        DossierMedical dossier = getOrCreateDossier(patientId);
        // Renseigne la propriété DossierMedical de `document` avec la valeur de dossier
        document.setDossierMedical(dossier);
        // Déclare la variable `saved` (DocumentMedical) initialisée avec l'enregistrement en base de document via documentMedicalRepository
        DocumentMedical saved = documentMedicalRepository.save(document);

        // Condition : exécute le bloc suivant seulement si `realtimePublisher != null`
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/dossier", "DOCUMENT_AJOUTE", Map.of(
                realtimePublisher.publish("/topic/dossier", "DOCUMENT_AJOUTE", Map.of(
                    // Paire clé/valeur : clé « type » associée à le texte "DOCUMENT"
                    "type", "DOCUMENT",
                    // Paire clé/valeur : clé « patientId » associée à `patientId.toString(`
                    "patientId", patientId.toString(),
                    // Paire clé/valeur : clé « nomFichier » associée à `saved.getTitre() != null ? saved.getTitre() : "Document"`
                    "nomFichier", saved.getTitre() != null ? saved.getTitre() : "Document",
                    // Paire clé/valeur : clé « date » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "date", LocalDateTime.now().toString()
                ));
                // Appelle la méthode `publish` sur `realtimePublisher` : realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.…
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Paire clé/valeur : clé « userId » associée à `patientId.toString(`
                    "userId", patientId.toString(),
                    // Paire clé/valeur : clé « type » associée à le texte "DOSSIER_MIS_A_JOUR"
                    "type", "DOSSIER_MIS_A_JOUR",
                    // Paire clé/valeur : clé « titre » associée à le texte "Nouveau document médical"
                    "titre", "Nouveau document médical",
                    // Paire clé/valeur : clé « corps » associée à le texte "Le médecin a ajouté un document à votre dossier médical."
                    "corps", "Le médecin a ajouté un document à votre dossier médical.",
                    // Paire clé/valeur : clé « message » associée à le texte "Le médecin a ajouté un document à votre dossier médical."
                    "message", "Le médecin a ajouté un document à votre dossier médical.",
                    // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Paire clé/valeur : clé « lue » associée à le booléen faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {}
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getDocuments` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de DocumentMedical ; intention : récupère (get documents)
    public List<DocumentMedical> getDocuments(UUID patientId) {
        // Déclare la variable `dossier` (DossierMedical) initialisée avec `getByPatientId(patientId)`
        DossierMedical dossier = getByPatientId(patientId);
        // Retourne le résultat de la requête findByDossierMedical exécutée via documentMedicalRepository
        return documentMedicalRepository.findByDossierMedical(dossier);
    }
}
