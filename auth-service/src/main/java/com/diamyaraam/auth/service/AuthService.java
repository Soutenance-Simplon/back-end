// Déclaration du package Java : `com.diamyaraam.auth.service`
package com.diamyaraam.auth.service;

// Import de la classe `AuthResponse` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.AuthResponse;
// Import de la classe `LoginRequest` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.LoginRequest;
// Import de la classe `RegisterPatientRequest` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.RegisterPatientRequest;
// Import de la classe `AuditLog` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.AuditLog;
// Import de la classe `OtpCode` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.OtpCode;
// Import de la classe `Role` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.Role;
// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `AuditLogRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.AuditLogRepository;
// Import de la classe `OtpCodeRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.OtpCodeRepository;
// Import de la classe `RoleRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.RoleRepository;
// Import de la classe `UserRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.UserRepository;
// Import de la classe `JwtService` (paquet com.diamyaraam.auth.security)
import com.diamyaraam.auth.security.JwtService;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `PasswordEncoder` (paquet org.springframework.security.crypto.password)
import org.springframework.security.crypto.password.PasswordEncoder;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * SERVICE MÉTIER : AUTHENTIFICATION, CONTRÔLE D'ACCÈS & SÉCURITÉ (AUTH SERVICE)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS CLÉS POUR LA SOUTENANCE :
 * Cette classe implémente le cœur de la logique de sécurité de la plateforme Diam Yaraam.
 * 
 * 🛡️ MÉCANISMES DE PROTECTION CONTRE LES ATTAQUES :
 * 1. Protection Anti-Brute-Force & Verrouillage Temporisé :
 *    - Compteur de tentatives échouées incrémenté en base (`failedLoginAttempts`).
 *    - Dès que 5 échecs consécutifs sont atteints, le compte passe automatiquement
 *      à l'état `BLOQUE` pour une durée de 15 minutes (`lockedUntil = now + 15 min`).
 *    - Empêche les attaques par dictionnaire et par force brute sur les mots de passe.
 * 
 * 2. Protection de la Vie Privée dans les Journaux (Privacy by Design) :
 *    - Les numéros de téléphone sont systématiquement masqués dans les logs (`maskTelephone`) :
 *      ex: "+22177****4567" pour empêcher la fuite de données personnelles (PII) dans les journaux serveurs.
 * 
 * 3. Hachage avec Sel Aléatoire (BCrypt Password Hashing) :
 *    - Ne stocke jamais de mot de passe en clair.
 *    - Utilisation de `passwordEncoder.matches()` qui protège nativement contre les attaques par canal auxiliaire (Timing Attacks).
 * 
 * 4. Piste d'Audit Systématique (Audit Trail) :
 *    - Chaque succès, échec, création de compte ou réinitialisation est instantanément persisté
 *      dans la table `audit_logs` avec l'IP cliente, la date et la cause de l'échec.
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `AuthService` (rôle : porte la logique métier)
public class AuthService {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(AuthService.class)`
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Attribut `userRepository` de type UserRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final UserRepository userRepository;
    // Attribut `roleRepository` de type RoleRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final RoleRepository roleRepository;
    // Attribut `otpCodeRepository` de type OtpCodeRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final OtpCodeRepository otpCodeRepository;
    // Attribut `auditLogRepository` de type AuditLogRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final AuditLogRepository auditLogRepository;
    // Attribut `passwordEncoder` de type PasswordEncoder — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final PasswordEncoder passwordEncoder;
    // Attribut `jwtService` de type JwtService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final JwtService jwtService;
    // Attribut `otpService` de type OtpService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final OtpService otpService;

    /** Nombre maximal de tentatives de mot de passe avant verrouillage temporaire (défaut : 5) */
    @Value("${auth.max-login-attempts:5}")
    // Attribut `maxLoginAttempts` de type entier [privée]
    private int maxLoginAttempts;

    /** Durée en minutes du blocage temporaire d'un compte (défaut : 15 min) */
    @Value("${auth.lockout-duration-minutes:15}")
    // Attribut `lockoutDurationMinutes` de type entier [privée]
    private int lockoutDurationMinutes;

    /**
     * Constructeur injectant l'ensemble des dépendances nécessaires au cycle d'authentification.
     */
    public AuthService(
            // Paramètre `userRepository` de type UserRepository
            UserRepository userRepository,
            // Paramètre `roleRepository` de type RoleRepository
            RoleRepository roleRepository,
            // Paramètre `otpCodeRepository` de type OtpCodeRepository
            OtpCodeRepository otpCodeRepository,
            // Paramètre `auditLogRepository` de type AuditLogRepository
            AuditLogRepository auditLogRepository,
            // Paramètre `passwordEncoder` de type PasswordEncoder
            PasswordEncoder passwordEncoder,
            // Paramètre `jwtService` de type JwtService
            JwtService jwtService,
            // Paramètre `otpService` de type OtpService
            OtpService otpService) {
        // Initialise l'attribut `userRepository` avec la valeur de userRepository
        this.userRepository = userRepository;
        // Initialise l'attribut `roleRepository` avec la valeur de roleRepository
        this.roleRepository = roleRepository;
        // Initialise l'attribut `otpCodeRepository` avec la valeur de otpCodeRepository
        this.otpCodeRepository = otpCodeRepository;
        // Initialise l'attribut `auditLogRepository` avec la valeur de auditLogRepository
        this.auditLogRepository = auditLogRepository;
        // Initialise l'attribut `passwordEncoder` avec la valeur de passwordEncoder
        this.passwordEncoder = passwordEncoder;
        // Initialise l'attribut `jwtService` avec la valeur de jwtService
        this.jwtService = jwtService;
        // Initialise l'attribut `otpService` avec la valeur de otpService
        this.otpService = otpService;
    }

    public java.util.Optional<User> findById(java.util.UUID id) {
        return userRepository.findById(id);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `registerPatient` (publique) — paramètres : `req` (RegisterPatientRequest) ; retourne : aucune valeur ; intention : inscrit (register patient)
    public void registerPatient(RegisterPatientRequest req) {
        // Condition : exécute le bloc suivant seulement si `userRepository.existsByTelephone(req.getTelephone())`
        if (userRepository.existsByTelephone(req.getTelephone())) {
            // Lève l'exception IllegalArgumentException avec le message « Ce numéro de téléphone est déjà utilisé. »
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
        }

        // Condition : exécute le bloc suivant seulement si `req.getEmail() != null && userRepository.existsByEmail(req.getEmail())`
        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            // Lève l'exception IllegalArgumentException avec le message « Cet email est déjà associé à un compte. »
            throw new IllegalArgumentException("Cet email est déjà associé à un compte.");
        }

        // Condition : exécute le bloc suivant seulement si `!req.getPassword().equals(req.getConfirmPassword())`
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            // Lève l'exception IllegalArgumentException avec le message « Les mots de passe ne correspondent pas. »
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        // Déclare la variable `rolePatient` (Role) initialisée avec le résultat de la requête findByNomRole exécutée via roleRepository
        Role rolePatient = roleRepository.findByNomRole("PATIENT")
                // Enchaînement : appelle `orElseGet(() -> roleRepository.save(new Role(null, "PATIENT…`
                .orElseGet(() -> roleRepository.save(new Role(null, "PATIENT")));

        // Déclare la variable `user` (User) initialisée avec une nouvelle instance de User
        User user = new User();
        // Renseigne la propriété FirstName de `user` avec la valeur de l'attribut FirstName de req
        user.setFirstName(req.getFirstName());
        // Renseigne la propriété LastName de `user` avec la valeur de l'attribut LastName de req
        user.setLastName(req.getLastName());
        // Renseigne la propriété Telephone de `user` avec la valeur de l'attribut Telephone de req
        user.setTelephone(req.getTelephone());
        // Renseigne la propriété Email de `user` avec la valeur de l'attribut Email de req
        user.setEmail(req.getEmail());
        // Renseigne la propriété Password de `user` avec `passwordEncoder.encode(req.getPassword())`
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        // Renseigne la propriété Role de `user` avec la valeur de rolePatient
        user.setRole(rolePatient);
        // Renseigne la propriété AccountStatus de `user` avec la valeur de User.AccountStatus.EN_ATTENTE
        user.setAccountStatus(User.AccountStatus.EN_ATTENTE);

        // Enregistre user en base de données via userRepository
        userRepository.save(user);

        // Appelle la méthode `sendOtp` sur `otpService` : otpService.sendOtp(req.getTelephone(), OtpCode.OtpType.VERIFICATION_TELEPH…
        otpService.sendOtp(req.getTelephone(), OtpCode.OtpType.VERIFICATION_TELEPHONE);
        // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.CREATION_COMPTE, user, null, "Inscription pati…
        journal(AuditLog.ActionType.CREATION_COMPTE, user, null, "Inscription patient", true);

        // Écrit un message informatif dans les journaux : "Compte patient créé : {} (statut EN_ATTENTE)", maskTelephone(req.get…
        log.info("Compte patient créé : {} (statut EN_ATTENTE)", maskTelephone(req.getTelephone()));
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `login` (publique) — paramètres : `req` (LoginRequest), `ipAddress` (chaîne de caractères) ; retourne : AuthResponse ; intention : authentifie (login)
    public AuthResponse login(LoginRequest req, String ipAddress) {
        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findByTelephone exécutée via userRepository
        User user = userRepository.findByTelephone(req.getTelephone())
                // Enchaînement : appelle `orElseThrow(() -> {`
                .orElseThrow(() -> {
                    // Appelle la méthode locale `journalEchec` : journalEchec(req.getTelephone(), ipAddress, "Numéro introuvable");
                    journalEchec(req.getTelephone(), ipAddress, "Numéro introuvable");
                    // Retourne une nouvelle instance de IllegalArgumentException
                    return new IllegalArgumentException("Numéro ou mot de passe incorrect.");
                });

        // Condition : exécute le bloc suivant seulement si `User.AccountStatus.EN_ATTENTE.equals(user.getAccountStatus())`
        if (User.AccountStatus.EN_ATTENTE.equals(user.getAccountStatus())) {
            // Lève l'exception IllegalStateException avec le message « Votre compte n'est pas encore activé. Vérifiez votre téléphone. »
            throw new IllegalStateException("Votre compte n'est pas encore activé. Vérifiez votre téléphone.");
        }

        // Condition : exécute le bloc suivant seulement si `User.AccountStatus.SUSPENDU.equals(user.getAccountStatus())`
        if (User.AccountStatus.SUSPENDU.equals(user.getAccountStatus())) {
            // Lève l'exception IllegalStateException avec le message « Votre compte est suspendu. Contactez l'administrateur. »
            throw new IllegalStateException("Votre compte est suspendu. Contactez l'administrateur.");
        }

        // Condition : exécute le bloc suivant seulement si `user.isAccountLocked()`
        if (user.isAccountLocked()) {
            // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Compte bloq…
            journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Compte bloqué", false);
            // Lève l'exception IllegalStateException avec le message « Compte bloqué jusqu'à »
            throw new IllegalStateException("Compte bloqué jusqu'à " + user.getLockedUntil() + ". Trop de tentatives.");
        }

        // Condition : exécute le bloc suivant seulement si `!passwordEncoder.matches(req.getPassword(), user.getPassword())`
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            // Appelle la méthode `incrementFailedAttempts` sur `userRepository` : userRepository.incrementFailedAttempts(user.getId());
            userRepository.incrementFailedAttempts(user.getId());
            // Déclare la variable `newCount` (entier) initialisée avec `user.getFailedLoginAttempts() + 1`
            int newCount = user.getFailedLoginAttempts() + 1;

            // Condition : exécute le bloc suivant seulement si `newCount >= maxLoginAttempts`
            if (newCount >= maxLoginAttempts) {
                // Renseigne la propriété AccountStatus de `user` avec la valeur de User.AccountStatus.BLOQUE
                user.setAccountStatus(User.AccountStatus.BLOQUE);
                // Renseigne la propriété LockedUntil de `user` avec la date et l'heure courantes (LocalDateTime.now().plusMinutes(lockoutDurationMinutes))
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                // Enregistre user en base de données via userRepository
                userRepository.save(user);
                // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.COMPTE_BLOQUE, user, ipAddress, "Bloqué après …
                journal(AuditLog.ActionType.COMPTE_BLOQUE, user, ipAddress, "Bloqué après " + newCount + " tentatives", false);
                // Lève l'exception IllegalStateException avec le message « Compte bloqué pour »
                throw new IllegalStateException("Compte bloqué pour " + lockoutDurationMinutes + " minutes.");
            }

            // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Tentative "…
            journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Tentative " + newCount + "/" + maxLoginAttempts, false);
            // Lève l'exception IllegalArgumentException avec le message « Numéro ou mot de passe incorrect. ( »
            throw new IllegalArgumentException("Numéro ou mot de passe incorrect. (" + newCount + "/" + maxLoginAttempts + " tentatives)");
        }

        // Appelle la méthode `resetFailedAttempts` sur `userRepository` : userRepository.resetFailedAttempts(user.getId());
        userRepository.resetFailedAttempts(user.getId());

        // Déclare la variable `token` (chaîne de caractères) initialisée avec `jwtService.generateToken(user)`
        String token = jwtService.generateToken(user);
        // Déclare la variable `refreshToken` (chaîne de caractères) initialisée avec `jwtService.generateRefreshToken(user)`
        String refreshToken = jwtService.generateRefreshToken(user);
        // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.CONNEXION_REUSSIE, user, ipAddress, "Login OK"…
        journal(AuditLog.ActionType.CONNEXION_REUSSIE, user, ipAddress, "Login OK", true);
        // Écrit un message informatif dans les journaux : "Login réussi : {}", maskTelephone(user.getTelephone()));
        log.info("Login réussi : {}", maskTelephone(user.getTelephone()));

        // Déclare la variable `response` (AuthResponse) initialisée avec une nouvelle instance de AuthResponse
        AuthResponse response = new AuthResponse();
        // Renseigne la propriété AccessToken de `response` avec la valeur de token
        response.setAccessToken(token);
        // Renseigne la propriété RefreshToken de `response` avec la valeur de refreshToken
        response.setRefreshToken(refreshToken);
        // Renseigne la propriété TokenType de `response` avec le texte "Bearer"
        response.setTokenType("Bearer");
        // Renseigne la propriété ExpiresIn de `response` avec `jwtService.getExpirationMs() / 1000`
        response.setExpiresIn(jwtService.getExpirationMs() / 1000);
        // Renseigne la propriété UserId de `response` avec la valeur de l'attribut Id de user
        response.setUserId(user.getId());
        // Renseigne la propriété Telephone de `response` avec la valeur de l'attribut Telephone de user
        response.setTelephone(user.getTelephone());
        // Renseigne la propriété FirstName de `response` avec la valeur de l'attribut FirstName de user
        response.setFirstName(user.getFirstName());
        // Renseigne la propriété LastName de `response` avec la valeur de l'attribut LastName de user
        response.setLastName(user.getLastName());
        // Renseigne la propriété Role de `response` avec `user.getRole() != null ? user.getRole().getNomRole() : ""`
        response.setRole(user.getRole() != null ? user.getRole().getNomRole() : "");
        // Renseigne la propriété AccountStatus de `response` avec `user.getAccountStatus().name()`
        response.setAccountStatus(user.getAccountStatus().name());
        // Renseigne la propriété PhotoProfil de `response` avec la valeur de l'attribut PhotoProfil de user
        response.setPhotoProfil(user.getPhotoProfil());

        // Retourne la valeur de response
        return response;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `updatePhotoProfil` (publique) — paramètres : `userId` (identifiant UUID), `photoProfil` (chaîne de caractères) ; retourne : chaîne de caractères ; intention : met à jour (update photo profil)
    public String updatePhotoProfil(UUID userId, String photoProfil) {
        // Condition : exécute le bloc suivant seulement si `userId == null`
        if (userId == null) {
            // Lève l'exception IllegalArgumentException avec le message « Identifiant utilisateur requis. »
            throw new IllegalArgumentException("Identifiant utilisateur requis.");
        }
        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findById exécutée via userRepository
        User user = userRepository.findById(userId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Utilisateur i…`
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        // Renseigne la propriété PhotoProfil de `user` avec la valeur de photoProfil
        user.setPhotoProfil(photoProfil);
        // Enregistre user en base de données via userRepository
        userRepository.save(user);
        // Écrit un message informatif dans les journaux : "Photo de profil mise à jour pour l'utilisateur ID: {}", userId);
        log.info("Photo de profil mise à jour pour l'utilisateur ID: {}", userId);
        // Retourne la valeur de photoProfil
        return photoProfil;
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique) (lecture seule : aucune modification de données)
    @Transactional(readOnly = true)
    // Méthode `searchUserByTelephone` (publique) — paramètres : `telephone` (chaîne de caractères) ; retourne : User
    public User searchUserByTelephone(String telephone) {
        // Retourne le résultat de la requête findByTelephone exécutée via userRepository
        return userRepository.findByTelephone(telephone)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Aucun utilisa…`
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur trouvé avec ce numéro."));
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `forgotPassword` (publique) — paramètres : `telephone` (chaîne de caractères) ; retourne : aucune valeur
    public void forgotPassword(String telephone) {
        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findByTelephone exécutée via userRepository
        User user = userRepository.findByTelephone(telephone)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Aucun utilisa…`
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur associé à ce numéro de téléphone."));

        // Appelle la méthode `sendOtp` sur `otpService` : otpService.sendOtp(user.getTelephone(), OtpCode.OtpType.REINITIALISATION_M…
        otpService.sendOtp(user.getTelephone(), OtpCode.OtpType.REINITIALISATION_MDP);
        // Écrit un message informatif dans les journaux : "Demande de réinitialisation de mot de passe envoyée pour : {}", tele…
        log.info("Demande de réinitialisation de mot de passe envoyée pour : {}", telephone);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `resetPassword` (publique) — paramètres : `telephone` (chaîne de caractères), `code` (chaîne de caractères), `newPassword` (chaîne de caractères), `confirmNewPassword` (chaîne de caractères) ; retourne : aucune valeur ; intention : réinitialise (reset password)
    public void resetPassword(String telephone, String code, String newPassword, String confirmNewPassword) {
        // Condition : exécute le bloc suivant seulement si `!newPassword.equals(confirmNewPassword)`
        if (!newPassword.equals(confirmNewPassword)) {
            // Lève l'exception IllegalArgumentException avec le message « Les nouveaux mots de passe ne correspondent pas. »
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas.");
        }

        // Déclare la variable `validOtp` (booléen) initialisée avec `otpService.verifyOtp(telephone, code, OtpCode.OtpType.REINITIALISATION_MDP)`
        boolean validOtp = otpService.verifyOtp(telephone, code, OtpCode.OtpType.REINITIALISATION_MDP);
        // Condition : exécute le bloc suivant seulement si `!validOtp`
        if (!validOtp) {
            // Lève l'exception IllegalArgumentException avec le message « Code OTP invalide ou expiré. »
            throw new IllegalArgumentException("Code OTP invalide ou expiré.");
        }

        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findByTelephone exécutée via userRepository
        User user = userRepository.findByTelephone(telephone)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Utilisateur n…`
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        // Renseigne la propriété Password de `user` avec `passwordEncoder.encode(newPassword)`
        user.setPassword(passwordEncoder.encode(newPassword));
        // Enregistre user en base de données via userRepository
        userRepository.save(user);

        // Appelle la méthode locale `journal` : journal(AuditLog.ActionType.ACTIVATION_COMPTE, user, null, "Mot de passe r…
        journal(AuditLog.ActionType.ACTIVATION_COMPTE, user, null, "Mot de passe réinitialisé", true);
        // Écrit un message informatif dans les journaux : "Mot de passe réinitialisé avec succès pour : {}", telephone);
        log.info("Mot de passe réinitialisé avec succès pour : {}", telephone);
    }

    // Méthode `refreshToken` (publique) — paramètres : `refreshToken` (chaîne de caractères) ; retourne : AuthResponse
    public AuthResponse refreshToken(String refreshToken) {
        // Condition : exécute le bloc suivant seulement si `!jwtService.isTokenValid(refreshToken)`
        if (!jwtService.isTokenValid(refreshToken)) {
            // Lève l'exception IllegalArgumentException avec le message « Refresh token invalide ou expiré. »
            throw new IllegalArgumentException("Refresh token invalide ou expiré.");
        }

        // Déclare la variable `userIdStr` (chaîne de caractères) initialisée avec `jwtService.extractUserId(refreshToken)`
        String userIdStr = jwtService.extractUserId(refreshToken);
        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findById exécutée via userRepository
        User user = userRepository.findById(java.util.UUID.fromString(userIdStr))
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Utilisateur n…`
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        // Déclare la variable `newAccessToken` (chaîne de caractères) initialisée avec `jwtService.generateToken(user)`
        String newAccessToken = jwtService.generateToken(user);
        // Déclare la variable `newRefreshToken` (chaîne de caractères) initialisée avec `jwtService.generateRefreshToken(user)`
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Déclare la variable `response` (AuthResponse) initialisée avec une nouvelle instance de AuthResponse
        AuthResponse response = new AuthResponse();
        // Renseigne la propriété AccessToken de `response` avec la valeur de newAccessToken
        response.setAccessToken(newAccessToken);
        // Renseigne la propriété RefreshToken de `response` avec la valeur de newRefreshToken
        response.setRefreshToken(newRefreshToken);
        // Renseigne la propriété TokenType de `response` avec le texte "Bearer"
        response.setTokenType("Bearer");
        // Renseigne la propriété ExpiresIn de `response` avec `jwtService.getExpirationMs() / 1000`
        response.setExpiresIn(jwtService.getExpirationMs() / 1000);
        // Renseigne la propriété UserId de `response` avec la valeur de l'attribut Id de user
        response.setUserId(user.getId());
        // Renseigne la propriété Telephone de `response` avec la valeur de l'attribut Telephone de user
        response.setTelephone(user.getTelephone());
        // Renseigne la propriété FirstName de `response` avec la valeur de l'attribut FirstName de user
        response.setFirstName(user.getFirstName());
        // Renseigne la propriété LastName de `response` avec la valeur de l'attribut LastName de user
        response.setLastName(user.getLastName());
        // Renseigne la propriété Role de `response` avec `user.getRole() != null ? user.getRole().getNomRole() : ""`
        response.setRole(user.getRole() != null ? user.getRole().getNomRole() : "");
        // Renseigne la propriété AccountStatus de `response` avec `user.getAccountStatus().name()`
        response.setAccountStatus(user.getAccountStatus().name());

        // Retourne la valeur de response
        return response;
    }

    // Méthode `journal` (privée) — paramètres : `action` (AuditLog.ActionType), `user` (User), `ip` (chaîne de caractères), `details` (chaîne de caractères), `success` (booléen) ; retourne : aucune valeur
    private void journal(AuditLog.ActionType action, User user, String ip, String details, boolean success) {
        // Déclare la variable `logItem` (AuditLog) initialisée avec une nouvelle instance de AuditLog
        AuditLog logItem = new AuditLog();
        // Renseigne la propriété ActionType de `logItem` avec la valeur de action
        logItem.setActionType(action);
        // Renseigne la propriété User de `logItem` avec la valeur de user
        logItem.setUser(user);
        // Renseigne la propriété IpAddress de `logItem` avec la valeur de ip
        logItem.setIpAddress(ip);
        // Renseigne la propriété Details de `logItem` avec la valeur de details
        logItem.setDetails(details);
        // Renseigne la propriété Success de `logItem` avec la valeur de success
        logItem.setSuccess(success);
        // Enregistre logItem en base de données via auditLogRepository
        auditLogRepository.save(logItem);
    }

    // Méthode `journalEchec` (privée) — paramètres : `telephone` (chaîne de caractères), `ip` (chaîne de caractères), `details` (chaîne de caractères) ; retourne : aucune valeur
    private void journalEchec(String telephone, String ip, String details) {
        // Déclare la variable `logItem` (AuditLog) initialisée avec une nouvelle instance de AuditLog
        AuditLog logItem = new AuditLog();
        // Renseigne la propriété ActionType de `logItem` avec la valeur de AuditLog.ActionType.ECHEC_CONNEXION
        logItem.setActionType(AuditLog.ActionType.ECHEC_CONNEXION);
        // Renseigne la propriété TelephoneTente de `logItem` avec la valeur de telephone
        logItem.setTelephoneTente(telephone);
        // Renseigne la propriété IpAddress de `logItem` avec la valeur de ip
        logItem.setIpAddress(ip);
        // Renseigne la propriété Details de `logItem` avec la valeur de details
        logItem.setDetails(details);
        // Renseigne la propriété Success de `logItem` avec le booléen faux
        logItem.setSuccess(false);
        // Enregistre logItem en base de données via auditLogRepository
        auditLogRepository.save(logItem);
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
}
