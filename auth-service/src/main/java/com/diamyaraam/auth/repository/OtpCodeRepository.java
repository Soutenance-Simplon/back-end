package com.diamyaraam.auth.repository;

import com.diamyaraam.auth.entity.OtpCode;
import com.diamyaraam.auth.entity.OtpCode.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    // Dernier OTP valide pour ce téléphone
    Optional<OtpCode> findTopByTelephoneAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
        String telephone, OtpType otpType
    );

    // Invalider les anciens OTP avant d'en générer un nouveau
    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.telephone = :tel AND o.otpType = :type AND o.used = false")
    void invalidateAll(String tel, OtpType type);

    // RM027 — Nettoyer les OTP expirés (tâche planifiée)
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
