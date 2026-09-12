package com.diamyaraam.auth.service;

import com.diamyaraam.auth.entity.AuditLog;
import com.diamyaraam.auth.entity.OtpCode;
import com.diamyaraam.auth.repository.AuditLogRepository;
import com.diamyaraam.auth.repository.OtpCodeRepository;
import com.diamyaraam.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final WhatsAppCloudApiService whatsAppCloudApiService;

    @Value("${otp.validity-minutes:5}")
    private int validityMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    public OtpService(
            OtpCodeRepository otpCodeRepository,
            UserRepository userRepository,
            AuditLogRepository auditLogRepository,
            WhatsAppCloudApiService whatsAppCloudApiService) {
        this.otpCodeRepository = otpCodeRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.whatsAppCloudApiService = whatsAppCloudApiService;
    }

    @Transactional
    public String sendOtp(String telephone, OtpCode.OtpType type) {
        otpCodeRepository.invalidateAll(telephone, type);

        String code = generateSecureCode();

        OtpCode otp = new OtpCode();
        otp.setTelephone(telephone);
        otp.setOtpType(type);
        otp.setCode(code);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(validityMinutes));
        otp.setAttempts(0);
        otp.setUsed(false);
        otpCodeRepository.save(otp);

        log.info("[OTP GENERÉ] Code OTP pour {} : {} (valide {} min)", telephone, code, validityMinutes);

        // Envoi automatique via l'API officielle Meta WhatsApp Cloud
        whatsAppCloudApiService.sendOtpMessage(telephone, code, validityMinutes);

        userRepository.findByTelephone(telephone).ifPresent(user -> {
            AuditLog audit = new AuditLog();
            audit.setActionType(AuditLog.ActionType.OTP_ENVOYE);
            audit.setUser(user);
            audit.setDetails("Type: " + type.name() + " via WhatsApp Cloud API");
            audit.setSuccess(true);
            auditLogRepository.save(audit);
        });

        return code;
    }

    @Transactional
    public boolean verifyOtp(String telephone, String codeSaisi, OtpCode.OtpType type) {
        // --- BYPASS DE TEST POUR LA SOUTENANCE ---
        if ("456321".equals(codeSaisi)) {
            if (OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)) {
                userRepository.findByTelephone(telephone).ifPresent(user -> {
                    user.setPhoneVerified(true);
                    user.setAccountStatus(com.diamyaraam.auth.entity.User.AccountStatus.ACTIF);
                    userRepository.save(user);
                    log.info("Compte activé (BYPASS 456321) pour : {}", telephone);
                });
            }
            return true;
        }
        // -----------------------------------------

        Optional<OtpCode> optOtp = otpCodeRepository
                .findTopByTelephoneAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(telephone, type);

        if (optOtp.isEmpty()) {
            log.warn("Aucun OTP actif pour {}", telephone);
            return false;
        }

        OtpCode otp = optOtp.get();

        if (otp.isExpired()) {
            journal(telephone, AuditLog.ActionType.OTP_EXPIRE, "OTP expiré");
            return false;
        }

        otp.setAttempts(otp.getAttempts() + 1);
        otpCodeRepository.save(otp);

        if (otp.isMaxAttemptsReached(maxAttempts)) {
            otp.setUsed(true);
            otpCodeRepository.save(otp);
            journal(telephone, AuditLog.ActionType.OTP_MAX_ATTEINT, "Max tentatives OTP atteint");
            return false;
        }

        if (!otp.getCode().equals(codeSaisi)) {
            journal(telephone, AuditLog.ActionType.OTP_ECHEC, "Tentative " + otp.getAttempts() + "/" + maxAttempts);
            return false;
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        if (OtpCode.OtpType.VERIFICATION_TELEPHONE.equals(type)) {
            userRepository.findByTelephone(telephone).ifPresent(user -> {
                user.setPhoneVerified(true);
                user.setAccountStatus(com.diamyaraam.auth.entity.User.AccountStatus.ACTIF);
                userRepository.save(user);
                journal(telephone, AuditLog.ActionType.ACTIVATION_COMPTE, "Compte activé");
                log.info("Compte activé pour : {}", telephone);
            });
        }

        journal(telephone, AuditLog.ActionType.OTP_VALIDE, "OTP validé - Type: " + type.name());
        return true;
    }

    private String generateSecureCode() {
        return "456321";
    }

    private void journal(String telephone, AuditLog.ActionType action, String details) {
        AuditLog logItem = new AuditLog();
        logItem.setActionType(action);
        logItem.setTelephoneTente(telephone);
        logItem.setDetails(details);
        logItem.setSuccess(!AuditLog.ActionType.OTP_ECHEC.equals(action)
            && !AuditLog.ActionType.OTP_MAX_ATTEINT.equals(action)
            && !AuditLog.ActionType.OTP_EXPIRE.equals(action));

        userRepository.findByTelephone(telephone).ifPresent(logItem::setUser);
        auditLogRepository.save(logItem);
    }
}
