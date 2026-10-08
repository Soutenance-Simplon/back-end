// Déclaration du package Java : `com.diamyaraam.auth.controller`
package com.diamyaraam.auth.controller;

// Import de la classe `AuditLog` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.AuditLog;
// Import de la classe `AuditLogRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.AuditLogRepository;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `UserRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.UserRepository;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : JOURNALISATION D'AUDIT & TRAÇABILITÉ LÉGALE (AUDIT LOG CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 JUSTIFICATION RÉGLEMENTAIRE & SÉCURITÉ POUR LA SOUTENANCE :
 * Dans le domaine de la santé numérique (e-santé), la protection des Données Personnelles de Santé (DPS)
 * est régie par la loi sénégalaise n° 2008-12 sur les données à caractère personnel (supervisée par la CDP)
 * ainsi que par les standards internationaux (RGPD / HIPAA).
 * 
 * 🛡️ OBJECTIFS DE LA PISTE D'AUDIT :
 * 1. Imputabilité & Non-Répudiation :
 *    Savoir avec certitude QUI a accédé à quel dossier médical, QUAND (horodatage serveur inviolable),
 *    DEPUIS QUELLE ADRESSE IP, et pour QUEL MOTIF (téléconsultation, urgence SAMU, consultation physique).
 * 
 * 2. Détection Précoce d'Intrusion (Brute-Force & Credential Stuffing) :
 *    Traçage des tentatives d'accès échouées (`success = false`) pour déclencher le blocage automatique
 *    après 5 tentatives infructueuses.
 * 
 * 3. Indépendance et Résilience :
 *    Les journaux sont conservés même en cas de suppression ou d'anonymisation d'un compte utilisateur.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /auth/audit »
@RequestMapping("/auth/audit")
// Déclaration de la classe `AuditLogController` (rôle : expose des routes HTTP)
public class AuditLogController {

    /** Répertoire Spring Data JPA pour les logs d'audit */
    private final AuditLogRepository auditLogRepository;

    /** Répertoire des utilisateurs pour lier l'auteur de l'action */
    private final UserRepository userRepository;

    /**
     * Constructeur avec injection des dépendances requises.
     */
    public AuditLogController(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        // Initialise l'attribut `auditLogRepository` avec la valeur de auditLogRepository
        this.auditLogRepository = auditLogRepository;
        // Initialise l'attribut `userRepository` avec la valeur de userRepository
        this.userRepository = userRepository;
    }


    // Déclaration de l'enregistrement (record) `AuditLogDto`
    public record AuditLogDto(
        // Suite de l'instruction précédente : UUID id,
        UUID id,
        // Suite de l'instruction précédente : String actionType,
        String actionType,
        // Suite de l'instruction précédente : String telephoneTente,
        String telephoneTente,
        // Suite de l'instruction précédente : String ipAddress,
        String ipAddress,
        // Suite de l'instruction précédente : String details,
        String details,
        // Suite de l'instruction précédente : Boolean success,
        Boolean success,
        // Suite de l'instruction précédente : LocalDateTime createdAt,
        LocalDateTime createdAt,
        // Suite de l'instruction précédente : UUID userId,
        UUID userId,
        // Suite de l'instruction précédente : String userName,
        String userName,
        // Suite de l'instruction précédente : String userTelephone,
        String userTelephone,
        // Suite de l'instruction précédente : String userRole
        String userRole
    ) {}

    // Route HTTP GET sur le chemin « /recent »
    @GetMapping("/recent")
    // Méthode `getRecentAuditLogs` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de AuditLogDto ; intention : récupère (get recent audit logs)
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getRecentAuditLogs() {
        // Déclare la variable `logs` (liste de AuditLog) initialisée avec `auditLogRepository.findTop100AuditLogs().stream()`
        List<AuditLog> logs = auditLogRepository.findTop100AuditLogs().stream()
                // Enchaînement : appelle `limit(100)`
                .limit(100)
                // Enchaînement : appelle `toList();`
                .toList();

        // Déclare la variable `dtos` (liste de AuditLogDto) initialisée avec `logs.stream().map(log -> {`
        List<AuditLogDto> dtos = logs.stream().map(log -> {
            // Déclare la variable `uId` (identifiant UUID) initialisée avec la valeur nulle (absence de valeur)
            UUID uId = null;
            // Déclare la variable `uName` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
            String uName = null;
            // Déclare la variable `uTel` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
            String uTel = null;
            // Déclare la variable `uRole` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
            String uRole = null;
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Condition : exécute le bloc suivant seulement si `log.getUser() != null`
                if (log.getUser() != null) {
                    // Affecte à `uId` `log.getUser().getId()`
                    uId = log.getUser().getId();
                    // Déclare la variable `fn` (chaîne de caractères) initialisée avec `log.getUser().getFirstName() != null ? log.getUser().getFirstName() : ""`
                    String fn = log.getUser().getFirstName() != null ? log.getUser().getFirstName() : "";
                    // Déclare la variable `ln` (chaîne de caractères) initialisée avec `log.getUser().getLastName() != null ? log.getUser().getLastName() : ""`
                    String ln = log.getUser().getLastName() != null ? log.getUser().getLastName() : "";
                    // Affecte à `uName` `(fn + " " + ln).trim()`
                    uName = (fn + " " + ln).trim();
                    // Affecte à `uTel` `log.getUser().getTelephone()`
                    uTel = log.getUser().getTelephone();
                    // Condition : exécute le bloc suivant seulement si `log.getUser().getRole() != null`
                    if (log.getUser().getRole() != null) {
                        // Affecte à `uRole` `log.getUser().getRole().getNomRole()`
                        uRole = log.getUser().getRole().getNomRole();
                    }
                }
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {
            }
            // Retourne une nouvelle instance de AuditLogDto
            return new AuditLogDto(
                // Argument/valeur : `log.getId(`
                log.getId(),
                // Argument/valeur : `log.getActionType() != null ? log.getActionType().name() : null`
                log.getActionType() != null ? log.getActionType().name() : null,
                // Argument/valeur : `log.getTelephoneTente(`
                log.getTelephoneTente(),
                // Argument/valeur : `log.getIpAddress(`
                log.getIpAddress(),
                // Argument/valeur : `log.getDetails(`
                log.getDetails(),
                // Argument/valeur : `log.getSuccess(`
                log.getSuccess(),
                // Argument/valeur : `log.getCreatedAt(`
                log.getCreatedAt(),
                // Argument/valeur : la valeur de uId
                uId,
                // Suite de l'instruction précédente : uName != null && !uName.isBlank() ? uName : null,
                uName != null && !uName.isBlank() ? uName : null,
                // Argument/valeur : la valeur de uTel
                uTel,
                // Argument/valeur : la valeur de uRole
                uRole
            );
        // Suite de la chaîne d'appels : .toList();
        }).toList();

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Derniers journaux d'audit", dt…
        return ResponseEntity.ok(ApiResponse.success("Derniers journaux d'audit", dtos));
    }

