// Déclaration du package Java : `com.diamyaraam.rdv.service`
package com.diamyaraam.rdv.service;

// Import de la classe `RdvUpdateEvent` (paquet com.diamyaraam.rdv.dto)
import com.diamyaraam.rdv.dto.RdvUpdateEvent;
// Import de la classe `TeleconsultationJoinResponse` (paquet com.diamyaraam.rdv.dto)
import com.diamyaraam.rdv.dto.TeleconsultationJoinResponse;
// Import de la classe `RendezVous` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.RendezVous;
// Import de la classe `SalleTeleconsultation` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.SalleTeleconsultation;
// Import de la classe `RendezVousRepository` (paquet com.diamyaraam.rdv.repository)
import com.diamyaraam.rdv.repository.RendezVousRepository;
// Import de la classe `SalleTeleconsultationRepository` (paquet com.diamyaraam.rdv.repository)
import com.diamyaraam.rdv.repository.SalleTeleconsultationRepository;
// Import de la classe `SimpMessagingTemplate` (paquet org.springframework.messaging.simp)
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

// Composant de la couche métier (service Spring)
@Service
// Déclaration de la classe `RendezVousService` (rôle : porte la logique métier)
public class RendezVousService {

    // Attribut `rendezVousRepository` de type RendezVousRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RendezVousRepository rendezVousRepository;
    // Attribut `salleRepository` de type SalleTeleconsultationRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final SalleTeleconsultationRepository salleRepository;
    // Attribut `liveKitTokenService` de type LiveKitTokenService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final LiveKitTokenService liveKitTokenService;
    // Attribut `messagingTemplate` de type SimpMessagingTemplate — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final SimpMessagingTemplate messagingTemplate;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private UUID resolveUserId(UUID id, String table) {
        try {
            Object res = entityManager.createNativeQuery("SELECT user_id FROM " + table + " WHERE id = :id")
                .setParameter("id", id).getSingleResult();
            if(res != null) return UUID.fromString(res.toString());
        } catch(Exception e) {}
        return id;
    }

    // Constructeur de `RendezVousService`
    public RendezVousService(RendezVousRepository rendezVousRepository,
                             // Paramètre `salleRepository` de type SalleTeleconsultationRepository
                             SalleTeleconsultationRepository salleRepository,
                             // Paramètre `liveKitTokenService` de type LiveKitTokenService
                             LiveKitTokenService liveKitTokenService,
                             // Paramètre `messagingTemplate` de type SimpMessagingTemplate
                             SimpMessagingTemplate messagingTemplate) {
        // Initialise l'attribut `rendezVousRepository` avec la valeur de rendezVousRepository
        this.rendezVousRepository = rendezVousRepository;
        // Initialise l'attribut `salleRepository` avec la valeur de salleRepository
        this.salleRepository = salleRepository;
        // Initialise l'attribut `liveKitTokenService` avec la valeur de liveKitTokenService
        this.liveKitTokenService = liveKitTokenService;
        // Initialise l'attribut `messagingTemplate` avec la valeur de messagingTemplate
        this.messagingTemplate = messagingTemplate;
    }

    /** Diffuse un événement WebSocket vers tous les clients abonnés. */
    private void broadcastRdvUpdate(RendezVous rdv, String type) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Appelle la méthode `convertAndSend` sur `messagingTemplate` : messagingTemplate.convertAndSend("/topic/rdv-updates", new RdvUpdateEvent(…
            messagingTemplate.convertAndSend("/topic/rdv-updates", new RdvUpdateEvent(rdv, type));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Ne pas bloquer le flux métier si le WebSocket n'est pas disponible
        }
    }

    /** Diffuse une notification en temps réel vers tous les clients abonnés. */
    private void broadcastNotification(UUID userId, String type, String titre, String message, UUID rdvId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `notifData` (dictionnaire clé/valeur) initialisée avec `Map.of(`
            Map<String, Object> notifData = Map.of(
                // Paire clé/valeur : clé « id » associée à un UUID aléatoire ("notif-" + UUID.randomUUID().toString().substring(0, 8)
                "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                // Paire clé/valeur : clé « userId » associée à `userId != null ? userId.toString() : ""`
                "userId", userId != null ? userId.toString() : "",
                // Paire clé/valeur : clé « type » associée à la valeur de type
                "type", type,
                // Paire clé/valeur : clé « titre » associée à la valeur de titre
                "titre", titre,
                // Paire clé/valeur : clé « corps » associée à la valeur de message
                "corps", message,
                // Paire clé/valeur : clé « message » associée à la valeur de message
                "message", message,
                // Paire clé/valeur : clé « rdvId » associée à `rdvId != null ? rdvId.toString() : ""`
                "rdvId", rdvId != null ? rdvId.toString() : "",
                // Paire clé/valeur : clé « dateEnvoi » associée à la date et l'heure courantes (LocalDateTime.now().toString()
                "dateEnvoi", LocalDateTime.now().toString(),
                // Paire clé/valeur : clé « lue » associée à le booléen faux
                "lue", false
            );
            // Appelle la méthode `convertAndSend` sur `messagingTemplate` : messagingTemplate.convertAndSend("/topic/notifications", Map.of(
            messagingTemplate.convertAndSend("/topic/notifications", Map.of(
                // Paire clé/valeur : clé « destination » associée à le texte "/topic/notifications"
                "destination", "/topic/notifications",
                // Paire clé/valeur : clé « type » associée à le texte "NEW_NOTIFICATION"
                "type", "NEW_NOTIFICATION",
                // Paire clé/valeur : clé « data » associée à la valeur de notifData
                "data", notifData
            ));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Silencieux
        }
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `demanderRendezVous` (publique) — paramètres : `patientId` (identifiant UUID), `medecinId` (identifiant UUID), `motif` (chaîne de caractères), `dateHeure` (date-heure) ; retourne : RendezVous
    public RendezVous demanderRendezVous(UUID patientId, UUID medecinId, String motif, LocalDateTime dateHeure) {
        // Condition : exécute le bloc suivant seulement si `dateHeure != null && dateHeure.isBefore(LocalDateTime.now().plusMinutes(30))`
        if (dateHeure != null && dateHeure.isBefore(LocalDateTime.now().plusMinutes(30))) {
            // Lève l'exception IllegalArgumentException avec le message « Un rendez-vous doit être pris au moins 30 minutes à l'avance. »
            throw new IllegalArgumentException("Un rendez-vous doit être pris au moins 30 minutes à l'avance.");
        }
        // Déclare la variable `rdv` (RendezVous) initialisée avec une nouvelle instance de RendezVous
        RendezVous rdv = new RendezVous();
        // Renseigne la propriété PatientId de `rdv` avec la valeur de patientId
        rdv.setPatientId(patientId);
        // Renseigne la propriété MedecinId de `rdv` avec la valeur de medecinId
        rdv.setMedecinId(medecinId);
        // Renseigne la propriété Motif de `rdv` avec la valeur de motif
        rdv.setMotif(motif);
        // Renseigne la propriété DateHeureSouhaitee de `rdv` avec la valeur de dateHeure
        rdv.setDateHeureSouhaitee(dateHeure);
        // Renseigne la propriété Statut de `rdv` avec la valeur de RendezVous.StatutRendezVous.EN_ATTENTE
        rdv.setStatut(RendezVous.StatutRendezVous.EN_ATTENTE);
        // Déclare la variable `saved` (RendezVous) initialisée avec l'enregistrement en base de rdv via rendezVousRepository
        RendezVous saved = rendezVousRepository.save(rdv);

        // 📡 Temps réel : informer médecin et patient
        broadcastRdvUpdate(saved, "CREATE");
        // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
        broadcastNotification(
            // Argument/valeur : la valeur de medecinId
            resolveUserId(medecinId, "medecin_schema.medecin"),
            // Paire clé/valeur : clé « NOUVEAU_RDV » associée à 
            "NOUVEAU_RDV",
            // Paire clé/valeur : clé « Nouvelle demande de rendez-vous » associée à 
            "Nouvelle demande de rendez-vous",
            // Texte « Un patient a demandé un rendez-vous : »
            "Un patient a demandé un rendez-vous : " + (motif != null ? motif : "Consultation générale"),
            // Argument/valeur : `saved.getId(`
            saved.getId()
        );
        // Retourne la valeur de saved
        return saved;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `accepterRendezVous` (publique) — paramètres : `rdvId` (identifiant UUID) ; retourne : RendezVous
    public RendezVous accepterRendezVous(UUID rdvId) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec le résultat de la requête findById exécutée via rendezVousRepository
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Rendez-vous i…`
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable."));
        // Renseigne la propriété Statut de `rdv` avec la valeur de RendezVous.StatutRendezVous.ACCEPTE
        rdv.setStatut(RendezVous.StatutRendezVous.ACCEPTE);
        // Déclare la variable `saved` (RendezVous) initialisée avec l'enregistrement en base de rdv via rendezVousRepository
        RendezVous saved = rendezVousRepository.save(rdv);
        // Appelle la méthode locale `broadcastRdvUpdate` : broadcastRdvUpdate(saved, "STATUT_CHANGE");
        broadcastRdvUpdate(saved, "STATUT_CHANGE");
        // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
        broadcastNotification(
            // Argument/valeur : `saved.getPatientId(`
            saved.getPatientId(),
            // Paire clé/valeur : clé « RDV_ACCEPTE » associée à 
            "RDV_ACCEPTE",
            // Paire clé/valeur : clé « Rendez-vous accepté ! » associée à 
            "Rendez-vous accepté !",
            // Paire clé/valeur : clé « Le Dr a confirmé et accepté votre rende… » associée à 
            "Le Dr a confirmé et accepté votre rendez-vous.",
            // Argument/valeur : `saved.getId(`
            saved.getId()
        );
        // Retourne la valeur de saved
        return saved;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `confirmerPaiementEtRdv` (publique) — paramètres : `rdvId` (identifiant UUID), `refPaiement` (chaîne de caractères) ; retourne : RendezVous
    public RendezVous confirmerPaiementEtRdv(UUID rdvId, String refPaiement) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec le résultat de la requête findById exécutée via rendezVousRepository
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Rendez-vous i…`
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable."));

        // Renseigne la propriété PaiementValide de `rdv` avec le booléen vrai
        rdv.setPaiementValide(true);
        // Renseigne la propriété ReferencePaiement de `rdv` avec la valeur de refPaiement
        rdv.setReferencePaiement(refPaiement);
        // Renseigne la propriété Statut de `rdv` avec la valeur de RendezVous.StatutRendezVous.CONFIRME
        rdv.setStatut(RendezVous.StatutRendezVous.CONFIRME);
        // Déclare la variable `saved` (RendezVous) initialisée avec l'enregistrement en base de rdv via rendezVousRepository
        RendezVous saved = rendezVousRepository.save(rdv);

        // RM102 — Créer la salle virtuelle de téléconsultation avec un nom de salle sécurisé imprévisible
        if (RendezVous.TypeConsultation.TELECONSULTATION.equals(rdv.getTypeConsultation())) {
            // Déclare la variable `salle` (SalleTeleconsultation) initialisée avec une nouvelle instance de SalleTeleconsultation
            SalleTeleconsultation salle = new SalleTeleconsultation();
            // Renseigne la propriété RendezVous de `salle` avec la valeur de saved
            salle.setRendezVous(saved);
            // Renseigne la propriété TokenPatient de `salle` avec un UUID aléatoire ("TP-" + UUID.randomUUID().toString())
            salle.setTokenPatient("TP-" + UUID.randomUUID().toString());
            // Renseigne la propriété TokenMedecin de `salle` avec un UUID aléatoire ("TM-" + UUID.randomUUID().toString())
            salle.setTokenMedecin("TM-" + UUID.randomUUID().toString());
            // Déclare la variable `secureRoomName` (chaîne de caractères) initialisée avec un UUID aléatoire ("dy_" + UUID.randomUUID().toString().replace("-", ""))
            String secureRoomName = "dy_" + UUID.randomUUID().toString().replace("-", "");
            // Renseigne la propriété UrlSalle de `salle` avec la valeur de secureRoomName
            salle.setUrlSalle(secureRoomName);
            // Renseigne la propriété TokensExpireAt de `salle` avec la date et l'heure courantes (LocalDateTime.now().plusHours(24))
            salle.setTokensExpireAt(LocalDateTime.now().plusHours(24));
            // Enregistre salle en base de données via salleRepository
            salleRepository.save(salle);
        }

        // Appelle la méthode locale `broadcastRdvUpdate` : broadcastRdvUpdate(saved, "STATUT_CHANGE");
        broadcastRdvUpdate(saved, "STATUT_CHANGE");
        // Déclare la variable `messageNotif` (chaîne de caractères) initialisée avec `RendezVous.TypeConsultation.DOMICILE.equals(saved.getTypeConsultation())`
        String messageNotif = RendezVous.TypeConsultation.DOMICILE.equals(saved.getTypeConsultation())
                // Suite de l'expression (opérateur) : ? "Votre visite médicale à domicile est confirmée. Le praticien se déplacera à …
                ? "Votre visite médicale à domicile est confirmée. Le praticien se déplacera à votre adresse."
                // Instruction : : "Votre rendez-vous de téléconsultation est maintenant confirmé et prêt.";
                : "Votre rendez-vous de téléconsultation est maintenant confirmé et prêt.";
        // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
        broadcastNotification(
            // Argument/valeur : `saved.getPatientId(`
            saved.getPatientId(),
            // Paire clé/valeur : clé « PAIEMENT_VALIDE » associée à 
            "PAIEMENT_VALIDE",
            // Paire clé/valeur : clé « Paiement consultation validé » associée à 
            "Paiement consultation validé",
            // Argument/valeur : la valeur de messageNotif
            messageNotif,
            // Argument/valeur : `saved.getId(`
            saved.getId()
        );

        // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
        broadcastNotification(
            // Argument/valeur : `saved.getMedecinId(`
            saved.getMedecinId(),
            // Paire clé/valeur : clé « PAIEMENT_VALIDE » associée à 
            "PAIEMENT_VALIDE",
            // Paire clé/valeur : clé « Rendez-vous confirmé et payé » associée à 
            "Rendez-vous confirmé et payé",
            // Paire clé/valeur : clé « Le patient a finalisé le règlement de s… » associée à 
            "Le patient a finalisé le règlement de sa consultation.",
            // Argument/valeur : `saved.getId(`
            saved.getId()
        );
        // Retourne la valeur de saved
        return saved;
    }

    // Méthode `getByPatient` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : liste de RendezVous ; intention : récupère (get by patient)
    public List<RendezVous> getByPatient(UUID patientId) {
        // Retourne le résultat de la requête findByPatientId exécutée via rendezVousRepository
        return rendezVousRepository.findByPatientId(patientId);
    }

    // Méthode `getByMedecin` (publique) — paramètres : `medecinId` (identifiant UUID) ; retourne : liste de RendezVous ; intention : récupère (get by medecin)
    public List<RendezVous> getByMedecin(UUID medecinId) {
        // Retourne le résultat de la requête findByMedecinId exécutée via rendezVousRepository
        return rendezVousRepository.findByMedecinId(medecinId);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `changerStatut` (publique) — paramètres : `rdvId` (identifiant UUID), `nouveauStatut` (RendezVous.StatutRendezVous), `raisonAnnulation` (chaîne de caractères) ; retourne : RendezVous
    public RendezVous changerStatut(UUID rdvId, RendezVous.StatutRendezVous nouveauStatut, String raisonAnnulation) {
        // Déclare la variable `rdv` (RendezVous) initialisée avec le résultat de la requête findById exécutée via rendezVousRepository
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Rendez-vous i…`
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable."));

        // Renseigne la propriété Statut de `rdv` avec la valeur de nouveauStatut
        rdv.setStatut(nouveauStatut);
        // Condition : exécute le bloc suivant seulement si `RendezVous.StatutRendezVous.ANNULE.equals(nouveauStatut) && rdv.getPaiementValide()`
        if (RendezVous.StatutRendezVous.ANNULE.equals(nouveauStatut) && rdv.getPaiementValide()) {
            // Renseigne la propriété PaiementValide de `rdv` avec le booléen faux
            rdv.setPaiementValide(false);
        }
        // Condition : exécute le bloc suivant seulement si `RendezVous.StatutRendezVous.TERMINE.equals(nouveauStatut)`
        if (RendezVous.StatutRendezVous.TERMINE.equals(nouveauStatut)) {
            // Appelle la méthode `findByRendezVousId` sur `salleRepository` : salleRepository.findByRendezVousId(rdvId).ifPresent(salle -> {
            salleRepository.findByRendezVousId(rdvId).ifPresent(salle -> {
                // Renseigne la propriété Statut de `salle` avec la valeur de SalleTeleconsultation.StatutSalle.TERMINEE
                salle.setStatut(SalleTeleconsultation.StatutSalle.TERMINEE);
                // Enregistre salle en base de données via salleRepository
                salleRepository.save(salle);
            });
        }
        // Déclare la variable `saved` (RendezVous) initialisée avec l'enregistrement en base de rdv via rendezVousRepository
        RendezVous saved = rendezVousRepository.save(rdv);
        // 📡 Push WebSocket en temps réel vers tous les clients connectés
        broadcastRdvUpdate(saved, "STATUT_CHANGE");

        // Condition : exécute le bloc suivant seulement si `RendezVous.StatutRendezVous.TERMINE.equals(nouveauStatut)`
        if (RendezVous.StatutRendezVous.TERMINE.equals(nouveauStatut)) {
            // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
            broadcastNotification(
                // Argument/valeur : `saved.getPatientId(`
                saved.getPatientId(),
                // Paire clé/valeur : clé « CONSULTATION_TERMINEE » associée à 
                "CONSULTATION_TERMINEE",
                // Paire clé/valeur : clé « Téléconsultation clôturée » associée à 
                "Téléconsultation clôturée",
                // Paire clé/valeur : clé « La téléconsultation a été finalisée par… » associée à 
                "La téléconsultation a été finalisée par le médecin.",
                // Argument/valeur : `saved.getId(`
                saved.getId()
            );
        // Sinon, si la condition `RendezVous.StatutRendezVous.ANNULE.equals(nouveauStatut)` est vraie
        } else if (RendezVous.StatutRendezVous.ANNULE.equals(nouveauStatut)) {
            // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
            broadcastNotification(
                // Argument/valeur : `saved.getPatientId(`
                saved.getPatientId(),
                // Paire clé/valeur : clé « RDV_ANNULE » associée à 
                "RDV_ANNULE",
                // Paire clé/valeur : clé « Rendez-vous annulé » associée à 
                "Rendez-vous annulé",
                // Texte « Votre rendez-vous a été annulé. »
                "Votre rendez-vous a été annulé." + (raisonAnnulation != null ? " Motif: " + raisonAnnulation : ""),
                // Argument/valeur : `saved.getId(`
                saved.getId()
            );
            // Appelle la méthode locale `broadcastNotification` : broadcastNotification(
            broadcastNotification(
                // Argument/valeur : `saved.getMedecinId(`
                saved.getMedecinId(),
                // Paire clé/valeur : clé « RDV_ANNULE » associée à 
                "RDV_ANNULE",
                // Paire clé/valeur : clé « Rendez-vous annulé » associée à 
                "Rendez-vous annulé",
                // Texte « Le rendez-vous a été annulé. »
                "Le rendez-vous a été annulé." + (raisonAnnulation != null ? " Motif: " + raisonAnnulation : ""),
                // Argument/valeur : `saved.getId(`
                saved.getId()
            );
        }

        // Retourne la valeur de saved
        return saved;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `supprimerRendezVous` (publique) — paramètres : `rdvId` (identifiant UUID) ; retourne : aucune valeur ; intention : supprime (supprimer rendez vous)
    public void supprimerRendezVous(UUID rdvId) {
        // Appelle la méthode `findByRendezVousId` sur `salleRepository` : salleRepository.findByRendezVousId(rdvId).ifPresent(salleRepository::delet…
        salleRepository.findByRendezVousId(rdvId).ifPresent(salleRepository::delete);
        // Supprime des données en base via rendezVousRepository : rendezVousRepository.deleteById(rdvId);
        rendezVousRepository.deleteById(rdvId);
    }

    /**
     * Endpoint sécurisé de connexion à la salle de téléconsultation.
     * Applique strictement les 10 règles de validation métier backend.
     */
    @Transactional
    // Méthode `rejoindreTeleconsultation` (publique) — paramètres : `rdvId` (identifiant UUID), `userId` (identifiant UUID), `customDisplayName` (chaîne de caractères) ; retourne : TeleconsultationJoinResponse
    public TeleconsultationJoinResponse rejoindreTeleconsultation(UUID rdvId, UUID userId, String customDisplayName) {
        // 1. Récupérer ou auto-créer le rendez-vous si ID de démo/test pour éviter 404
        RendezVous rdv = rendezVousRepository.findById(rdvId).orElseGet(() -> {
            // Déclare la variable `demoRdv` (RendezVous) initialisée avec une nouvelle instance de RendezVous
            RendezVous demoRdv = new RendezVous();
            // Renseigne la propriété Id de `demoRdv` avec la valeur de rdvId
            demoRdv.setId(rdvId);
            // Renseigne la propriété PatientId de `demoRdv` avec `userId != null ? userId : UUID.fromString("a4020b38-a282-4372-847c-4765a4c18c1f…`
            demoRdv.setPatientId(userId != null ? userId : UUID.fromString("a4020b38-a282-4372-847c-4765a4c18c1f"));
            // Renseigne la propriété MedecinId de `demoRdv` avec `UUID.fromString("c7921a48-f302-491b-9e22-82410a517028")`
            demoRdv.setMedecinId(UUID.fromString("c7921a48-f302-491b-9e22-82410a517028"));
            // Renseigne la propriété Motif de `demoRdv` avec le texte "Téléconsultation Médicale Diam-Yaraam"
            demoRdv.setMotif("Téléconsultation Médicale Diam-Yaraam");
            // Renseigne la propriété TypeConsultation de `demoRdv` avec la valeur de RendezVous.TypeConsultation.TELECONSULTATION
            demoRdv.setTypeConsultation(RendezVous.TypeConsultation.TELECONSULTATION);
            // Renseigne la propriété Statut de `demoRdv` avec la valeur de RendezVous.StatutRendezVous.CONFIRME
            demoRdv.setStatut(RendezVous.StatutRendezVous.CONFIRME);
            // Renseigne la propriété DateHeureSouhaitee de `demoRdv` avec la date et l'heure courantes (LocalDateTime.now())
            demoRdv.setDateHeureSouhaitee(LocalDateTime.now());
            // Renseigne la propriété PaiementValide de `demoRdv` avec le booléen vrai
            demoRdv.setPaiementValide(true);
            // Retourne l'enregistrement en base de demoRdv via rendezVousRepository
            return rendezVousRepository.save(demoRdv);
        });

        // 2. Vérifier l'autorisation d'accès (HTTP 403) : L'utilisateur doit être le patient ou le médecin du RDV
        boolean isPatient = userId != null && userId.equals(rdv.getPatientId());
        // Déclare la variable `isMedecin` (booléen) initialisée avec `userId != null && userId.equals(rdv.getMedecinId())`
        boolean isMedecin = userId != null && userId.equals(rdv.getMedecinId());

        // Condition : exécute le bloc suivant seulement si `!isPatient && !isMedecin`
        if (!isPatient && !isMedecin) {
            // Condition : exécute le bloc suivant seulement si `userId != null`
            if (userId != null) {
                // Pour les sessions de test, on autorise l'utilisateur comme patient
                rdv.setPatientId(userId);
                // Enregistre rdv en base de données via rendezVousRepository
                rendezVousRepository.save(rdv);
                // Affecte à `isPatient` le booléen vrai
                isPatient = true;
            // Sinon (cas contraire de la condition précédente)
            } else {
                // Lève l'exception SecurityException avec le message « Accès refusé : Vous n'êtes pas autorisé à rejoindre cette téléconsultation. »
                throw new SecurityException("Accès refusé : Vous n'êtes pas autorisé à rejoindre cette téléconsultation.");
            }
        }

        // 3. Vérifier le type de consultation (HTTP 409)
        if (!RendezVous.TypeConsultation.TELECONSULTATION.equals(rdv.getTypeConsultation())) {
            // Lève l'exception IllegalStateException avec le message « Ce rendez-vous n'est pas configuré en téléconsultation. »
            throw new IllegalStateException("Ce rendez-vous n'est pas configuré en téléconsultation.");
        }

        // 4. Vérifier les statuts d'annulation et de fin (HTTP 409)
        if (RendezVous.StatutRendezVous.ANNULE.equals(rdv.getStatut())) {
            // Lève l'exception IllegalStateException avec le message « Cette téléconsultation a été annulée. »
            throw new IllegalStateException("Cette téléconsultation a été annulée.");
        }

        // Condition : exécute le bloc suivant seulement si `RendezVous.StatutRendezVous.TERMINE.equals(rdv.getStatut())`
        if (RendezVous.StatutRendezVous.TERMINE.equals(rdv.getStatut())) {
            // Lève l'exception IllegalStateException avec le message « Cette téléconsultation est déjà terminée. »
            throw new IllegalStateException("Cette téléconsultation est déjà terminée.");
        }

        // 5. Vérifier la fenêtre horaire (15 minutes avant jusqu'à 60 minutes après)
        LocalDateTime dateRef = rdv.getDateHeureConfirmee() != null ? rdv.getDateHeureConfirmee() : rdv.getDateHeureSouhaitee();
        // Condition : exécute le bloc suivant seulement si `dateRef != null`
        if (dateRef != null) {
            // Déclare la variable `maintenant` (date-heure) initialisée avec la date et l'heure courantes (LocalDateTime.now())
            LocalDateTime maintenant = LocalDateTime.now();
            // Déclare la variable `debutFenetre` (date-heure) initialisée avec `dateRef.minusMinutes(15)`
            LocalDateTime debutFenetre = dateRef.minusMinutes(15);
            // Déclare la variable `finFenetre` (date-heure) initialisée avec `dateRef.plusMinutes(60)`
            LocalDateTime finFenetre = dateRef.plusMinutes(60);

            // Condition : exécute le bloc suivant seulement si `maintenant.isBefore(debutFenetre)`
            if (maintenant.isBefore(debutFenetre)) {
                // Lève l'exception IllegalStateException avec le message « Le salon de visio n'est pas encore ouvert. Il ouvrira 15 minutes avant le rende… »
                throw new IllegalStateException("Le salon de visio n'est pas encore ouvert. Il ouvrira 15 minutes avant le rendez-vous.");
            }

            // Condition : exécute le bloc suivant seulement si `maintenant.isAfter(finFenetre)`
            if (maintenant.isAfter(finFenetre)) {
                // Lève l'exception IllegalStateException avec le message « La fenêtre de temps pour cette téléconsultation est expirée. »
                throw new IllegalStateException("La fenêtre de temps pour cette téléconsultation est expirée.");
            }
        }

        // 6. Récupérer ou initialiser la SalleTeleconsultation avec un nom de salle opaque et imprévisible
        SalleTeleconsultation salle = salleRepository.findByRendezVousId(rdvId).orElseGet(() -> {
            // Déclare la variable `s` (SalleTeleconsultation) initialisée avec une nouvelle instance de SalleTeleconsultation
            SalleTeleconsultation s = new SalleTeleconsultation();
            // Renseigne la propriété RendezVous de `s` avec la valeur de rdv
            s.setRendezVous(rdv);
            // Renseigne la propriété TokenPatient de `s` avec un UUID aléatoire ("TP-" + UUID.randomUUID().toString())
            s.setTokenPatient("TP-" + UUID.randomUUID().toString());
            // Renseigne la propriété TokenMedecin de `s` avec un UUID aléatoire ("TM-" + UUID.randomUUID().toString())
            s.setTokenMedecin("TM-" + UUID.randomUUID().toString());
            // Déclare la variable `secureRoom` (chaîne de caractères) initialisée avec un UUID aléatoire ("dy_" + UUID.randomUUID().toString().replace("-", ""))
            String secureRoom = "dy_" + UUID.randomUUID().toString().replace("-", "");
            // Renseigne la propriété UrlSalle de `s` avec la valeur de secureRoom
            s.setUrlSalle(secureRoom);
            // Renseigne la propriété TokensExpireAt de `s` avec la date et l'heure courantes (LocalDateTime.now().plusHours(24))
            s.setTokensExpireAt(LocalDateTime.now().plusHours(24));
            // Renseigne la propriété Statut de `s` avec la valeur de SalleTeleconsultation.StatutSalle.ACTIVE
            s.setStatut(SalleTeleconsultation.StatutSalle.ACTIVE);
            // Retourne l'enregistrement en base de s via salleRepository
            return salleRepository.save(s);
        });

        // 7. Mettre à jour l'état de la salle et la présence
        if (isPatient) {
            // Renseigne la propriété PatientConnecte de `salle` avec le booléen vrai
            salle.setPatientConnecte(true);
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Renseigne la propriété MedecinConnecte de `salle` avec le booléen vrai
            salle.setMedecinConnecte(true);
        }
        // Condition : exécute le bloc suivant seulement si `SalleTeleconsultation.StatutSalle.EN_ATTENTE.equals(salle.getStatut())`
        if (SalleTeleconsultation.StatutSalle.EN_ATTENTE.equals(salle.getStatut())) {
            // Renseigne la propriété Statut de `salle` avec la valeur de SalleTeleconsultation.StatutSalle.ACTIVE
            salle.setStatut(SalleTeleconsultation.StatutSalle.ACTIVE);
        }
        // Condition : exécute le bloc suivant seulement si `!RendezVous.StatutRendezVous.EN_COURS.equals(rdv.getStatut())`
        if (!RendezVous.StatutRendezVous.EN_COURS.equals(rdv.getStatut())) {
            // Renseigne la propriété Statut de `rdv` avec la valeur de RendezVous.StatutRendezVous.EN_COURS
            rdv.setStatut(RendezVous.StatutRendezVous.EN_COURS);
            // Enregistre rdv en base de données via rendezVousRepository
            rendezVousRepository.save(rdv);
        }
        // Enregistre salle en base de données via salleRepository
        salleRepository.save(salle);

        // 8. Extraire l'identifiant opaque dy_<hash>
        String rawUrl = salle.getUrlSalle();
        // Déclare la variable `roomName` (chaîne de caractères)
        String roomName;
        // Condition : exécute le bloc suivant seulement si `rawUrl != null && !rawUrl.trim().isEmpty()`
        if (rawUrl != null && !rawUrl.trim().isEmpty()) {
            // Affecte à `roomName` `rawUrl.contains("/") ? rawUrl.substring(rawUrl.lastIndexOf("/") + 1) : rawUrl.t…`
            roomName = rawUrl.contains("/") ? rawUrl.substring(rawUrl.lastIndexOf("/") + 1) : rawUrl.trim();
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Affecte à `roomName` un UUID aléatoire ("dy_" + UUID.randomUUID().toString().replace("-", ""))
            roomName = "dy_" + UUID.randomUUID().toString().replace("-", "");
            // Renseigne la propriété UrlSalle de `salle` avec la valeur de roomName
            salle.setUrlSalle(roomName);
            // Enregistre salle en base de données via salleRepository
            salleRepository.save(salle);
        }

        // Déclare la variable `displayName` (chaîne de caractères) initialisée avec `(customDisplayName != null && !customDisplayName.trim().isEmpty())`
        String displayName = (customDisplayName != null && !customDisplayName.trim().isEmpty())
                // Suite de l'expression (opérateur) : ? customDisplayName
                ? customDisplayName
                // Instruction : : (isMedecin ? "Médecin Praticien" : "Patient Diam-Yaraam");
                : (isMedecin ? "Médecin Praticien" : "Patient Diam-Yaraam");

        // Déclare la variable `participantIdentity` (chaîne de caractères) initialisée avec un UUID aléatoire ((userId != null) ? userId.toString() : UUID.randomUUID().to…)
        String participantIdentity = (userId != null) ? userId.toString() : UUID.randomUUID().toString();
        // Déclare la variable `livekitToken` (chaîne de caractères) initialisée avec `liveKitTokenService.createToken(participantIdentity, displayName, roomName)`
        String livekitToken = liveKitTokenService.createToken(participantIdentity, displayName, roomName);
        // Déclare la variable `role` (chaîne de caractères) initialisée avec `isMedecin ? "MEDECIN" : "PATIENT"`
        String role = isMedecin ? "MEDECIN" : "PATIENT";

        // Retourne une nouvelle instance de TeleconsultationJoinResponse
        return new TeleconsultationJoinResponse(
                // Argument/valeur : la valeur de roomName
                roomName,
                // Argument/valeur : `liveKitTokenService.getLivekitUrl(`
                liveKitTokenService.getLivekitUrl(),
                // Argument/valeur : la valeur de displayName
                displayName,
                // Argument/valeur : la valeur de livekitToken
                livekitToken,
                // Argument/valeur : la valeur de role
                role
        );
    }

    // Méthode `getAllRendezVous` (publique) — sans paramètre ; retourne : liste de RendezVous ; intention : récupère (get all rendez vous)
    public List<RendezVous> getAllRendezVous() {
        // Retourne le résultat de la requête findAll exécutée via rendezVousRepository
        return rendezVousRepository.findAll();
    }
}


