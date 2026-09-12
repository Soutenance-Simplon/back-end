package com.diamyaraam.auth.repository;

import com.diamyaraam.auth.entity.User;
import com.diamyaraam.auth.entity.User.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // RM021 — Login par téléphone
    Optional<User> findByTelephone(String telephone);
    Optional<User> findByEmail(String email);

    // RM003/RM010 — Unicité
    boolean existsByTelephone(String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephoneOrEmail(String telephone, String email);

    // RM024 — Incrémenter le compteur d'échecs
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    void incrementFailedAttempts(UUID id);

    // RM024 — Réinitialiser après succès
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :id")
    void resetFailedAttempts(UUID id);

    // RM019 — par statut
    java.util.List<User> findByAccountStatus(AccountStatus status);
}