    // Route HTTP POST sur le chemin « /log »
    @PostMapping("/log")
    // Méthode `createAuditLog` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de AuditLogDto ; intention : crée (create audit log)
    public ResponseEntity<ApiResponse<AuditLogDto>> createAuditLog(
            // Paramètre `actionType` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String actionType,
            // Paramètre `userId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) UUID userId,
            // Paramètre `details` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String details,
            // Paramètre `ipAddress` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String ipAddress,
            // Paramètre `telephoneTente` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String telephoneTente,
            // Paramètre `success` de type booléen — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "true") Boolean success) {

        // Déclare la variable `log` (AuditLog) initialisée avec une nouvelle instance de AuditLog
        AuditLog log = new AuditLog();
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Renseigne la propriété ActionType de `log` avec `AuditLog.ActionType.valueOf(actionType)`
            log.setActionType(AuditLog.ActionType.valueOf(actionType));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Renseigne la propriété ActionType de `log` avec la valeur de AuditLog.ActionType.ACCES_DOSSIER_MEDECIN
            log.setActionType(AuditLog.ActionType.ACCES_DOSSIER_MEDECIN);
        }
        // Condition : exécute le bloc suivant seulement si `userId != null`
        if (userId != null) {
            // Appelle la méthode `findById` sur `userRepository` : userRepository.findById(userId).ifPresent(log::setUser);
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        // Renseigne la propriété Details de `log` avec la valeur de details
        log.setDetails(details);
        // Renseigne la propriété IpAddress de `log` avec `ipAddress != null ? ipAddress : "127.0.0.1"`
        log.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
        // Renseigne la propriété TelephoneTente de `log` avec la valeur de telephoneTente
        log.setTelephoneTente(telephoneTente);
        // Renseigne la propriété Success de `log` avec `success != null ? success : true`
        log.setSuccess(success != null ? success : true);
        // Déclare la variable `saved` (AuditLog) initialisée avec l'enregistrement en base de log via auditLogRepository
        AuditLog saved = auditLogRepository.save(log);

        // Déclare la variable `uName` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
        String uName = null;
        // Déclare la variable `uTel` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
        String uTel = null;
        // Déclare la variable `uRole` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
        String uRole = null;
        // Condition : exécute le bloc suivant seulement si `saved.getUser() != null`
        if (saved.getUser() != null) {
            // Déclare la variable `fn` (chaîne de caractères) initialisée avec `saved.getUser().getFirstName() != null ? saved.getUser().getFirstName() : ""`
            String fn = saved.getUser().getFirstName() != null ? saved.getUser().getFirstName() : "";
            // Déclare la variable `ln` (chaîne de caractères) initialisée avec `saved.getUser().getLastName() != null ? saved.getUser().getLastName() : ""`
            String ln = saved.getUser().getLastName() != null ? saved.getUser().getLastName() : "";
            // Affecte à `uName` `(fn + " " + ln).trim()`
            uName = (fn + " " + ln).trim();
            // Affecte à `uTel` `saved.getUser().getTelephone()`
            uTel = saved.getUser().getTelephone();
            // Condition : exécute le bloc suivant seulement si `saved.getUser().getRole() != null`
            if (saved.getUser().getRole() != null) {
                // Affecte à `uRole` `saved.getUser().getRole().getNomRole()`
                uRole = saved.getUser().getRole().getNomRole();
            }
        }

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Événement consigné avec succès…
        return ResponseEntity.ok(ApiResponse.success("Événement consigné avec succès", new AuditLogDto(
            // Argument/valeur : `saved.getId(`
            saved.getId(),
            // Argument/valeur : `saved.getActionType().name(`
            saved.getActionType().name(),
            // Argument/valeur : `saved.getTelephoneTente(`
            saved.getTelephoneTente(),
            // Argument/valeur : `saved.getIpAddress(`
            saved.getIpAddress(),
            // Argument/valeur : `saved.getDetails(`
            saved.getDetails(),
            // Argument/valeur : `saved.getSuccess(`
            saved.getSuccess(),
            // Argument/valeur : `saved.getCreatedAt(`
            saved.getCreatedAt(),
            // Argument/valeur : la valeur de userId
            userId,
            // Argument/valeur : la valeur de uName
            uName,
            // Argument/valeur : la valeur de uTel
            uTel,
            // Argument/valeur : la valeur de uRole
            uRole
        )));
    }
}

