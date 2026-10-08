// Déclaration du package Java : `com.diamyaraam.auth.service`
package com.diamyaraam.auth.service;

// Import de la classe `AuditLog` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.AuditLog;
// Import de la classe `OtpCode` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.OtpCode;
// Import de la classe `AuditLogRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.AuditLogRepository;
// Import de la classe `OtpCodeRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.OtpCodeRepository;
// Import de la classe `UserRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.UserRepository;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `SecureRandom` (paquet java.security)
import java.security.SecureRandom;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;

/**
 * ====================================================================================================
 * SERVICE TECHNIQUE : CYCLE DE VIE DES CODES OTP (OTP SERVICE)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS CLÉS POUR LA SOUTENANCE :
 * La validation par OTP (One-Time Password) est le socle de l'authentification sans mot de passe
 * ou de la double authentification (2FA) adaptée au contexte ouest-africain.
 * 
 * 🛡️ RÈGLES DE SÉCURITÉ CRYPTOGRAPHIQUE :
 * 1. Générateur Pseudo-Aléatoire Cryptographiquement Sûr (CSPRNG) :
 *    - Utilisation de `java.security.SecureRandom` plutôt que `java.util.Random`.
 *    - `SecureRandom` tire son entropie de l'OS (/dev/urandom sous Linux/Docker), rendant la suite de nombres
 *      strictement imprédictible pour un attaquant externe.
 * 
 * 2. Invalidation Systématique des Codes Précédents :
 *    - Dès qu'un nouvel OTP est demandé pour un numéro, tous les anciens codes encore valides sont révoqués.
 * 
 * 3. Fenêtre de Validité Temporelle & Plafond de Tentatives :
 *    - Validité restreinte à 5 minutes pour réduire le risque d'interception.
 *    - Maximum 5 tentatives autorisées avant destruction immédiate de l'OTP pour contrer le bruteforce.
 * 
 * 4. Canal de Diffusion WhatsApp Cloud API :
 *    - Envoi direct via WhatsApp (Meta Graph API) offrant un taux de délivrabilité supérieur aux SMS classiques.
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `OtpService` (rôle : porte la logique métier)
public class OtpService {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(OtpService.class)`
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    // Attribut `otpCodeRepository` de type OtpCodeRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final OtpCodeRepository otpCodeRepository;
    // Attribut `userRepository` de type UserRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final UserRepository userRepository;
    // Attribut `auditLogRepository` de type AuditLogRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final AuditLogRepository auditLogRepository;
    // Attribut `whatsAppCloudApiService` de type WhatsAppCloudApiService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final WhatsAppCloudApiService whatsAppCloudApiService;

    /** Durée de validité de l'OTP en minutes (défaut : 5) */
    @Value("${otp.validity-minutes:5}")
    // Attribut `validityMinutes` de type entier [privée]
    private int validityMinutes;

    /** Nombre maximal de tentatives erronées permises (défaut : 5) */
    @Value("${otp.max-attempts:5}")
    // Attribut `maxAttempts` de type entier [privée]
    private int maxAttempts;

    /** Activation du contournement pour les tests automatisés ou démonstration sans réseau */
    @Value("${otp.bypass.enabled:false}")
    // Attribut `otpBypassEnabled` de type booléen [privée]
    private boolean otpBypassEnabled;

    /** Code statique utilisé uniquement lorsque le mode bypass de test est activé */
    @Value("${otp.bypass.code:456321}")
    // Attribut `otpBypassCode` de type chaîne de caractères [privée]
    private String otpBypassCode;

    /**
     * Constructeur injectant les repositories et le service tiers WhatsApp.
     */
    public OtpService(
            // Paramètre `otpCodeRepository` de type OtpCodeRepository
            OtpCodeRepository otpCodeRepository,
            // Paramètre `userRepository` de type UserRepository
            UserRepository userRepository,
            // Paramètre `auditLogRepository` de type AuditLogRepository
            AuditLogRepository auditLogRepository,
            // Paramètre `whatsAppCloudApiService` de type WhatsAppCloudApiService
            WhatsAppCloudApiService whatsAppCloudApiService) {
        // Initialise l'attribut `otpCodeRepository` avec la valeur de otpCodeRepository
        this.otpCodeRepository = otpCodeRepository;
        // Initialise l'attribut `userRepository` avec la valeur de userRepository
        this.userRepository = userRepository;
        // Initialise l'attribut `auditLogRepository` avec la valeur de auditLogRepository
        this.auditLogRepository = auditLogRepository;
        // Initialise l'attribut `whatsAppCloudApiService` avec la valeur de whatsAppCloudApiService
        this.whatsAppCloudApiService = whatsAppCloudApiService;
    }


    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `sendOtp` (publique) — paramètres : `telephone` (chaîne de caractères), `type` (OtpCode.OtpType) ; retourne : chaîne de caractères ; intention : envoie (send otp)
    public String sendOtp(String telephone, OtpCode.OtpType type) {
        // Appelle la méthode `invalidateAll` sur `otpCodeRepository` : otpCodeRepository.invalidateAll(telephone, type);
        otpCodeRepository.invalidateAll(telephone, type);

        // Déclare la variable `code` (chaîne de caractères) initialisée avec `generateSecureCode()`
        String code = generateSecureCode();

        // Déclare la variable `otp` (OtpCode) initialisée avec une nouvelle instance de OtpCode
        OtpCode otp = new OtpCode();
        // Renseigne la propriété Telephone de `otp` avec la valeur de telephone
        otp.setTelephone(telephone);
        // Renseigne la propriété OtpType de `otp` avec la valeur de type
        otp.setOtpType(type);
        // Renseigne la propriété Code de `otp` avec la valeur de code
        otp.setCode(code);
        // Renseigne la propriété ExpiresAt de `otp` avec la date et l'heure courantes (LocalDateTime.now().plusMinutes(validityMinutes))
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(validityMinutes));
        // Renseigne la propriété Attempts de `otp` avec la valeur numérique 0
        otp.setAttempts(0);
        // Renseigne la propriété Used de `otp` avec le booléen faux
        otp.setUsed(false);
        // Enregistre otp en base de données via otpCodeRepository
        otpCodeRepository.save(otp);

        // Écrit un message informatif dans les journaux : "[OTP ENVOYÉ] Code OTP expédié pour {} (valide {} min)", maskTelephon…
        log.info("[OTP ENVOYÉ] Code OTP expédié pour {} (valide {} min)", maskTelephone(telephone), validityMinutes);

        // Envoi automatique via l'API officielle Meta WhatsApp Cloud
        whatsAppCloudApiService.sendOtpMessage(telephone, code, validityMinutes);

        // Appelle la méthode `findByTelephone` sur `userRepository` : userRepository.findByTelephone(telephone).ifPresent(user -> {
        userRepository.findByTelephone(telephone).ifPresent(user -> {
            // Déclare la variable `audit` (AuditLog) initialisée avec une nouvelle instance de AuditLog
            AuditLog audit = new AuditLog();
            // Renseigne la propriété ActionType de `audit` avec la valeur de AuditLog.ActionType.OTP_ENVOYE
            audit.setActionType(AuditLog.ActionType.OTP_ENVOYE);
            // Renseigne la propriété User de `audit` avec la valeur de user
            audit.setUser(user);
            // Renseigne la propriété Details de `audit` avec `"Type: " + type.name() + " via WhatsApp Cloud API"`
            audit.setDetails("Type: " + type.name() + " via WhatsApp Cloud API");
            // Renseigne la propriété Success de `audit` avec le booléen vrai
            audit.setSuccess(true);
            // Enregistre audit en base de données via auditLogRepository
            auditLogRepository.save(audit);
        });

        // Retourne la valeur de code
        return code;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `verifyOtp` (publique) — paramètres : `telephone` (chaîne de caractères), `codeSaisi` (chaîne de caractères), `type` (OtpCode.OtpType) ; retourne : booléen ; intention : vérifie (verify otp)
    public boolean verifyOtp(String telephone, String codeSaisi, OtpCode.OtpType type) {
        // Bypass de test pour environnement contrôlé (désactivable en production)
        if (otpBypassEnabled && otpBypassCode != null && !otpBypassCode.isBlank() && otpBypassCode.equals(codeSaisi)) {
            // Condition : exécute le bloc suivant seulement si `OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)`
            if (OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)) {
                // Appelle la méthode `findByTelephone` sur `userRepository` : userRepository.findByTelephone(telephone).ifPresent(user -> {
                userRepository.findByTelephone(telephone).ifPresent(user -> {
                    // Renseigne la propriété PhoneVerified de `user` avec le booléen vrai
                    user.setPhoneVerified(true);
                    // Renseigne la propriété AccountStatus de `user` avec la valeur de com.diamyaraam.auth.entity.User.AccountStatus.ACTIF
                    user.setAccountStatus(com.diamyaraam.auth.entity.User.AccountStatus.ACTIF);
                    // Enregistre user en base de données via userRepository
                    userRepository.save(user);
                    // Écrit un message informatif dans les journaux : "Compte activé (BYPASS configuré) pour : {}", maskTelephone(telephone…
                    log.info("Compte activé (BYPASS configuré) pour : {}", maskTelephone(telephone));
                });
            }
            // Retourne le booléen vrai
            return true;
        }

        // Déclare la variable `optOtp` (valeur optionnelle de OtpCode) initialisée avec la valeur de otpCodeRepository
        Optional<OtpCode> optOtp = otpCodeRepository
                // Enchaînement : appelle `findTopByTelephoneAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(telephone, type);`
                .findTopByTelephoneAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(telephone, type);

        // Condition : exécute le bloc suivant seulement si `optOtp.isEmpty()`
        if (optOtp.isEmpty()) {
            // Écrit un message d'avertissement dans les journaux : "Aucun OTP actif pour {}", maskTelephone(telephone));
            log.warn("Aucun OTP actif pour {}", maskTelephone(telephone));
            // Retourne le booléen faux
            return false;
        }

        // Déclare la variable `otp` (OtpCode) initialisée avec `optOtp.get()`
        OtpCode otp = optOtp.get();

        // Condition : exécute le bloc suivant seulement si `otp.isExpired()`
        if (otp.isExpired()) {
            // Appelle la méthode locale `journal` : journal(telephone, AuditLog.ActionType.OTP_EXPIRE, "OTP expiré");
            journal(telephone, AuditLog.ActionType.OTP_EXPIRE, "OTP expiré");
            // Retourne le booléen faux
            return false;
        }

        // Renseigne la propriété Attempts de `otp` avec `otp.getAttempts() + 1`
        otp.setAttempts(otp.getAttempts() + 1);
        // Enregistre otp en base de données via otpCodeRepository
        otpCodeRepository.save(otp);

        // Condition : exécute le bloc suivant seulement si `otp.isMaxAttemptsReached(maxAttempts)`
        if (otp.isMaxAttemptsReached(maxAttempts)) {
            // Renseigne la propriété Used de `otp` avec le booléen vrai
            otp.setUsed(true);
            // Enregistre otp en base de données via otpCodeRepository
            otpCodeRepository.save(otp);
            // Appelle la méthode locale `journal` : journal(telephone, AuditLog.ActionType.OTP_MAX_ATTEINT, "Max tentatives OT…
            journal(telephone, AuditLog.ActionType.OTP_MAX_ATTEINT, "Max tentatives OTP atteint");
            // Retourne le booléen faux
            return false;
        }

        // Condition : exécute le bloc suivant seulement si `!otp.getCode().equals(codeSaisi)`
        if (!otp.getCode().equals(codeSaisi)) {
            // Appelle la méthode locale `journal` : journal(telephone, AuditLog.ActionType.OTP_ECHEC, "Tentative " + otp.getAt…
            journal(telephone, AuditLog.ActionType.OTP_ECHEC, "Tentative " + otp.getAttempts() + "/" + maxAttempts);
            // Retourne le booléen faux
            return false;
        }

        // Renseigne la propriété Used de `otp` avec le booléen vrai
        otp.setUsed(true);
        // Enregistre otp en base de données via otpCodeRepository
        otpCodeRepository.save(otp);

        // Condition : exécute le bloc suivant seulement si `OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)`
        if (OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)) {
            // Appelle la méthode `findByTelephone` sur `userRepository` : userRepository.findByTelephone(telephone).ifPresent(user -> {
            userRepository.findByTelephone(telephone).ifPresent(user -> {
                // Renseigne la propriété PhoneVerified de `user` avec le booléen vrai
                user.setPhoneVerified(true);
                // Renseigne la propriété AccountStatus de `user` avec la valeur de com.diamyaraam.auth.entity.User.AccountStatus.ACTIF
                user.setAccountStatus(com.diamyaraam.auth.entity.User.AccountStatus.ACTIF);
                // Enregistre user en base de données via userRepository
                userRepository.save(user);
                // Appelle la méthode locale `journal` : journal(telephone, AuditLog.ActionType.ACTIVATION_COMPTE, "Compte activé");
                journal(telephone, AuditLog.ActionType.ACTIVATION_COMPTE, "Compte activé");
                // Écrit un message informatif dans les journaux : "Compte activé pour : {}", maskTelephone(telephone));
                log.info("Compte activé pour : {}", maskTelephone(telephone));
            });
        }

        // Appelle la méthode locale `journal` : journal(telephone, AuditLog.ActionType.OTP_VALIDE, "OTP validé - Type: " +…
        journal(telephone, AuditLog.ActionType.OTP_VALIDE, "OTP validé - Type: " + type.name());
        // Retourne le booléen vrai
        return true;
    }

    // Méthode `generateSecureCode` (privée) — sans paramètre ; retourne : chaîne de caractères ; intention : génère (generate secure code)
    private String generateSecureCode() {
        // Condition : exécute le bloc suivant seulement si `otpBypassEnabled && otpBypassCode != null && !otpBypassCode.isBlank()`
        if (otpBypassEnabled && otpBypassCode != null && !otpBypassCode.isBlank()) {
            // Retourne la valeur de otpBypassCode
            return otpBypassCode;
        }
        // Déclare la variable `random` (SecureRandom) initialisée avec une nouvelle instance de SecureRandom
        SecureRandom random = new SecureRandom();
        // Déclare la variable `num` (entier) initialisée avec `100000 + random.nextInt(900000)`
        int num = 100000 + random.nextInt(900000);
        // Retourne `String.valueOf(num)`
        return String.valueOf(num);
    }

    // Méthode `maskTelephone` (privée) — paramètres : `telephone` (chaîne de caractères) ; retourne : chaîne de caractères
    private String maskTelephone(String telephone) {
        // Condition : exécute le bloc suivant seulement si `telephone == null || telephone.length() < 4`
        if (telephone == null || telephone.length() < 4) {
            // Retourne le texte "****"
            return "****";
        }
        // Déclare la variable `len` (entier) initialisée avec `telephone.length()`
        int len = telephone.length();
        // Retourne `telephone.substring(0, Math.min(3, len - 4)) + "****" + telephone.substring(len…`
        return telephone.substring(0, Math.min(3, len - 4)) + "****" + telephone.substring(len - 4);
    }

    // Méthode `journal` (privée) — paramètres : `telephone` (chaîne de caractères), `action` (AuditLog.ActionType), `details` (chaîne de caractères) ; retourne : aucune valeur
    private void journal(String telephone, AuditLog.ActionType action, String details) {
        // Déclare la variable `logItem` (AuditLog) initialisée avec une nouvelle instance de AuditLog
        AuditLog logItem = new AuditLog();
        // Renseigne la propriété ActionType de `logItem` avec la valeur de action
        logItem.setActionType(action);
        // Renseigne la propriété TelephoneTente de `logItem` avec la valeur de telephone
        logItem.setTelephoneTente(telephone);
        // Renseigne la propriété Details de `logItem` avec la valeur de details
        logItem.setDetails(details);
        // Renseigne la propriété Success de `logItem` avec `!AuditLog.ActionType.OTP_ECHEC.equals(action`
        logItem.setSuccess(!AuditLog.ActionType.OTP_ECHEC.equals(action)
            // Suite de l'expression (opérateur) : && !AuditLog.ActionType.OTP_MAX_ATTEINT.equals(action)
            && !AuditLog.ActionType.OTP_MAX_ATTEINT.equals(action)
            // Suite de l'expression (opérateur) : && !AuditLog.ActionType.OTP_EXPIRE.equals(action));
            && !AuditLog.ActionType.OTP_EXPIRE.equals(action));

        // Appelle la méthode `findByTelephone` sur `userRepository` : userRepository.findByTelephone(telephone).ifPresent(logItem::setUser);
        userRepository.findByTelephone(telephone).ifPresent(logItem::setUser);
        // Enregistre logItem en base de données via auditLogRepository
        auditLogRepository.save(logItem);
    }
}
