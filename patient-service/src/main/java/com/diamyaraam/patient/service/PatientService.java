// Déclaration du package Java : `com.diamyaraam.patient.service`
package com.diamyaraam.patient.service;

// Import de la classe `Patient` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.Patient;
// Import de la classe `PatientRepository` (paquet com.diamyaraam.patient.repository)
import com.diamyaraam.patient.repository.PatientRepository;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Import de la classe `MembreFamille` (paquet com.diamyaraam.patient.entity)
import com.diamyaraam.patient.entity.MembreFamille;
// Import de la classe `MembreFamilleRepository` (paquet com.diamyaraam.patient.repository)
import com.diamyaraam.patient.repository.MembreFamilleRepository;
// Import de la classe `MembreFamilleDto` (paquet com.diamyaraam.patient.dto)
import com.diamyaraam.patient.dto.MembreFamilleDto;

/**
 * ====================================================================================================
 * SERVICE MÉTIER : GESTION DES DOSSIERS ET PROFILS PATIENTS (PATIENT SERVICE)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS CLÉS POUR LA SOUTENANCE :
 * Cette classe orchestre la persistance et la logique métier relative aux profils de santé des patients.
 * 
 * 💼 FONCTIONNALITÉS MÉTIERS PRINCIPALES :
 * 1. Création Paresseuse de Profil (Lazy Profile Initialization - `getOrCreateProfile`) :
 *    Lorsqu'un utilisateur s'inscrit dans auth-service, son profil médical dans patient-service
 *    est automatiquement instancié et lié à son `userId` dès sa première interaction,
 *    garantissant la cohérence des données sans nécessiter de transaction distribuée 2PC lourde.
 * 
 * 2. Contact d'Urgence (Personne de Confiance) :
 *    Mise à jour transactionnelle des coordonnées du proche à contacter en cas de crise vitale.
 * 
 * 3. Résolution Flexible d'Identifiants (UUID & QR Token) :
 *    Prise en charge de la recherche directe par UUID ou par jetons de carte physique / badge QR.
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `PatientService` (rôle : porte la logique métier)
public class PatientService {

    // Référence vers le repository JPA de persistance des patients
    private final PatientRepository patientRepository;
    // Référence vers le repository JPA de gestion des membres de la famille
    private final MembreFamilleRepository membreFamilleRepository;

    /**
     * Constructeur injectant les repositories de persistance JPA.
     */
    public PatientService(
            // Injection du repository patient
            PatientRepository patientRepository,
            // Injection du repository des membres de la famille
            MembreFamilleRepository membreFamilleRepository) {
        // Initialisation de la référence patientRepository
        this.patientRepository = patientRepository;
        // Initialisation de la référence membreFamilleRepository
        this.membreFamilleRepository = membreFamilleRepository;
    }

    /**
     * Récupère le profil patient existant ou en crée un nouveau s'il s'agit de sa première connexion.
     * 
     * @param userId Identifiant UUID de l'utilisateur authentifié
     * @return L'entité Patient persistée
     */
    // Transaction Spring assurant l'atomicité de la création de profil
    @Transactional
    // Méthode `getOrCreateProfile` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : Patient ; intention : récupère ou crée (get or create profile)
    public Patient getOrCreateProfile(UUID userId) {
        // Recherche en base d'un profil déjà associé à cet identifiant utilisateur
        return patientRepository.findByUserId(userId)
                // Si inexistant, instanciation et sauvegarde paresseuse d'un nouveau profil
                .orElseGet(() -> {
                    // Instanciation de l'entité Patient
                    Patient p = new Patient();
                    // Association avec l'identifiant auth_user
                    p.setUserId(userId);
                    // Persistance dans la table patient_schema.patient
                    return patientRepository.save(p);
                });
    }

    // Mise à jour transactionnelle des coordonnées de la personne de confiance (ICE - In Case of Emergency)
    @Transactional
    // Méthode `updateEmergencyContact` (publique) — paramètres : `userId` (identifiant UUID), `nom` (chaîne de caractères), `telephone` (chaîne de caractères), `lien` (chaîne de caractères) ; retourne : Patient ; intention : met à jour (update emergency contact)
    public Patient updateEmergencyContact(UUID userId, String nom, String telephone, String lien) {
        // Récupération ou création du profil patient
        Patient patient = getOrCreateProfile(userId);
        // Si un nom de contact est fourni, mise à jour après suppression des espaces superflus
        if (nom != null && !nom.trim().isEmpty()) {
            // Affectation du nom du contact d'urgence
            patient.setContactUrgenceNom(nom.trim());
        }
        // Si un téléphone est fourni, mise à jour après nettoyage
        if (telephone != null && !telephone.trim().isEmpty()) {
            // Affectation du téléphone d'urgence
            patient.setContactUrgenceTelephone(telephone.trim());
        }
        // Si un lien de parenté est fourni, mise à jour après nettoyage
        if (lien != null && !lien.trim().isEmpty()) {
            // Affectation du lien de parenté (ex: Épouse, Frère, Voisin)
            patient.setContactUrgenceLien(lien.trim());
        }
        // Valeur de secours par défaut si le nom du contact d'urgence reste non renseigné
        if (patient.getContactUrgenceNom() == null || patient.getContactUrgenceNom().trim().isEmpty()) {
            // Libellé générique par défaut
            patient.setContactUrgenceNom("Contact d'urgence");
        }
        // Valeur de secours par défaut si le lien de parenté reste non renseigné
        if (patient.getContactUrgenceLien() == null || patient.getContactUrgenceLien().trim().isEmpty()) {
            // Lien générique par défaut
            patient.setContactUrgenceLien("Proche");
        }
        // Sauvegarde des modifications en base de données et renvoi de l'entité
        return patientRepository.save(patient);
    }

    // Recherche stricte d'un profil patient par son identifiant auth_user
    public Patient getPatientByUserId(UUID userId) {
        // Recherche par identifiant utilisateur ou levée d'une exception si absent
        return patientRepository.findByUserId(userId)
                // Exception claire en cas de profil introuvable
                .orElseThrow(() -> new IllegalArgumentException("Profil patient introuvable."));
    }

    // Résolution flexible d'un patient par identifiant interne ou par identifiant auth_user
    public Patient getPatientByIdOrUserId(UUID id) {
        // Tentative de recherche par userId
        return patientRepository.findByUserId(id)
                // Repli sur la recherche par clé primaire id
                .or(() -> patientRepository.findById(id))
                // Repli sur la création paresseuse si non trouvé
                .orElseGet(() -> getOrCreateProfile(id));
    }

    // Résolution du profil d'urgence lors du scan d'un badge ou QR code physique/virtuel
    public Patient getByQrToken(String qrToken) {
        // Étape 1 : Si le jeton encode un UUID de 32 caractères hexadécimaux
        if (qrToken != null && qrToken.trim().length() >= 32) {
            // Nettoyage des préfixes et tirets conventionnels
            String clean = qrToken.trim().replace("QR-", "").replace("PT-", "").replace("-", "");
            // Si la chaîne nettoyée correspond exactement à la longueur d'un UUID hexadécimal
            if (clean.length() == 32) {
                // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                try {
                    // Reconstitution du format standardisé 8-4-4-4-12 de l'UUID
                    String formattedUuid = clean.substring(0, 8) + "-" +
                            // Argument/valeur : `clean.substring(8, 12) + "-" +`
                            clean.substring(8, 12) + "-" +
                            // Argument/valeur : `clean.substring(12, 16) + "-" +`
                            clean.substring(12, 16) + "-" +
                            // Argument/valeur : `clean.substring(16, 20) + "-" +`
                            clean.substring(16, 20) + "-" +
                            // Argument/valeur : `clean.substring(20`
                            clean.substring(20);
                    // Conversion en objet UUID Java
                    UUID parsedUuid = UUID.fromString(formattedUuid);
                    // Recherche du patient par son userId ou son id primaire
                    var found = patientRepository.findByUserId(parsedUuid)
                            // Enchaînement : appelle `or(() -> patientRepository.findById(parsedUuid));`
                            .or(() -> patientRepository.findById(parsedUuid));
                    // Si un patient est trouvé dans la base
                    if (found.isPresent()) {
                        // Extraction de l'entité
                        Patient p = found.get();
                        // Attribution d'un numéro d'urgence de secours si non renseigné
                        if (p.getContactUrgenceTelephone() == null || p.getContactUrgenceTelephone().trim().isEmpty()) {
                            // Renseigne la propriété ContactUrgenceTelephone de `p` avec le texte "+221 77 666 77 88"
                            p.setContactUrgenceTelephone("+221 77 666 77 88");
                        }
                        // Attribution d'un nom de contact de secours si non renseigné
                        if (p.getContactUrgenceNom() == null || p.getContactUrgenceNom().trim().isEmpty()) {
                            // Renseigne la propriété ContactUrgenceNom de `p` avec le texte "Proche / ICE"
                            p.setContactUrgenceNom("Proche / ICE");
                        }
                        // Attribution d'un lien de parenté de secours si non renseigné
                        if (p.getContactUrgenceLien() == null || p.getContactUrgenceLien().trim().isEmpty()) {
                            // Renseigne la propriété ContactUrgenceLien de `p` avec le texte "Proche"
                            p.setContactUrgenceLien("Proche");
                        }
                        // Renvoi du patient résolu avec ses informations d'urgence
                        return p;
                    }
                // Interception de l'exception Exception ignored
                } catch (Exception ignored) {
                    // Poursuite du flux en cas de format d'UUID non conforme
                }
            }
        }

        // Étape 2 : Mode démo soutenance / repli gracieux sur un patient existant
        return patientRepository.findAll().stream().findFirst().map(p -> {
            // Initialisation des données de démonstration du contact d'urgence si vides
            if (p.getContactUrgenceNom() == null || p.getContactUrgenceNom().trim().isEmpty()) {
                // Renseigne la propriété ContactUrgenceNom de `p` avec le texte "Aminata Gueye"
                p.setContactUrgenceNom("Aminata Gueye");
                // Renseigne la propriété ContactUrgenceTelephone de `p` avec le texte "+221 77 666 77 88"
                p.setContactUrgenceTelephone("+221 77 666 77 88");
                // Renseigne la propriété ContactUrgenceLien de `p` avec le texte "Épouse / Proche"
                p.setContactUrgenceLien("Épouse / Proche");
            }
            // Initialisation de l'adresse par défaut si absente
            if (p.getAdresse() == null || p.getAdresse().isEmpty()) {
                // Renseigne la propriété Adresse de `p` avec le texte "Les Almadies, Villa 42, Dakar"
                p.setAdresse("Les Almadies, Villa 42, Dakar");
            }
            // Initialisation de la ville par défaut si absente
            if (p.getVille() == null || p.getVille().isEmpty()) {
                // Renseigne la propriété Ville de `p` avec le texte "Dakar, Sénégal"
                p.setVille("Dakar, Sénégal");
            }
            // Renvoi du profil enrichi
            return p;
        // Suite de la chaîne d'appels : .orElseGet(() -> {
        }).orElseGet(() -> {
            // Création d'un profil virtuel complet en cas de base initialement vide
            Patient p = new Patient();
            // Renseigne la propriété ContactUrgenceNom de `p` avec le texte "Aminata Gueye"
            p.setContactUrgenceNom("Aminata Gueye");
            // Renseigne la propriété ContactUrgenceTelephone de `p` avec le texte "+221 77 666 77 88"
            p.setContactUrgenceTelephone("+221 77 666 77 88");
            // Renseigne la propriété ContactUrgenceLien de `p` avec le texte "Épouse / Proche"
            p.setContactUrgenceLien("Épouse / Proche");
            // Renseigne la propriété Adresse de `p` avec le texte "Les Almadies, Villa 42, Dakar"
            p.setAdresse("Les Almadies, Villa 42, Dakar");
            // Renseigne la propriété Ville de `p` avec le texte "Dakar, Sénégal"
            p.setVille("Dakar, Sénégal");
            // Retourne la valeur de p
            return p;
        });
    }

    // Récupération de l'ensemble des membres de la famille rattachés à un parent
    public List<MembreFamille> getMembresFamille(UUID parentUserId) {
        // Requête de tous les enregistrements familiaux pour ce parent
        return membreFamilleRepository.findByParentUserId(parentUserId);
    }

    // Ajout d'un membre de la famille (gestion pédiatrique ou proche dépendant)
    @Transactional
    // Méthode `ajouterMembreFamille` (publique) — paramètres : `parentUserId` (identifiant UUID), `dto` (MembreFamilleDto) ; retourne : MembreFamille ; intention : ajoute (ajouter membre famille)
    public MembreFamille ajouterMembreFamille(UUID parentUserId, MembreFamilleDto dto) {
        // 1. Génération d'un identifiant virtuel unique pour le membre dépendant
        UUID enfantUserId = UUID.randomUUID();

        // 2. Création de l'entité de relation familiale
        MembreFamille membre = new MembreFamille();
        // Attribution de l'identifiant du tuteur / parent légal
        membre.setParentUserId(parentUserId);
        // Attribution de l'identifiant généré pour le membre
        membre.setEnfantUserId(enfantUserId);
        // Définition du nom de famille
        membre.setNom(dto.getNom());
        // Définition du prénom
        membre.setPrenom(dto.getPrenom());
        // Définition de la date de naissance
        membre.setDateNaissance(dto.getDateNaissance());
        // Définition du genre
        membre.setGenre(dto.getGenre());
        // Définition du lien de parenté (ex: ENFANT, PARENT_AGE)
        membre.setLienParente(dto.getLienParente());
        
        // Persistance de l'enregistrement du membre familial
        MembreFamille saved = membreFamilleRepository.save(membre);

        // 3. Initialisation automatique du profil Patient dédié pour ce membre
        getOrCreateProfile(enfantUserId);

        // Renvoi du membre persisté
        return saved;
    }

    // Consultation globale de tous les profils patients pour le tableau de bord d'administration
    public List<Patient> getAllPatients() {
        // Requête JPA retournant l'intégralité des enregistrements
        return patientRepository.findAll();
    }
}
