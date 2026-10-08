// Déclaration du package Java : `com.diamyaraam.auth.security`
package com.diamyaraam.auth.security;

// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `UserRepository` (paquet com.diamyaraam.auth.repository)
import com.diamyaraam.auth.repository.UserRepository;
// Import de la classe `SimpleGrantedAuthority` (paquet org.springframework.security.core.authority)
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// Import de la classe `UserDetails` (paquet org.springframework.security.core.userdetails)
import org.springframework.security.core.userdetails.UserDetails;
// Import de la classe `UserDetailsService` (paquet org.springframework.security.core.userdetails)
import org.springframework.security.core.userdetails.UserDetailsService;
// Import de la classe `UsernameNotFoundException` (paquet org.springframework.security.core.userdetails)
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;

/**
 * ====================================================================================================
 * SERVICE D'ADAPTATION SPRING SECURITY : CHARGEMENT DES PRINCIPALS (USER DETAILS SERVICE)
 * ====================================================================================================
 * 
 * 🎓 JUSTIFICATION TECHNIQUE POUR LA SOUTENANCE :
 * Comment Spring Security fait-il le lien entre notre table SQL personnalisée `users`
 * et son moteur interne d'authentification ?
 * 
 * 🔌 RÔLE DU PATRON ADAPTATEUR (ADAPTER PATTERN) :
 * 1. Implémentation du contrat `org.springframework.security.core.userdetails.UserDetailsService` :
 *    Spring Security ne connaît pas notre entité métier `User`. Il exige un objet implémentant `UserDetails`.
 * 
 * 2. Numéro de Téléphone comme "Username" :
 *    Au lieu du couple classique "nom d'utilisateur / email", la méthode `loadUserByUsername`
 *    reçoit ici le numéro de téléphone normalisé (ex: "+221771234567").
 * 
 * 3. Transmission des Règles de Sécurité à la JVM :
 *    Transmet le hash BCrypt, les autorités ("ROLE_ADMIN", "ROLE_MEDECIN", "ROLE_PATIENT"),
 *    l'état de verrouillage (`accountLocked`) et d'activation (`disabled`).
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `UserDetailsServiceImpl`, et implémente UserDetailsService
public class UserDetailsServiceImpl implements UserDetailsService {

    // Attribut `userRepository` de type UserRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final UserRepository userRepository;

    // Constructeur de `UserDetailsServiceImpl` — paramètres : `userRepository` (UserRepository) (injection des dépendances par Spring)
    public UserDetailsServiceImpl(UserRepository userRepository) {
        // Initialise l'attribut `userRepository` avec la valeur de userRepository
        this.userRepository = userRepository;
    }


    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `loadUserByUsername` (publique) — paramètres : `telephone` (chaîne de caractères) ; retourne : UserDetails ; intention : charge (load user by username)
    public UserDetails loadUserByUsername(String telephone) throws UsernameNotFoundException {

        // Déclare la variable `user` (User) initialisée avec le résultat de la requête findByTelephone exécutée via userRepository
        User user = userRepository.findByTelephone(telephone)
                // Enchaînement : appelle `orElseThrow(() -> new UsernameNotFoundException(`
                .orElseThrow(() -> new UsernameNotFoundException(
                    // Texte « Aucun compte trouvé avec ce numéro : »
                    "Aucun compte trouvé avec ce numéro : " + telephone
                ));

        // Déclare la variable `role` (chaîne de caractères) initialisée avec `user.getRole() != null`
        String role = user.getRole() != null
                // Suite de l'expression (opérateur) : ? "ROLE_" + user.getRole().getNomRole()
                ? "ROLE_" + user.getRole().getNomRole()
                // Instruction : : "ROLE_USER";
                : "ROLE_USER";

        // Retourne la valeur de org.springframework.security.core.userdetails.User
        return org.springframework.security.core.userdetails.User
                // Enchaînement : appelle `withUsername(telephone)`
                .withUsername(telephone)
                // Enchaînement : appelle `password(user.getPassword())`
                .password(user.getPassword())
                // Enchaînement : appelle `authorities(new SimpleGrantedAuthority(role))`
                .authorities(new SimpleGrantedAuthority(role))
                // Enchaînement : appelle `accountLocked(user.isAccountLocked())`
                .accountLocked(user.isAccountLocked())
                // Enchaînement : appelle `disabled(!user.isActive())`
                .disabled(!user.isActive())
                // Enchaînement : appelle `build();`
                .build();
    }
}
