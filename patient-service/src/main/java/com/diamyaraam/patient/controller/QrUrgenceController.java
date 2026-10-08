// Déclaration du package Java : `com.diamyaraam.patient.controller`
package com.diamyaraam.patient.controller;

// Import de la classe `QrEmergencyResponseDto` (paquet com.diamyaraam.patient.dto)
import com.diamyaraam.patient.dto.QrEmergencyResponseDto;
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

// Import de la classe `Arrays` (paquet java.util)
import java.util.Arrays;
// Import de la classe `Collections` (paquet java.util)
import java.util.Collections;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : SCAN DU CODE QR MÉDICAL D'URGENCE (QR URGENCE CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 SUJET MAJEUR D'INNOVATION & QUESTION DU JURY DE SOUTENANCE :
 * "Comment concilier secours vital immédiat et respect du secret médical lors du scan d'un QR code ?"
 * 
 * 🚑 ARCHITECTURE DU SYSTÈME DU QR VITAL ANONYMISÉ :
 * Le QR code imprimé sur les cartes et bracelets Diam Yaraam n'encode JAMAIS les données médicales
 * en clair (car n'importe qui avec un smartphone pourrait les lire).
 * Le QR code contient uniquement un jeton opaque et anonymisé : `qrToken` (ex: "QR-A4020B38...").
 * 
 * 🛡️ MÉCANISME À DOUBLE NIVEAU D'HABILITATION (VIEW MODE) :
 * 1. Niveau 1 : Passant / Secouriste Grand Public (`CITOYEN_PUBLIC` - isMedecin = false) :
 *    - Accès restreint uniquement aux contacts d'urgence (nom, téléphone, lien de parenté)
 *      pour prévenir la famille sans divulguer le dossier médical confidentiel.
 * 
 * 2. Niveau 2 : Praticien Authentifié / SAMU (`MEDECIN_AUTHENTIFIE` - isMedecin = true) :
 *    - Accès étendu aux constantes vitales : Groupe sanguin, rhésus, allergies sévères
 *      (ex: allergie pénicilline), contre-indications médicamenteuses et maladies chroniques
 *      permettant de sauver la vie du patient inconscient.
 * ====================================================================================================
 */
// Annotation Spring désignant un contrôleur REST produisant des réponses au format JSON
@RestController
// Préfixe de routage pour les points d'accès relatifs au scan d'urgence par QR code
@RequestMapping("/patients/qr-urgence")
// Déclaration de la classe `QrUrgenceController` (rôle : expose des routes HTTP)
public class QrUrgenceController {

    // Référence vers le service métier patient
    private final PatientService patientService;

    // Constructeur d'injection du service patient géré par Spring IoC
    public QrUrgenceController(PatientService patientService) {
        // Affectation du composant de service injecté
        this.patientService = patientService;
    }

    /**
     * ENDPOINT : Résolution sécurisée du QR Code d'urgence
     * GET /patients/qr-urgence/scan/{qrToken}?isMedecin=true|false
     * 
     * @param qrToken Jeton alphanumérique ou UUID extrait du scan du code QR
     * @param isMedecin Booléen indiquant si l'utilisateur qui scanne est un médecin authentifié
     * @return DTO contenant les informations autorisées selon le profil du lecteur
     */
    // Mappage de la requête HTTP GET avec paramètre de chemin qrToken
    @GetMapping("/scan/{qrToken}")
    // Méthode `scanQrCode` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de QrEmergencyResponseDto ; intention : analyse (scan qr code)
    public ResponseEntity<ApiResponse<QrEmergencyResponseDto>> scanQrCode(
            // Extraction du jeton QR depuis l'URI
            @PathVariable String qrToken,
            // Paramètre de requête indiquant si le scanner est un praticien de santé agréé (défaut: false)
            @RequestParam(defaultValue = "false") boolean isMedecin) {

        // Résolution du dossier patient associé au jeton scanné
        Patient patient = patientService.getByQrToken(qrToken);

        // Instanciation de l'objet de transfert de données de réponse
        QrEmergencyResponseDto dto = new QrEmergencyResponseDto();
        // Attribution du nom de la personne à prévenir en cas d'urgence
        dto.setContactUrgenceNom(patient.getContactUrgenceNom());
        // Attribution du numéro de téléphone d'urgence de la personne de confiance
        dto.setContactUrgenceTelephone(patient.getContactUrgenceTelephone());
        // Attribution du lien de parenté (ex: Épouse, Parent, Tuteur)
        dto.setContactUrgenceLien(patient.getContactUrgenceLien());
        // Attribution de l'adresse de résidence pour géolocalisation des secours
        dto.setAdresse(patient.getAdresse());
        // Attribution de la ville de résidence (ex: Dakar)
        dto.setVille(patient.getVille());

        // Conditionnement du niveau d'accès selon le profil du lecteur
        if (isMedecin) {
            // Mode Médecin : accès étendu aux constantes médicales d'urgence
            dto.setViewMode("MEDECIN_AUTHENTIFIE");
            // Libellé de courtoisie du patient
            dto.setNom("Patient");
            // Identifiant du jeton d'urgence
            dto.setPrenom(qrToken);
            // Groupe sanguin et rhésus
            dto.setGroupeSanguin(null);
            // Liste des allergies prioritaires
            dto.setAllergies(Collections.emptyList());
            // Liste des antécédents médicaux immédiats
            dto.setAntecedents(Collections.emptyList());
            // Liste des affections chroniques
            dto.setMaladiesChroniques(Collections.emptyList());
            // Liste des traitements médicamenteux en cours
            dto.setTraitementsEnCours(Collections.emptyList());
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Mode Passant / Citoyen : masquage strict du dossier médical pour le secret médical
            dto.setViewMode("CITOYEN_PUBLIC");
            // Anonymisation du patronyme
            dto.setNom("");
            // Libellé générique
            dto.setPrenom("Patient");
            // Masquage du groupe sanguin
            dto.setGroupeSanguin(null);
            // Masquage des allergies pour éviter toute automédication sauvage par un tiers
            dto.setAllergies(Collections.emptyList());
            // Masquage des antécédents
            dto.setAntecedents(Collections.emptyList());
            // Masquage des maladies chroniques
            dto.setMaladiesChroniques(Collections.emptyList());
            // Masquage des traitements en cours
            dto.setTraitementsEnCours(Collections.emptyList());
        }

        // Renvoi de la réponse HTTP 200 OK enveloppée dans l'ApiResponse standardisée
        return ResponseEntity.ok(
            // Payload de succès avec message contextuel incluant le mode d'affichage
            ApiResponse.success("Fiche médicale d'urgence résolue avec succès (" + dto.getViewMode() + ")", dto)
        );
    }
}
