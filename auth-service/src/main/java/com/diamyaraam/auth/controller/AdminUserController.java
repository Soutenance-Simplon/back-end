// Déclaration du package Java : `com.diamyaraam.auth.controller`
package com.diamyaraam.auth.controller;

// Import de la classe `Role` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.Role;
// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `AuditLogRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.AuditLogRepository;
// Import de la classe `RoleRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.RoleRepository;
// Import de la classe `UserRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.UserRepository;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de la classe `PasswordEncoder` (paquet org.springframework.security.crypto.password)
import org.springframework.security.crypto.password.PasswordEncoder;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de toutes les classes du paquet `java.util`
import java.util.*;
// Import de la classe `Collectors` (paquet java.util.stream)
import java.util.stream.Collectors;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST D'ADMINISTRATION : GESTION DES UTILISATEURS & RBAC (ADMIN USER CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & SÉCURITÉ POUR LA SOUTENANCE :
 * Ce contrôleur est le centre névralgique de gestion des identités consommé exclusivement par le Dashboard Web Admin.
 * 
 * 🛡️ CONTRÔLE D'ACCÈS RBAC (ROLE-BASED ACCESS CONTROL) :
 * Les requêtes adressées à `/auth/admin/**` (exposées via la Gateway sur `/api/auth/admin/**`)
 * exigent impérativement le rôle `ROLE_ADMIN` validé par le filtre JWT réactif de l'API Gateway.
 * Aucun patient ni médecin standard ne peut exécuter ces opérations.
 * 
 * 💼 FONCTIONNALITÉS MÉTIERS D'ADMINISTRATION :
 * - Lister et filtrer les utilisateurs (par recherche textuelle, rôle, état du compte).
 * - Modérer et suspendre/bloquer des comptes en cas d'activité suspecte ou de fraude.
 * - Réinitialiser le mot de passe d'un utilisateur ou déverrouiller un compte bloqué par trop d'échecs.
 * - Création manuelle d'utilisateurs ou praticiens de santé.
 * - Suppression sécurisée préservant la piste d'audit légale (Anonymisation des AuditLogs).
 * - Fourniture d'indicateurs clés de performance (KPIs statistiques) pour le tableau de bord d'accueil.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /auth/admin »
@RequestMapping("/auth/admin")
// Déclaration de la classe `AdminUserController` (rôle : expose des routes HTTP)
public class AdminUserController {

    /** Répertoire Spring Data JPA pour les opérations CRUD sur la table users */
    private final UserRepository userRepository;

    /** Répertoire Spring Data JPA pour la gestion des rôles (table roles) */
    private final RoleRepository roleRepository;

    /** Encodeur cryptographique BCrypt pour hacher les mots de passe de manière irréversible */
    private final PasswordEncoder passwordEncoder;

    /** Répertoire pour enregistrer et auditer les modifications administratives sensibles */
    private final AuditLogRepository auditLogRepository;

