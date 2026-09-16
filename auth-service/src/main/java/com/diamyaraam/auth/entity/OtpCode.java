package com.diamyaraam.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OTP stocké en BDD (RM026-RM028)
 */
@Entity
@Table(name = "otp_code", schema = "auth_schema")
public class OtpCode {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    public enum OtpType {
        VERIFICATION_TELEPHONE,
        REINITIALISATION_MDP,
        DOUBLE_AUTHENTIFICATION
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", length = 30, nullable = false)
    private OtpType otpType;

    @Column(nullable = false, length = 20)
    private String telephone;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private Boolean used = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public OtpCode() {}

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isMaxAttemptsReached(int max) {
        return this.attempts >= max;
    }

    public boolean isValid(int max) {
        return !this.used && !isExpired() && !isMaxAttemptsReached(max);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OtpType getOtpType() { return otpType; }
    public void setOtpType(OtpType otpType) { this.otpType = otpType; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
