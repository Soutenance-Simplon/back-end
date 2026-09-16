package com.diamyaraam.auth.service;

import com.diamyaraam.auth.dto.AuthResponse;
import com.diamyaraam.auth.dto.LoginRequest;
import com.diamyaraam.auth.dto.RegisterPatientRequest;
import com.diamyaraam.auth.entity.AuditLog;
import com.diamyaraam.auth.entity.OtpCode;
import com.diamyaraam.auth.entity.Role;
import com.diamyaraam.auth.entity.User;
import com.diamyaraam.auth.repository.AuditLogRepository;
import com.diamyaraam.auth.repository.OtpCodeRepository;
import com.diamyaraam.auth.repository.RoleRepository;
import com.diamyaraam.auth.repository.UserRepository;
import com.diamyaraam.auth.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    @Value("${auth.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            OtpCodeRepository otpCodeRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public void registerPatient(RegisterPatientRequest req) {
        if (userRepository.existsByTelephone(req.getTelephone())) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
        }

        if (req.getEmail() != null && userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà associé à un compte.");
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        Role rolePatient = roleRepository.findByNomRole("PATIENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "PATIENT")));

        User user = new User();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setTelephone(req.getTelephone());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(rolePatient);
        user.setAccountStatus(User.AccountStatus.EN_ATTENTE);

        userRepository.save(user);

        otpService.sendOtp(req.getTelephone(), OtpCode.OtpType.VERIFICATION_TELEPHONE);
        journal(AuditLog.ActionType.CREATION_COMPTE, user, null, "Inscription patient", true);

        log.info("Compte patient créé : {} (statut EN_ATTENTE)", req.getTelephone());
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String ipAddress) {
        User user = userRepository.findByTelephone(req.getTelephone())
                .orElseThrow(() -> {
                    journalEchec(req.getTelephone(), ipAddress, "Numéro introuvable");
                    return new IllegalArgumentException("Numéro ou mot de passe incorrect.");
                });

        if (User.AccountStatus.EN_ATTENTE.equals(user.getAccountStatus())) {
            throw new IllegalStateException("Votre compte n'est pas encore activé. Vérifiez votre téléphone.");
        }

        if (User.AccountStatus.SUSPENDU.equals(user.getAccountStatus())) {
            throw new IllegalStateException("Votre compte est suspendu. Contactez l'administrateur.");
        }

        if (user.isAccountLocked()) {
            journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Compte bloqué", false);
            throw new IllegalStateException("Compte bloqué jusqu'à " + user.getLockedUntil() + ". Trop de tentatives.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            userRepository.incrementFailedAttempts(user.getId());
            int newCount = user.getFailedLoginAttempts() + 1;

            if (newCount >= maxLoginAttempts) {
                user.setAccountStatus(User.AccountStatus.BLOQUE);
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
                userRepository.save(user);
                journal(AuditLog.ActionType.COMPTE_BLOQUE, user, ipAddress, "Bloqué après " + newCount + " tentatives", false);
                throw new IllegalStateException("Compte bloqué pour " + lockoutDurationMinutes + " minutes.");
            }

            journal(AuditLog.ActionType.ECHEC_CONNEXION, user, ipAddress, "Tentative " + newCount + "/" + maxLoginAttempts, false);
            throw new IllegalArgumentException("Numéro ou mot de passe incorrect. (" + newCount + "/" + maxLoginAttempts + " tentatives)");
        }

        userRepository.resetFailedAttempts(user.getId());

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        journal(AuditLog.ActionType.CONNEXION_REUSSIE, user, ipAddress, "Login OK", true);
        log.info("Login réussi : {}", user.getTelephone());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getExpirationMs() / 1000);
        response.setUserId(user.getId());
        response.setTelephone(user.getTelephone());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole() != null ? user.getRole().getNomRole() : "");
        response.setAccountStatus(user.getAccountStatus().name());

        return response;
    }

    @Transactional(readOnly = true)
    public User searchUserByTelephone(String telephone) {
        return userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur trouvé avec ce numéro."));
    }

    @Transactional
    public void forgotPassword(String telephone) {
        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new IllegalArgumentException("Aucun utilisateur associé à ce numéro de téléphone."));

        otpService.sendOtp(user.getTelephone(), OtpCode.OtpType.REINITIALISATION_MDP);
        log.info("Demande de réinitialisation de mot de passe envoyée pour : {}", telephone);
    }

    @Transactional
    public void resetPassword(String telephone, String code, String newPassword, String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas.");
        }

        boolean validOtp = otpService.verifyOtp(telephone, code, OtpCode.OtpType.REINITIALISATION_MDP);
        if (!validOtp) {
            throw new IllegalArgumentException("Code OTP invalide ou expiré.");
        }

        User user = userRepository.findByTelephone(telephone)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        journal(AuditLog.ActionType.ACTIVATION_COMPTE, user, null, "Mot de passe réinitialisé", true);
        log.info("Mot de passe réinitialisé avec succès pour : {}", telephone);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré.");
        }

        String userIdStr = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(java.util.UUID.fromString(userIdStr))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtService.getExpirationMs() / 1000);
        response.setUserId(user.getId());
        response.setTelephone(user.getTelephone());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole() != null ? user.getRole().getNomRole() : "");
        response.setAccountStatus(user.getAccountStatus().name());

        return response;
    }

    private void journal(AuditLog.ActionType action, User user, String ip, String details, boolean success) {
        AuditLog logItem = new AuditLog();
        logItem.setActionType(action);
        logItem.setUser(user);
        logItem.setIpAddress(ip);
        logItem.setDetails(details);
        logItem.setSuccess(success);
        auditLogRepository.save(logItem);
    }

    private void journalEchec(String telephone, String ip, String details) {
        AuditLog logItem = new AuditLog();
        logItem.setActionType(AuditLog.ActionType.ECHEC_CONNEXION);
        logItem.setTelephoneTente(telephone);
        logItem.setIpAddress(ip);
        logItem.setDetails(details);
        logItem.setSuccess(false);
        auditLogRepository.save(logItem);
    }
}