    /**
     * Constructeur avec injection automatique des dépendances Spring.
     */
    public AdminUserController(UserRepository userRepository,
                               // Paramètre `roleRepository` de type RoleRepository
                               RoleRepository roleRepository,
                               // Paramètre `passwordEncoder` de type PasswordEncoder
                               PasswordEncoder passwordEncoder,
                               // Paramètre `auditLogRepository` de type AuditLogRepository
                               AuditLogRepository auditLogRepository) {
        // Initialise l'attribut `userRepository` avec la valeur de userRepository
        this.userRepository = userRepository;
        // Initialise l'attribut `roleRepository` avec la valeur de roleRepository
        this.roleRepository = roleRepository;
        // Initialise l'attribut `passwordEncoder` avec la valeur de passwordEncoder
        this.passwordEncoder = passwordEncoder;
        // Initialise l'attribut `auditLogRepository` avec la valeur de auditLogRepository
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Méthode utilitaire de projection : convertit l'entité User en Map JSON propre et sécurisée.
     * Masque le mot de passe haché (passwordHash) pour éviter toute fuite vers l'interface web.
     * 
     * @param u Entité User
     * @return Map des attributs non sensibles de l'utilisateur
     */
    private Map<String, Object> toUserMap(User u) {
        // Déclare la variable `map` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de LinkedHashMap<>
        Map<String, Object> map = new LinkedHashMap<>();
        // Appelle la méthode `put` sur `map` : map.put("id", u.getId());
        map.put("id", u.getId());
        // Appelle la méthode `put` sur `map` : map.put("firstName", u.getFirstName());
        map.put("firstName", u.getFirstName());
        // Appelle la méthode `put` sur `map` : map.put("lastName", u.getLastName());
        map.put("lastName", u.getLastName());
        // Appelle la méthode `put` sur `map` : map.put("telephone", u.getTelephone());
        map.put("telephone", u.getTelephone());
        // Appelle la méthode `put` sur `map` : map.put("email", u.getEmail());
        map.put("email", u.getEmail());
        // Appelle la méthode `put` sur `map` : map.put("dateNaissance", u.getDateNaissance());
        map.put("dateNaissance", u.getDateNaissance());
        // Appelle la méthode `put` sur `map` : map.put("genre", u.getGenre() != null ? u.getGenre().name() : null);
        map.put("genre", u.getGenre() != null ? u.getGenre().name() : null);
        // Appelle la méthode `put` sur `map` : map.put("photoProfil", u.getPhotoProfil());
        map.put("photoProfil", u.getPhotoProfil());
        // Appelle la méthode `put` sur `map` : map.put("role", u.getRole() != null ? u.getRole().getNomRole() : "PATIENT"…
        map.put("role", u.getRole() != null ? u.getRole().getNomRole() : "PATIENT");
        // Appelle la méthode `put` sur `map` : map.put("accountStatus", u.getAccountStatus() != null ? u.getAccountStatus…
        map.put("accountStatus", u.getAccountStatus() != null ? u.getAccountStatus().name() : "ACTIF");
        // Appelle la méthode `put` sur `map` : map.put("phoneVerified", u.getPhoneVerified() != null ? u.getPhoneVerified…
        map.put("phoneVerified", u.getPhoneVerified() != null ? u.getPhoneVerified() : false);
        // Appelle la méthode `put` sur `map` : map.put("failedLoginAttempts", u.getFailedLoginAttempts() != null ? u.getF…
        map.put("failedLoginAttempts", u.getFailedLoginAttempts() != null ? u.getFailedLoginAttempts() : 0);
        // Appelle la méthode `put` sur `map` : map.put("lockedUntil", u.getLockedUntil());
        map.put("lockedUntil", u.getLockedUntil());
        // Appelle la méthode `put` sur `map` : map.put("isStaff", u.getIsStaff() != null ? u.getIsStaff() : false);
        map.put("isStaff", u.getIsStaff() != null ? u.getIsStaff() : false);
        // Appelle la méthode `put` sur `map` : map.put("isSuperuser", u.getIsSuperuser() != null ? u.getIsSuperuser() : f…
        map.put("isSuperuser", u.getIsSuperuser() != null ? u.getIsSuperuser() : false);
        // Appelle la méthode `put` sur `map` : map.put("createdAt", u.getCreatedAt());
        map.put("createdAt", u.getCreatedAt());
        // Appelle la méthode `put` sur `map` : map.put("updatedAt", u.getUpdatedAt());
        map.put("updatedAt", u.getUpdatedAt());
        // Retourne la valeur de map
        return map;
    }

    /**
     * ENDPOINT : Récupération paginée et filtrée de la liste complète des utilisateurs
     * GET /auth/admin/users?search=...&role=...&status=...
     * 
     * @param search Terme de recherche textuelle (nom, prénom, téléphone, email)
     * @param role Filtre sur le rôle (PATIENT, MEDECIN, ADMIN)
     * @param status Filtre sur l'état (ACTIF, SUSPENDU, BLOQUE, EN_ATTENTE)
     * @return Liste ordonnée par date de création descendante
     */
    @GetMapping("/users")
    // Méthode `getAllUsers` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de dictionnaire clé/valeur ; intention : récupère (get all users)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers(
            // Paramètre `search` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String search,
            // Paramètre `role` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String role,
            // Paramètre `status` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String status) {

        // Déclare la variable `users` (liste de User) initialisée avec le résultat de la requête findAll exécutée via userRepository
        List<User> users = userRepository.findAll();


        // Condition : exécute le bloc suivant seulement si `search != null && !search.trim().isEmpty()`
        if (search != null && !search.trim().isEmpty()) {
            // Déclare la variable `q` (chaîne de caractères) initialisée avec `search.trim().toLowerCase()`
            String q = search.trim().toLowerCase();
            // Affecte à `users` `users.stream().filter(u ->`
            users = users.stream().filter(u ->
                // Suite de l'instruction précédente : (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q)) ||
                (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q)) ||
                // Suite de l'instruction précédente : (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)) ||
                (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)) ||
                // Suite de l'instruction précédente : (u.getTelephone() != null && u.getTelephone().toLowerCase().contains(q)) ||
                (u.getTelephone() != null && u.getTelephone().toLowerCase().contains(q)) ||
                // Suite de l'instruction précédente : (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
            // Enchaînement : appelle `collect(Collectors.toList());`
            ).collect(Collectors.toList());
        }

        // Condition : exécute le bloc suivant seulement si `role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("TOUS")`
        if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("TOUS")) {
            // Affecte à `users` `users.stream().filter(u ->`
            users = users.stream().filter(u ->
                // Argument/valeur : `u.getRole() != null && u.getRole().getNomRole().equalsIgnoreCase(role.trim(`
                u.getRole() != null && u.getRole().getNomRole().equalsIgnoreCase(role.trim())
            // Enchaînement : appelle `collect(Collectors.toList());`
            ).collect(Collectors.toList());
        }

        // Condition : exécute le bloc suivant seulement si `status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("TOUS")`
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("TOUS")) {
            // Affecte à `users` `users.stream().filter(u ->`
            users = users.stream().filter(u ->
                // Argument/valeur : `u.getAccountStatus() != null && u.getAccountStatus().name().equalsIgnoreCase(st…`
                u.getAccountStatus() != null && u.getAccountStatus().name().equalsIgnoreCase(status.trim())
            // Enchaînement : appelle `collect(Collectors.toList());`
            ).collect(Collectors.toList());
        }

        // Trier par date de création descendante
        users.sort((a, b) -> {
            // Condition : exécute le bloc suivant seulement si `a.getCreatedAt() == null) return 1;`
            if (a.getCreatedAt() == null) return 1;
            // Condition : exécute le bloc suivant seulement si `b.getCreatedAt() == null) return -1;`
            if (b.getCreatedAt() == null) return -1;
            // Retourne `b.getCreatedAt().compareTo(a.getCreatedAt())`
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Déclare la variable `result` (liste de dictionnaire clé/valeur) initialisée avec `users.stream().map(this::toUserMap).collect(Collectors.toList())`
        List<Map<String, Object>> result = users.stream().map(this::toUserMap).collect(Collectors.toList());
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs", resul…
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs", result));
    }

    // Route HTTP GET sur le chemin « /users/{id} »
    @GetMapping("/users/{id}")
    // Méthode `getUserById` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : récupère (get user by id)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable UUID id) {
        // Retourne le résultat de la requête findById exécutée via userRepository
        return userRepository.findById(id)
                // Enchaînement : appelle `map(u -> ResponseEntity.ok(ApiResponse.success("Détai…`
                .map(u -> ResponseEntity.ok(ApiResponse.success("Détails utilisateur", toUserMap(u))))
                // Enchaînement : appelle `orElseGet(() -> ResponseEntity.status(404).body(ApiResponse…`
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable")));
    }

    // Route HTTP PUT sur le chemin « /users/{id}/status »
    @PutMapping("/users/{id}/status")
    // Méthode `updateUserStatus` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : met à jour (update user status)
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserStatus(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `status` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String status,
            // Paramètre `body` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody(required = false) Map<String, String> body) {

        // Déclare la variable `newStatus` (chaîne de caractères) initialisée avec la valeur de status
        String newStatus = status;
        // Condition : exécute le bloc suivant seulement si `newStatus == null && body != null`
        if (newStatus == null && body != null) {
            // Affecte à `newStatus` `body.get("status") != null ? body.get("status") : body.get("accountStatus")`
            newStatus = body.get("status") != null ? body.get("status") : body.get("accountStatus");
        }
        // Condition : exécute le bloc suivant seulement si `newStatus == null`
        if (newStatus == null) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Statut requis (ACTIF, SUSPE…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Statut requis (ACTIF, SUSPENDU, BLOQUE, EN_ATTENTE)"));
        }

        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        // Déclare la variable `user` (User) initialisée avec `opt.get()`
        User user = opt.get();
        // Renseigne la propriété AccountStatus de `user` avec `User.AccountStatus.valueOf(newStatus.toUpperCase())`
        user.setAccountStatus(User.AccountStatus.valueOf(newStatus.toUpperCase()));
        // Condition : exécute le bloc suivant seulement si `user.getAccountStatus() == User.AccountStatus.ACTIF`
        if (user.getAccountStatus() == User.AccountStatus.ACTIF) {
            // Renseigne la propriété FailedLoginAttempts de `user` avec la valeur numérique 0
            user.setFailedLoginAttempts(0);
            // Renseigne la propriété LockedUntil de `user` avec la valeur nulle (absence de valeur)
            user.setLockedUntil(null);
        }
        // Déclare la variable `saved` (User) initialisée avec l'enregistrement en base de user via userRepository
        User saved = userRepository.save(user);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès"…
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", toUserMap(saved)));
    }

    // Route HTTP PUT sur le chemin « /users/{id}/role »
    @PutMapping("/users/{id}/role")
    // Méthode `updateUserRole` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : met à jour (update user role)
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserRole(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `role` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String role,
            // Paramètre `body` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody(required = false) Map<String, String> body) {

        // Déclare la variable `newRoleStr` (chaîne de caractères) initialisée avec la valeur de role
        String newRoleStr = role;
        // Condition : exécute le bloc suivant seulement si `newRoleStr == null && body != null`
        if (newRoleStr == null && body != null) {
            // Affecte à `newRoleStr` `body.get("role") != null ? body.get("role") : body.get("nomRole")`
            newRoleStr = body.get("role") != null ? body.get("role") : body.get("nomRole");
        }
        // Condition : exécute le bloc suivant seulement si `newRoleStr == null`
        if (newRoleStr == null) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Rôle requis (PATIENT, MEDEC…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Rôle requis (PATIENT, MEDECIN, ADMIN)"));
        }

        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        // Déclare la variable `finalRole` (chaîne de caractères) initialisée avec `newRoleStr.toUpperCase()`
        final String finalRole = newRoleStr.toUpperCase();
        // Déclare la variable `r` (Role) initialisée avec le résultat de la requête findByNomRole exécutée via roleRepository
        Role r = roleRepository.findByNomRole(finalRole)
                // Enchaînement : appelle `orElseGet(() -> roleRepository.save(new Role(null, finalRol…`
                .orElseGet(() -> roleRepository.save(new Role(null, finalRole)));

        // Déclare la variable `user` (User) initialisée avec `opt.get()`
        User user = opt.get();
        // Renseigne la propriété Role de `user` avec la valeur de r
        user.setRole(r);
        // Condition : exécute le bloc suivant seulement si `"ADMIN".equalsIgnoreCase(finalRole)`
        if ("ADMIN".equalsIgnoreCase(finalRole)) {
            // Renseigne la propriété IsStaff de `user` avec le booléen vrai
            user.setIsStaff(true);
            // Renseigne la propriété IsSuperuser de `user` avec le booléen vrai
            user.setIsSuperuser(true);
        }
        // Déclare la variable `saved` (User) initialisée avec l'enregistrement en base de user via userRepository
        User saved = userRepository.save(user);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Rôle mis à jour avec succès", …
        return ResponseEntity.ok(ApiResponse.success("Rôle mis à jour avec succès", toUserMap(saved)));
    }

    // Route HTTP PUT sur le chemin « /users/{id}/unlock »
    @PutMapping("/users/{id}/unlock")
    // Méthode `unlockUser` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlockUser(@PathVariable UUID id) {
        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        // Déclare la variable `user` (User) initialisée avec `opt.get()`
        User user = opt.get();
        // Renseigne la propriété FailedLoginAttempts de `user` avec la valeur numérique 0
        user.setFailedLoginAttempts(0);
        // Renseigne la propriété LockedUntil de `user` avec la valeur nulle (absence de valeur)
        user.setLockedUntil(null);
        // Renseigne la propriété AccountStatus de `user` avec la valeur de User.AccountStatus.ACTIF
        user.setAccountStatus(User.AccountStatus.ACTIF);
        // Déclare la variable `saved` (User) initialisée avec l'enregistrement en base de user via userRepository
        User saved = userRepository.save(user);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Compte débloqué et réactivé", …
        return ResponseEntity.ok(ApiResponse.success("Compte débloqué et réactivé", toUserMap(saved)));
    }

    // Route HTTP PUT sur le chemin « /users/{id}/password »
    @PutMapping("/users/{id}/password")
    // Méthode `changePassword` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void
    public ResponseEntity<ApiResponse<Void>> changePassword(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `payload` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody Map<String, String> payload,
            // Paramètre `httpRequest` de type jakarta.servlet.http.HttpServletRequest
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Déclare la variable `ancienMotDePasse` (chaîne de caractères) initialisée avec `payload.get("ancienMotDePasse")`
        String ancienMotDePasse = payload.get("ancienMotDePasse");
        // Déclare la variable `nouveauMotDePasse` (chaîne de caractères) initialisée avec `payload.get("nouveauMotDePasse")`
        String nouveauMotDePasse = payload.get("nouveauMotDePasse");
        // Déclare la variable `confirmationMotDePasse` (chaîne de caractères) initialisée avec `payload.get("confirmationMotDePasse")`
        String confirmationMotDePasse = payload.get("confirmationMotDePasse");

        // Condition : exécute le bloc suivant seulement si `nouveauMotDePasse == null || nouveauMotDePasse.trim().length() < 6`
        if (nouveauMotDePasse == null || nouveauMotDePasse.trim().length() < 6) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Le nouveau mot de passe doi…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Le nouveau mot de passe doit comporter au moins 6 caractères."));
        }

        // Condition : exécute le bloc suivant seulement si `confirmationMotDePasse != null && !nouveauMotDePasse.equals(confirmationMotDePasse)`
        if (confirmationMotDePasse != null && !nouveauMotDePasse.equals(confirmationMotDePasse)) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("La confirmation du mot de p…`
            return ResponseEntity.badRequest().body(ApiResponse.error("La confirmation du mot de passe ne correspond pas."));
        }

        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable."))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable."));
        }

        // Déclare la variable `user` (User) initialisée avec `opt.get()`
        User user = opt.get();

        // Si l'ancien mot de passe est fourni, on le vérifie
        if (ancienMotDePasse != null && !ancienMotDePasse.isEmpty()) {
            // Condition : exécute le bloc suivant seulement si `!passwordEncoder.matches(ancienMotDePasse, user.getPassword())`
            if (!passwordEncoder.matches(ancienMotDePasse, user.getPassword())) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("L'ancien mot de passe est i…`
                return ResponseEntity.badRequest().body(ApiResponse.error("L'ancien mot de passe est incorrect."));
            }
        }

        // Renseigne la propriété Password de `user` avec `passwordEncoder.encode(nouveauMotDePasse)`
        user.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        // Renseigne la propriété FailedLoginAttempts de `user` avec la valeur numérique 0
        user.setFailedLoginAttempts(0);
        // Renseigne la propriété LockedUntil de `user` avec la valeur nulle (absence de valeur)
        user.setLockedUntil(null);
        // Enregistre user en base de données via userRepository
        userRepository.save(user);

        // Journaliser dans l'audit log
        try {
            // Instruction : com.diamyaraam.auth.entity.AuditLog log = new com.diamyaraam.auth.entity.AuditLog();
            com.diamyaraam.auth.entity.AuditLog log = new com.diamyaraam.auth.entity.AuditLog();
            // Renseigne la propriété ActionType de `log` avec la valeur de com.diamyaraam.auth.entity.AuditLog.ActionType.CHANGEMENT_MOT_DE_PASSE
            log.setActionType(com.diamyaraam.auth.entity.AuditLog.ActionType.CHANGEMENT_MOT_DE_PASSE);
            // Renseigne la propriété User de `log` avec la valeur de user
            log.setUser(user);
            // Renseigne la propriété TelephoneTente de `log` avec la valeur de l'attribut Telephone de user
            log.setTelephoneTente(user.getTelephone());
            // Renseigne la propriété IpAddress de `log` avec la valeur de l'attribut RemoteAddr de httpRequest
            log.setIpAddress(httpRequest.getRemoteAddr());
            // Renseigne la propriété Details de `log` avec le texte "Changement de mot de passe par le profil administrateur"
            log.setDetails("Changement de mot de passe par le profil administrateur");
            // Renseigne la propriété Success de `log` avec le booléen vrai
            log.setSuccess(true);
            // Enregistre log en base de données via auditLogRepository
            auditLogRepository.save(log);
        // Interception de l'exception Exception ignored
        } catch (Exception ignored) {}

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Mot de passe mis à jour avec s…
        return ResponseEntity.ok(ApiResponse.success("Mot de passe mis à jour avec succès.", null));
    }

    // Route HTTP PUT sur le chemin « /users/{id}/profile »
    @PutMapping("/users/{id}/profile")
    // Méthode `updateProfile` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : met à jour (update profile)
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            // Paramètre `id` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID id,
            // Paramètre `payload` de type dictionnaire clé/valeur — corps json de la requête désérialisé en objet java
            @RequestBody Map<String, Object> payload,
            // Paramètre `httpRequest` de type jakarta.servlet.http.HttpServletRequest
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable."))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable."));
        }

        // Déclare la variable `user` (User) initialisée avec `opt.get()`
        User user = opt.get();
        // Déclare la variable `firstName` (chaîne de caractères) initialisée avec `(String) payload.get("firstName")`
        String firstName = (String) payload.get("firstName");
        // Déclare la variable `lastName` (chaîne de caractères) initialisée avec `(String) payload.get("lastName")`
        String lastName = (String) payload.get("lastName");
        // Déclare la variable `telephone` (chaîne de caractères) initialisée avec `(String) payload.get("telephone")`
        String telephone = (String) payload.get("telephone");
        // Déclare la variable `email` (chaîne de caractères) initialisée avec `(String) payload.get("email")`
        String email = (String) payload.get("email");
        // Déclare la variable `photoProfil` (chaîne de caractères) initialisée avec `(String) payload.get("photoProfil")`
        String photoProfil = (String) payload.get("photoProfil");

        // Condition : exécute le bloc suivant seulement si `firstName != null && !firstName.trim().isEmpty()`
        if (firstName != null && !firstName.trim().isEmpty()) {
            // Renseigne la propriété FirstName de `user` avec `firstName.trim()`
            user.setFirstName(firstName.trim());
        }
        // Condition : exécute le bloc suivant seulement si `lastName != null && !lastName.trim().isEmpty()`
        if (lastName != null && !lastName.trim().isEmpty()) {
            // Renseigne la propriété LastName de `user` avec `lastName.trim()`
            user.setLastName(lastName.trim());
        }

        // Condition : exécute le bloc suivant seulement si `telephone != null && !telephone.trim().isEmpty() && !telephone.equals(user.getTelephone())`
        if (telephone != null && !telephone.trim().isEmpty() && !telephone.equals(user.getTelephone())) {
            // Condition : exécute le bloc suivant seulement si `userRepository.existsByTelephone(telephone.trim())`
            if (userRepository.existsByTelephone(telephone.trim())) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Ce numéro de téléphone est …`
                return ResponseEntity.badRequest().body(ApiResponse.error("Ce numéro de téléphone est déjà utilisé par un autre compte."));
            }
            // Renseigne la propriété Telephone de `user` avec `telephone.trim()`
            user.setTelephone(telephone.trim());
        }

        // Condition : exécute le bloc suivant seulement si `email != null && !email.trim().isEmpty() && !email.equalsIgnoreCase(user.getEmail())`
        if (email != null && !email.trim().isEmpty() && !email.equalsIgnoreCase(user.getEmail())) {
            // Condition : exécute le bloc suivant seulement si `userRepository.existsByEmail(email.trim())`
            if (userRepository.existsByEmail(email.trim())) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Cette adresse email est déj…`
                return ResponseEntity.badRequest().body(ApiResponse.error("Cette adresse email est déjà associée à un compte."));
            }
            // Renseigne la propriété Email de `user` avec `email.trim()`
            user.setEmail(email.trim());
        // Sinon, si la condition `email != null && email.trim().isEmpty()` est vraie
        } else if (email != null && email.trim().isEmpty()) {
            // Renseigne la propriété Email de `user` avec la valeur nulle (absence de valeur)
            user.setEmail(null);
        }

        // Condition : exécute le bloc suivant seulement si `photoProfil != null`
        if (photoProfil != null) {
            // Renseigne la propriété PhotoProfil de `user` avec la valeur de photoProfil
            user.setPhotoProfil(photoProfil);
        }

        // Déclare la variable `saved` (User) initialisée avec l'enregistrement en base de user via userRepository
        User saved = userRepository.save(user);

        // Journaliser dans l'audit log
        try {
            // Instruction : com.diamyaraam.auth.entity.AuditLog log = new com.diamyaraam.auth.entity.AuditLog();
            com.diamyaraam.auth.entity.AuditLog log = new com.diamyaraam.auth.entity.AuditLog();
            // Renseigne la propriété ActionType de `log` avec la valeur de com.diamyaraam.auth.entity.AuditLog.ActionType.MODIFICATION_PROFIL
            log.setActionType(com.diamyaraam.auth.entity.AuditLog.ActionType.MODIFICATION_PROFIL);
            // Renseigne la propriété User de `log` avec la valeur de saved
            log.setUser(saved);
            // Renseigne la propriété TelephoneTente de `log` avec la valeur de l'attribut Telephone de saved
            log.setTelephoneTente(saved.getTelephone());
            // Renseigne la propriété IpAddress de `log` avec la valeur de l'attribut RemoteAddr de httpRequest
            log.setIpAddress(httpRequest.getRemoteAddr());
            // Renseigne la propriété Details de `log` avec le texte "Mise à jour des coordonnées par le profil administrateur"
            log.setDetails("Mise à jour des coordonnées par le profil administrateur");
            // Renseigne la propriété Success de `log` avec le booléen vrai
            log.setSuccess(true);
            // Enregistre log en base de données via auditLogRepository
            auditLogRepository.save(log);
        // Interception de l'exception Exception ignored
        } catch (Exception ignored) {}

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès.…
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès.", toUserMap(saved)));
    }

    // Route HTTP POST sur le chemin « /users »
    @PostMapping("/users")
    // Méthode `createUser` (publique) — paramètres : `payload` (dictionnaire clé/valeur) ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : crée (create user)
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody Map<String, Object> payload) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `telephone` (chaîne de caractères) initialisée avec `(String) payload.get("telephone")`
            String telephone = (String) payload.get("telephone");
            // Déclare la variable `firstName` (chaîne de caractères) initialisée avec `(String) payload.get("firstName")`
            String firstName = (String) payload.get("firstName");
            // Déclare la variable `lastName` (chaîne de caractères) initialisée avec `(String) payload.get("lastName")`
            String lastName = (String) payload.get("lastName");
            // Déclare la variable `email` (chaîne de caractères) initialisée avec `(String) payload.get("email")`
            String email = (String) payload.get("email");
            // Déclare la variable `rawPassword` (chaîne de caractères) initialisée avec `(String) payload.get("password")`
            String rawPassword = (String) payload.get("password");
            // Déclare la variable `roleStr` (chaîne de caractères) initialisée avec `(String) payload.get("role")`
            String roleStr = (String) payload.get("role");

            // Condition : exécute le bloc suivant seulement si `telephone == null || firstName == null || lastName == null`
            if (telephone == null || firstName == null || lastName == null) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Nom, prénom et téléphone so…`
                return ResponseEntity.badRequest().body(ApiResponse.error("Nom, prénom et téléphone sont requis"));
            }

            // Condition : exécute le bloc suivant seulement si `userRepository.existsByTelephone(telephone)`
            if (userRepository.existsByTelephone(telephone)) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Ce numéro de téléphone est …`
                return ResponseEntity.badRequest().body(ApiResponse.error("Ce numéro de téléphone est déjà utilisé"));
            }

            // Condition : exécute le bloc suivant seulement si `email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)`
            if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)) {
                // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Cette adresse email est déj…`
                return ResponseEntity.badRequest().body(ApiResponse.error("Cette adresse email est déjà utilisée"));
            }

            // Déclare la variable `user` (User) initialisée avec une nouvelle instance de User
            User user = new User();
            // Renseigne la propriété FirstName de `user` avec la valeur de firstName
            user.setFirstName(firstName);
            // Renseigne la propriété LastName de `user` avec la valeur de lastName
            user.setLastName(lastName);
            // Renseigne la propriété Telephone de `user` avec la valeur de telephone
            user.setTelephone(telephone);
            // Renseigne la propriété Email de `user` avec `email != null && !email.trim().isEmpty() ? email : null`
            user.setEmail(email != null && !email.trim().isEmpty() ? email : null);
            // Renseigne la propriété Password de `user` avec `passwordEncoder.encode(rawPassword != null && !rawPassword.isEmpty() ? rawPassw…`
            user.setPassword(passwordEncoder.encode(rawPassword != null && !rawPassword.isEmpty() ? rawPassword : "Password123!"));
            // Renseigne la propriété AccountStatus de `user` avec la valeur de User.AccountStatus.ACTIF
            user.setAccountStatus(User.AccountStatus.ACTIF);
            // Renseigne la propriété PhoneVerified de `user` avec le booléen vrai
            user.setPhoneVerified(true);

            // Déclare la variable `roleFinal` (chaîne de caractères) initialisée avec `(roleStr != null && !roleStr.trim().isEmpty()) ? roleStr.toUpperCase() : "PATIE…`
            final String roleFinal = (roleStr != null && !roleStr.trim().isEmpty()) ? roleStr.toUpperCase() : "PATIENT";
            // Déclare la variable `role` (Role) initialisée avec le résultat de la requête findByNomRole exécutée via roleRepository
            Role role = roleRepository.findByNomRole(roleFinal)
                    // Enchaînement : appelle `orElseGet(() -> roleRepository.save(new Role(null, roleFina…`
                    .orElseGet(() -> roleRepository.save(new Role(null, roleFinal)));
            // Renseigne la propriété Role de `user` avec la valeur de role
            user.setRole(role);

            // Condition : exécute le bloc suivant seulement si `"ADMIN".equalsIgnoreCase(roleFinal)`
            if ("ADMIN".equalsIgnoreCase(roleFinal)) {
                // Renseigne la propriété IsStaff de `user` avec le booléen vrai
                user.setIsStaff(true);
                // Renseigne la propriété IsSuperuser de `user` avec le booléen vrai
                user.setIsSuperuser(true);
            }

            // Déclare la variable `saved` (User) initialisée avec l'enregistrement en base de user via userRepository
            User saved = userRepository.save(user);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Utilisateur créé avec succès",…
            return ResponseEntity.ok(ApiResponse.success("Utilisateur créé avec succès", toUserMap(saved)));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Erreur lors de la création …`
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur lors de la création : " + e.getMessage()));
        }
    }

    // Route HTTP DELETE sur le chemin « /users/{id} »
    @DeleteMapping("/users/{id}")
    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `deleteUser` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : supprime (delete user)
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        // Déclare la variable `opt` (valeur optionnelle de User) initialisée avec le résultat de la requête findById exécutée via userRepository
        Optional<User> opt = userRepository.findById(id);
        // Condition : exécute le bloc suivant seulement si `opt.isEmpty()`
        if (opt.isEmpty()) {
            // Retourne `ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"))`
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        // Déclare la variable `u` (User) initialisée avec `opt.get()`
        User u = opt.get();
        // Détacher l'utilisateur des journaux d'audit pour préserver la traçabilité médicale sans bloquer la suppression
        auditLogRepository.findByUserOrderByCreatedAtDesc(u).forEach(a -> {
            // Renseigne la propriété User de `a` avec la valeur nulle (absence de valeur)
            a.setUser(null);
            // Condition : exécute le bloc suivant seulement si `a.getTelephoneTente() == null`
            if (a.getTelephoneTente() == null) {
                // Renseigne la propriété TelephoneTente de `a` avec la valeur de l'attribut Telephone de u
                a.setTelephoneTente(u.getTelephone());
            }
            // Enregistre a en base de données via auditLogRepository
            auditLogRepository.save(a);
        });

        // Supprime des données en base via userRepository : userRepository.delete(u);
        userRepository.delete(u);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succ…
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès", null));
    }

    // Route HTTP GET sur le chemin « /stats »
    @GetMapping("/stats")
    // Méthode `getStats` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de dictionnaire clé/valeur ; intention : récupère (get stats)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        // Déclare la variable `users` (liste de User) initialisée avec le résultat de la requête findAll exécutée via userRepository
        List<User> users = userRepository.findAll();

        // Déclare la variable `total` (entier long) initialisée avec `users.size()`
        long total = users.size();
        // Déclare la variable `patients` (entier long) initialisée avec `users.stream().filter(u -> u.getRole() != null && "PATIENT".equalsIgnoreCase(u.…`
        long patients = users.stream().filter(u -> u.getRole() != null && "PATIENT".equalsIgnoreCase(u.getRole().getNomRole())).count();
        // Déclare la variable `medecins` (entier long) initialisée avec `users.stream().filter(u -> u.getRole() != null && "MEDECIN".equalsIgnoreCase(u.…`
        long medecins = users.stream().filter(u -> u.getRole() != null && "MEDECIN".equalsIgnoreCase(u.getRole().getNomRole())).count();
        // Déclare la variable `admins` (entier long) initialisée avec `users.stream().filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.ge…`
        long admins = users.stream().filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getNomRole())).count();

        // Déclare la variable `actifs` (entier long) initialisée avec `users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.ACTIF).co…`
        long actifs = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.ACTIF).count();
        // Déclare la variable `suspendus` (entier long) initialisée avec `users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.SUSPENDU)…`
        long suspendus = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.SUSPENDU).count();
        // Déclare la variable `bloques` (entier long) initialisée avec `users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.BLOQUE).c…`
        long bloques = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.BLOQUE).count();
        // Déclare la variable `enAttente` (entier long) initialisée avec `users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.EN_ATTENT…`
        long enAttente = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.EN_ATTENTE).count();

        // Déclare la variable `stats` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de LinkedHashMap<>
        Map<String, Object> stats = new LinkedHashMap<>();
        // Appelle la méthode `put` sur `stats` : stats.put("totalUsers", total);
        stats.put("totalUsers", total);
        // Appelle la méthode `put` sur `stats` : stats.put("totalPatients", patients);
        stats.put("totalPatients", patients);
        // Appelle la méthode `put` sur `stats` : stats.put("totalMedecins", medecins);
        stats.put("totalMedecins", medecins);
        // Appelle la méthode `put` sur `stats` : stats.put("totalAdmins", admins);
        stats.put("totalAdmins", admins);
        // Appelle la méthode `put` sur `stats` : stats.put("statusActif", actifs);
        stats.put("statusActif", actifs);
        // Appelle la méthode `put` sur `stats` : stats.put("statusSuspendu", suspendus);
        stats.put("statusSuspendu", suspendus);
        // Appelle la méthode `put` sur `stats` : stats.put("statusBloque", bloques);
        stats.put("statusBloque", bloques);
        // Appelle la méthode `put` sur `stats` : stats.put("statusEnAttente", enAttente);
        stats.put("statusEnAttente", enAttente);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statistiques utilisateurs", st…
        return ResponseEntity.ok(ApiResponse.success("Statistiques utilisateurs", stats));
    }
}
