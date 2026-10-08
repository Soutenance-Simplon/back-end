// Déclaration du package Java : `com.diamyaraam.discovery.config`
package com.diamyaraam.discovery.config;

// Import de la classe `Bean` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Bean;
// Import de la classe `Configuration` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Configuration;
// Import de la classe `HttpSecurity` (paquet org.springframework.security.config.annotation.web.builders)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Import de la classe `EnableWebSecurity` (paquet org.springframework.security.config.annotation.web.configuration)
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import de la classe `AbstractHttpConfigurer` (paquet org.springframework.security.config.annotation.web.configurers)
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// Import de la classe `SecurityFilterChain` (paquet org.springframework.security.web)
import org.springframework.security.web.SecurityFilterChain;

/**
 * ====================================================================================================
 * CONFIGURATION DE SÉCURITÉ DU SERVEUR EUREKA
 * ====================================================================================================
 * 
 * 🎓 JUSTIFICATION TECHNIQUE POUR LA SOUTENANCE :
 * Par défaut, Spring Security active la protection contre les attaques CSRF (Cross-Site Request Forgery)
 * sur toutes les requêtes HTTP mutatives (POST, PUT, DELETE).
 * 
 * Or, les clients Eureka (nos microservices Spring Boot) envoient des requêtes HTTP POST régulières
 * pour s'enregistrer et pour envoyer des signaux de battement de cœur (heartbeats) sans inclure de jeton CSRF.
 * 
 * ⚠️ PROBLÈME CLASSIQUE SANS CETTE CONFIGURATION :
 * Sans la désactivation ou l'exclusion du CSRF, Eureka renverrait systématiquement un code HTTP 403 Forbidden
 * aux microservices essayant de s'enregistrer, bloquant ainsi l'ensemble du réseau de microservices.
 * 
 * 🛡️ CHOIX D'ARCHITECTURE RETENU :
 * 1. Désactivation du CSRF sur l'ensemble des endpoints d'Eureka (/eureka/**).
 * 2. Autorisation d'accès universel (permitAll) aux requêtes d'enregistrement et au tableau de bord.
 *    Dans un réseau privé interne (ex: réseau Docker Bridge ou Kubernetes), les services se découvrent
 *    de manière fluide sans barrière d'authentification basique bloquante.
 * ====================================================================================================
 */
@Configuration
// Active la sécurité web Spring Security
@EnableWebSecurity // Active la pile de sécurité Spring Security
// Déclaration de la classe `SecurityConfig` (rôle : porte la configuration)
public class SecurityConfig {

    /**
     * Chaîne de filtres de sécurité HTTP (SecurityFilterChain).
     * 
     * @param http Constructeur de sécurité HTTP fluent
     * @return L'instance SecurityFilterChain configurée
     * @throws Exception En cas d'erreur de configuration
     */
    @Bean
    // Méthode `securityFilterChain` (publique) — paramètres : `http` (HttpSecurity) ; retourne : SecurityFilterChain
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Retourne la valeur de http
        return http
                // Ignorer la vérification CSRF pour permettre l'enregistrement automatique des clients Eureka
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                // Autoriser toutes les requêtes entrantes sans exiger d'authentification préalable
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Désactiver l'authentification HTTP Basic pour éviter les fenêtres pop-up du navigateur
                .httpBasic(AbstractHttpConfigurer::disable)
                // Construction et retour de la chaîne de filtres Spring Security
                .build();
    }
}

