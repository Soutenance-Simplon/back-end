// Déclaration du package Java : `com.diamyaraam.auth.repository`
package com.diamyaraam.auth.repository;

// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `AccountStatus` (paquet com.diamyaraam.auth.entity.User)
import com.diamyaraam.auth.entity.User.AccountStatus;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Modifying` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Modifying;
// Import de la classe `Query` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Query;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `UserRepository` : repository Spring Data JPA de l'entité `User` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface UserRepository extends JpaRepository<User, UUID> {

    // RM021 — Login par téléphone
    Optional<User> findByTelephone(String telephone);
    // Méthode abstraite (contrat) `findByEmail` — paramètres : `email` (chaîne de caractères) ; retourne : valeur optionnelle de User ; intention : recherche par (find by email)
    Optional<User> findByEmail(String email);

    // RM003/RM010 — Unicité
    boolean existsByTelephone(String telephone);
    // Méthode abstraite (contrat) `existsByEmail` — paramètres : `email` (chaîne de caractères) ; retourne : booléen ; intention : teste l'existence de (exists by email)
    boolean existsByEmail(String email);
    // Méthode abstraite (contrat) `existsByTelephoneOrEmail` — paramètres : `telephone` (chaîne de caractères), `email` (chaîne de caractères) ; retourne : booléen ; intention : teste l'existence de (exists by telephone or email)
    boolean existsByTelephoneOrEmail(String telephone, String email);

    // RM024 — Incrémenter le compteur d'échecs
    @Modifying
    // Requête JPQL/SQL personnalisée : UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 W…
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :id")
    // Méthode abstraite (contrat) `incrementFailedAttempts` — paramètres : `id` (identifiant UUID) ; retourne : aucune valeur
    void incrementFailedAttempts(UUID id);

    // RM024 — Réinitialiser après succès
    @Modifying
    // Requête JPQL/SQL personnalisée : UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHE…
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :id")
    // Méthode abstraite (contrat) `resetFailedAttempts` — paramètres : `id` (identifiant UUID) ; retourne : aucune valeur ; intention : réinitialise (reset failed attempts)
    void resetFailedAttempts(UUID id);

    // RM019 — par statut
    java.util.List<User> findByAccountStatus(AccountStatus status);
}
