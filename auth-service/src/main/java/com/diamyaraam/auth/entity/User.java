package com.diamyaraam.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITÉ : User — auth-service
 *
 * RM002 — UUID auto-généré
 * RM019 — 5 statuts : EN_ATTENTE, ACTIF, SUSPENDU, BLOQUE, SUPPRIME
 * RM021 — Login par téléphone
 * RM023 — Mot de passe haché (BCrypt)
 * RM024 — Compteur d'échecs de connexion + lockedUntil
 */
@Entity
@Table(
    name = "users",
    schema = "auth_schema",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "telephone"),
        @UniqueConstraint(columnNames = "email")
    }
)
public class User {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 150)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Pattern(
        regexp = "^\\+221\\d{9}$",
        message = "Format : +221XXXXXXXXX"
    )
    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    public enum Genre { M, F }

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Genre genre;

    @Column(name = "photo_profil", columnDefinition = "TEXT")
    private String photoProfil;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    public enum AccountStatus {
        EN_ATTENTE, ACTIF, SUSPENDU, BLOQUE, SUPPRIME
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 15, nullable = false)
    private AccountStatus accountStatus = AccountStatus.EN_ATTENTE;

    @Column(name = "phone_verified", nullable = false)
    private Boolean phoneVerified = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "is_staff", nullable = false)
    private Boolean isStaff = false;

    @Column(name = "is_superuser", nullable = false)
    private Boolean isSuperuser = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {}

    public boolean isAccountLocked() {
        if (!AccountStatus.BLOQUE.equals(this.accountStatus)) return false;
        if (this.lockedUntil == null) return true;
        return LocalDateTime.now().isBefore(this.lockedUntil);
    }

    public boolean isActive() {
        return AccountStatus.ACTIF.equals(this.accountStatus);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public Boolean getIsStaff() { return isStaff; }
    public void setIsStaff(Boolean isStaff) { this.isStaff = isStaff; }

    public Boolean getIsSuperuser() { return isSuperuser; }
    public void setIsSuperuser(Boolean isSuperuser) { this.isSuperuser = isSuperuser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
