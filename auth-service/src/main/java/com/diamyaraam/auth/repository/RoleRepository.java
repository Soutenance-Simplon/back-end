// Déclaration du package Java : `com.diamyaraam.auth.repository`
package com.diamyaraam.auth.repository;

// Import de la classe `Role` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.Role;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `RoleRepository` : repository Spring Data JPA de l'entité `Role` (clé primaire `Long`) — les opérations CRUD sont générées automatiquement
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Méthode abstraite (contrat) `findByNomRole` — paramètres : `nomRole` (chaîne de caractères) ; retourne : valeur optionnelle de Role ; intention : recherche par (find by nom role)
    Optional<Role> findByNomRole(String nomRole);
    // Méthode abstraite (contrat) `existsByNomRole` — paramètres : `nomRole` (chaîne de caractères) ; retourne : booléen ; intention : teste l'existence de (exists by nom role)
    boolean existsByNomRole(String nomRole);
}
