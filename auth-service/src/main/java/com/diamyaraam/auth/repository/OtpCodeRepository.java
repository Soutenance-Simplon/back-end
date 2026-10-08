// Déclaration du package Java : `com.diamyaraam.auth.repository`
package com.diamyaraam.auth.repository;

// Import de la classe `OtpCode` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.OtpCode;
// Import de la classe `OtpType` (paquet com.diamyaraam.auth.entity.OtpCode)
import com.diamyaraam.auth.entity.OtpCode.OtpType;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Modifying` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Modifying;
// Import de la classe `Query` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Query;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `OtpCodeRepository` : repository Spring Data JPA de l'entité `OtpCode` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    // Dernier OTP valide pour ce téléphone
    Optional<OtpCode> findTopByTelephoneAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(
        // Paramètre `otpType` de type String telephone, OtpType
        String telephone, OtpType otpType
    );

    // Invalider les anciens OTP avant d'en générer un nouveau
    @Modifying
    // Requête JPQL/SQL personnalisée : UPDATE OtpCode o SET o.used = true WHERE o.telephone = :tel AND o.otp…
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.telephone = :tel AND o.otpType = :type AND o.used = false")
    // Méthode abstraite (contrat) `invalidateAll` — paramètres : `tel` (chaîne de caractères), `type` (OtpType) ; retourne : aucune valeur
    void invalidateAll(String tel, OtpType type);

    // RM027 — Nettoyer les OTP expirés (tâche planifiée)
    @Modifying
    // Requête JPQL/SQL personnalisée : DELETE FROM OtpCode o WHERE o.expiresAt < :now
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    // Méthode abstraite (contrat) `deleteExpired` — paramètres : `now` (date-heure) ; retourne : aucune valeur ; intention : supprime (delete expired)
    void deleteExpired(LocalDateTime now);
}
