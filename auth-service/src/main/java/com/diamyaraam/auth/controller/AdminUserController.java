package com.diamyaraam.auth.controller;

import com.diamyaraam.auth.entity.Role;
import com.diamyaraam.auth.entity.User;
import com.diamyaraam.auth.repository.RoleRepository;
import com.diamyaraam.auth.repository.UserRepository;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur d'administration pour la gestion complète des utilisateurs.
 * Accessible via l'API Gateway : /api/auth/admin/**
 */
@RestController
@RequestMapping("/auth/admin")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private Map<String, Object> toUserMap(User u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", u.getId());
        map.put("firstName", u.getFirstName());
        map.put("lastName", u.getLastName());
        map.put("telephone", u.getTelephone());
        map.put("email", u.getEmail());
        map.put("dateNaissance", u.getDateNaissance());
        map.put("genre", u.getGenre() != null ? u.getGenre().name() : null);
        map.put("photoProfil", u.getPhotoProfil());
        map.put("role", u.getRole() != null ? u.getRole().getNomRole() : "PATIENT");
        map.put("accountStatus", u.getAccountStatus() != null ? u.getAccountStatus().name() : "ACTIF");
        map.put("phoneVerified", u.getPhoneVerified() != null ? u.getPhoneVerified() : false);
        map.put("failedLoginAttempts", u.getFailedLoginAttempts() != null ? u.getFailedLoginAttempts() : 0);
        map.put("lockedUntil", u.getLockedUntil());
        map.put("isStaff", u.getIsStaff() != null ? u.getIsStaff() : false);
        map.put("isSuperuser", u.getIsSuperuser() != null ? u.getIsSuperuser() : false);
        map.put("createdAt", u.getCreatedAt());
        map.put("updatedAt", u.getUpdatedAt());
        return map;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        List<User> users = userRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            users = users.stream().filter(u ->
                (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q)) ||
                (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)) ||
                (u.getTelephone() != null && u.getTelephone().toLowerCase().contains(q)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
            ).collect(Collectors.toList());
        }

        if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("TOUS")) {
            users = users.stream().filter(u ->
                u.getRole() != null && u.getRole().getNomRole().equalsIgnoreCase(role.trim())
            ).collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("TOUS")) {
            users = users.stream().filter(u ->
                u.getAccountStatus() != null && u.getAccountStatus().name().equalsIgnoreCase(status.trim())
            ).collect(Collectors.toList());
        }

        // Trier par date de création descendante
        users.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        List<Map<String, Object>> result = users.stream().map(this::toUserMap).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs", result));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.success("Détails utilisateur", toUserMap(u))))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable")));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, String> body) {

        String newStatus = status;
        if (newStatus == null && body != null) {
            newStatus = body.get("status") != null ? body.get("status") : body.get("accountStatus");
        }
        if (newStatus == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Statut requis (ACTIF, SUSPENDU, BLOQUE, EN_ATTENTE)"));
        }

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        User user = opt.get();
        user.setAccountStatus(User.AccountStatus.valueOf(newStatus.toUpperCase()));
        if (user.getAccountStatus() == User.AccountStatus.ACTIF) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
        User saved = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour avec succès", toUserMap(saved)));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUserRole(
            @PathVariable UUID id,
            @RequestParam(required = false) String role,
            @RequestBody(required = false) Map<String, String> body) {

        String newRoleStr = role;
        if (newRoleStr == null && body != null) {
            newRoleStr = body.get("role") != null ? body.get("role") : body.get("nomRole");
        }
        if (newRoleStr == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Rôle requis (PATIENT, MEDECIN, ADMIN)"));
        }

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        final String finalRole = newRoleStr.toUpperCase();
        Role r = roleRepository.findByNomRole(finalRole)
                .orElseGet(() -> roleRepository.save(new Role(null, finalRole)));

        User user = opt.get();
        user.setRole(r);
        if ("ADMIN".equalsIgnoreCase(finalRole)) {
            user.setIsStaff(true);
            user.setIsSuperuser(true);
        }
        User saved = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Rôle mis à jour avec succès", toUserMap(saved)));
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlockUser(@PathVariable UUID id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        User user = opt.get();
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setAccountStatus(User.AccountStatus.ACTIF);
        User saved = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Compte débloqué et réactivé", toUserMap(saved)));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody Map<String, Object> payload) {
        try {
            String telephone = (String) payload.get("telephone");
            String firstName = (String) payload.get("firstName");
            String lastName = (String) payload.get("lastName");
            String email = (String) payload.get("email");
            String rawPassword = (String) payload.get("password");
            String roleStr = (String) payload.get("role");

            if (telephone == null || firstName == null || lastName == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Nom, prénom et téléphone sont requis"));
            }

            if (userRepository.existsByTelephone(telephone)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Ce numéro de téléphone est déjà utilisé"));
            }

            if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Cette adresse email est déjà utilisée"));
            }

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setTelephone(telephone);
            user.setEmail(email != null && !email.trim().isEmpty() ? email : null);
            user.setPassword(passwordEncoder.encode(rawPassword != null && !rawPassword.isEmpty() ? rawPassword : "Password123!"));
            user.setAccountStatus(User.AccountStatus.ACTIF);
            user.setPhoneVerified(true);

            final String roleFinal = (roleStr != null && !roleStr.trim().isEmpty()) ? roleStr.toUpperCase() : "PATIENT";
            Role role = roleRepository.findByNomRole(roleFinal)
                    .orElseGet(() -> roleRepository.save(new Role(null, roleFinal)));
            user.setRole(role);

            if ("ADMIN".equalsIgnoreCase(roleFinal)) {
                user.setIsStaff(true);
                user.setIsSuperuser(true);
            }

            User saved = userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success("Utilisateur créé avec succès", toUserMap(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur lors de la création : " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable"));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur supprimé avec succès", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        List<User> users = userRepository.findAll();

        long total = users.size();
        long patients = users.stream().filter(u -> u.getRole() != null && "PATIENT".equalsIgnoreCase(u.getRole().getNomRole())).count();
        long medecins = users.stream().filter(u -> u.getRole() != null && "MEDECIN".equalsIgnoreCase(u.getRole().getNomRole())).count();
        long admins = users.stream().filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getNomRole())).count();

        long actifs = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.ACTIF).count();
        long suspendus = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.SUSPENDU).count();
        long bloques = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.BLOQUE).count();
        long enAttente = users.stream().filter(u -> u.getAccountStatus() == User.AccountStatus.EN_ATTENTE).count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", total);
        stats.put("totalPatients", patients);
        stats.put("totalMedecins", medecins);
        stats.put("totalAdmins", admins);
        stats.put("statusActif", actifs);
        stats.put("statusSuspendu", suspendus);
        stats.put("statusBloque", bloques);
        stats.put("statusEnAttente", enAttente);

        return ResponseEntity.ok(ApiResponse.success("Statistiques utilisateurs", stats));
    }
}
