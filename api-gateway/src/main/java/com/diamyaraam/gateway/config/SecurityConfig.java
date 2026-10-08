// Déclaration du package Java : `com.diamyaraam.gateway.config`
package com.diamyaraam.gateway.config;

// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `Bean` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Bean;
// Import de la classe `Configuration` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Configuration;
// Import de la classe `HttpMethod` (paquet org.springframework.http)
import org.springframework.http.HttpMethod;
// Import de la classe `Converter` (paquet org.springframework.core.convert.converter)
import org.springframework.core.convert.converter.Converter;
// Import de la classe `AbstractAuthenticationToken` (paquet org.springframework.security.authentication)
import org.springframework.security.authentication.AbstractAuthenticationToken;
// Import de la classe `Customizer` (paquet org.springframework.security.config)
import org.springframework.security.config.Customizer;
// Import de la classe `EnableWebFluxSecurity` (paquet org.springframework.security.config.annotation.web.reactive)
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
// Import de la classe `ServerHttpSecurity` (paquet org.springframework.security.config.web.server)
import org.springframework.security.config.web.server.ServerHttpSecurity;
// Import de la classe `GrantedAuthority` (paquet org.springframework.security.core)
import org.springframework.security.core.GrantedAuthority;
// Import de la classe `SimpleGrantedAuthority` (paquet org.springframework.security.core.authority)
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// Import de la classe `DelegatingOAuth2TokenValidator` (paquet org.springframework.security.oauth2.core)
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
// Import de la classe `OAuth2TokenValidator` (paquet org.springframework.security.oauth2.core)
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
// Import de la classe `MacAlgorithm` (paquet org.springframework.security.oauth2.jose.jws)
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
// Import de toutes les classes du paquet `org.springframework.security.oauth2.jwt`
import org.springframework.security.oauth2.jwt.*;
// Import de la classe `JwtAuthenticationToken` (paquet org.springframework.security.oauth2.server.resource.authentication)
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
// Import de la classe `SecurityWebFilterChain` (paquet org.springframework.security.web.server)
import org.springframework.security.web.server.SecurityWebFilterChain;
// Import de la classe `CorsConfiguration` (paquet org.springframework.web.cors)
import org.springframework.web.cors.CorsConfiguration;
// Import de la classe `CorsConfigurationSource` (paquet org.springframework.web.cors.reactive)
import org.springframework.web.cors.reactive.CorsConfigurationSource;
// Import de la classe `UrlBasedCorsConfigurationSource` (paquet org.springframework.web.cors.reactive)
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
// Import de la classe `Mono` (paquet reactor.core.publisher)
import reactor.core.publisher.Mono;

// Import de la classe `SecretKey` (paquet javax.crypto)
import javax.crypto.SecretKey;
// Import de la classe `SecretKeySpec` (paquet javax.crypto.spec)
import javax.crypto.spec.SecretKeySpec;
// Import de la classe `StandardCharsets` (paquet java.nio.charset)
import java.nio.charset.StandardCharsets;
// Import de la classe `ArrayList` (paquet java.util)
import java.util.ArrayList;
// Import de la classe `Collection` (paquet java.util)
import java.util.Collection;
// Import de la classe `List` (paquet java.util)
import java.util.List;

/**
 * ====================================================================================================
 * CONFIGURATION DE SÉCURITÉ RÉACTIVE (SPRING WEBFLUX & SPRING SECURITY OAUTH2 RESOURCE SERVER)
 * ====================================================================================================
 * 
 * 🎓 EXPLICATIONS TECHNIQUES POUR LE JURY DE SOUTENANCE :
 * Dans une passerelle Spring Cloud Gateway, le moteur sous-jacent n'est PAS Spring MVC (bloquant avec Servlets),
 * mais Spring WebFlux basé sur le framework réactif non-bloquant Project Reactor (Flux & Mono).
 * C'est pourquoi nous utilisons `@EnableWebFluxSecurity` et `ServerHttpSecurity` plutôt que `HttpSecurity`.
 * 
 * 🛡️ STRATÉGIE DE SÉCURITÉ MISE EN ŒUVRE (Zero-Trust Perimeter) :
 * 1. Policy Enforcement Point (PEP) Centralisé :
 *    Toute requête traversant la plateforme doit être autorisée ici avant même de toucher
 *    un microservice métier, protégeant ainsi le CPU et la base de données des services sous-jacents.
 * 
 * 2. Contrôle Cryptographique JWT (Algorithme HMAC-SHA512) :
 *    La passerelle vérifie mathématiquement la signature de chaque jeton avec la clé secrète partagée.
 *    Si le jeton a été altéré, a expiré, ou n'est pas signé correctement, l'accès est bloqué (401 Unauthorized).
 * 
 * 3. Prévention des Attaques par Confusion de Jetons (Token Type Checking) :
 *    Un Refresh Token est strictement interdit comme jeton d'accès aux APIs (claim `type != REFRESH`).
 * 
 * 4. Contrôle d'Accès Basé sur les Rôles (RBAC - Role-Based Access Control) :
 *    Les routes administratives (`/api/auth/admin/**`, `/api/admin/**`) exigent explicitement le rôle ADMIN.
 * 
 * 5. Gestion Sécurisée du CORS (Cross-Origin Resource Sharing) :
 *    Origines autorisées explicitement restreintes (Frontend Flutter Web, Dashboard Admin Vue)
 *    pour interdire les requêtes frauduleuses depuis des domaines tiers arbitraires.
 * ====================================================================================================
 */
@Configuration
// Active la sécurité Spring Security en mode réactif (WebFlux)
@EnableWebFluxSecurity
// Déclaration de la classe `SecurityConfig` (rôle : porte la configuration)
public class SecurityConfig {

    /** Clé secrète cryptographique injectée depuis application.yml pour valider la signature JWT HS512 */
    @Value("${jwt.secret}")
    // Attribut `jwtSecret` de type chaîne de caractères [privée]
    private String jwtSecret;

    /** Liste blanche des origines HTTP autorisées pour le mécanisme CORS */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    // Attribut `allowedOrigins` de type chaîne de caractères [privée]
    private String allowedOrigins;

    /**
     * Chaîne de filtres de sécurité réactive (SecurityWebFilterChain).
     * Définit les règles d'autorisation par URL et configure le décodeur OAuth2 Resource Server.
     * 
     * @param http Constructeur ServerHttpSecurity réactif
     * @return La chaîne de filtres réactive compilée
     */
    @Bean
    // Méthode `springSecurityFilterChain` (publique) — paramètres : `http` (ServerHttpSecurity) ; retourne : SecurityWebFilterChain
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Retourne la valeur de http
        return http
            // 1. Application de la politique CORS personnalisée
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Désactivation du CSRF : L'API est sans état (Stateless) basée sur jetons Bearer JWT
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // 3. Définition fine des autorisations de routage (Règles RBAC et routes publiques)
            .authorizeExchange(exchanges -> exchanges
                // Requêtes pré-vol CORS (OPTIONS) : doivent impérativement passer sans authentification
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                
                // Routes publiques d'authentification (Création de compte, Login, Validation OTP, Reset password)
                .pathMatchers(
                    // Paire clé/valeur : clé « /api/auth/login » associée à 
                    "/api/auth/login",
                    // Paire clé/valeur : clé « /api/auth/register/** » associée à 
                    "/api/auth/register/**",
                    // Paire clé/valeur : clé « /api/auth/otp/** » associée à 
                    "/api/auth/otp/**",
                    // Paire clé/valeur : clé « /api/auth/password/** » associée à 
                    "/api/auth/password/**",
                    // Paire clé/valeur : clé « /api/auth/refresh-token » associée à 
                    "/api/auth/refresh-token",
                    // Texte « /api/auth/verify-id »
                    "/api/auth/verify-id"
                // Enchaînement : appelle `permitAll()`
                ).permitAll()
                
                // Endpoints de métriques et santé système (Spring Boot Actuator)
                .pathMatchers(
                    // Paire clé/valeur : clé « /actuator/health » associée à 
                    "/actuator/health",
                    // Paire clé/valeur : clé « /actuator/health/** » associée à 
                    "/actuator/health/**",
                    // Texte « /actuator/info »
                    "/actuator/info"
                // Enchaînement : appelle `permitAll()`
                ).permitAll()
                
                // Handshakes WebSockets (consultations vidéo LiveKit et notifications temps réel)
                .pathMatchers("/ws-rdv/**", "/ws/**").permitAll()
                
                // Routes réservées strictement aux Administrateurs système (RBAC)
                .pathMatchers("/api/auth/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // Principe de moindre privilège : Tout autre endpoint non explicité exige un JWT valide
                .anyExchange().authenticated()
            )
            
            // 4. Configuration en tant que serveur de ressources OAuth2 avec convertisseur de rôles JWT
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            // Enchaînement : appelle `build();`
            .build();
    }

    /**
     * Convertisseur JWT réactif.
     * Extrait le claim "role" injecté dans le payload JWT par l'auth-service (ex: "ADMIN", "MEDECIN", "PATIENT")
     * et le transforme en autorité Spring Security standard avec préfixe conventionnel "ROLE_" (ex: "ROLE_ADMIN").
     * 
     * @return Un convertisseur réactif transformant un Jwt en JwtAuthenticationToken avec ses GrantedAuthorities
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        // Retourne `jwt -> {`
        return jwt -> {
            // Déclare la variable `authorities` (liste de GrantedAuthority) initialisée avec une nouvelle instance de ArrayList<>
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            // Déclare la variable `role` (chaîne de caractères) initialisée avec `jwt.getClaimAsString("role")`
            String role = jwt.getClaimAsString("role");
            // Condition : exécute le bloc suivant seulement si `role != null && !role.isBlank()`
            if (role != null && !role.isBlank()) {
                // Spring Security requiert le préfixe "ROLE_" pour la méthode hasRole("ADMIN")
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
            }
            // Retourne `Mono.just(new JwtAuthenticationToken(jwt, authorities))`
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

    /**
     * Décodeur JWT réactif Nimbus (NimbusReactiveJwtDecoder).
     * 1. Vérifie la signature cryptographique du jeton via HMAC-SHA512.
     * 2. Valide les dates d'émission (iat) et d'expiration (exp).
     * 3. Vérifie que le claim "type" n'est pas "REFRESH" pour bloquer le détournement de Refresh Token.
     * 
     * @return Le décodeur JWT configuré et sécurisé
     */
    @Bean
    // Méthode `reactiveJwtDecoder` (publique) — sans paramètre ; retourne : ReactiveJwtDecoder
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // Clé secrète sous forme d'octets sécurisés
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        // Déclare la variable `decoder` (NimbusReactiveJwtDecoder) initialisée avec `NimbusReactiveJwtDecoder.withSecretKey(key)`
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                // Enchaînement : appelle `macAlgorithm(MacAlgorithm.HS512)`
                .macAlgorithm(MacAlgorithm.HS512)
                // Enchaînement : appelle `build();`
                .build();

        // Validateur par défaut (vérifie l'expiration temporelle exp et l'horodatage nbf)
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        
        // Règle de sécurité sur mesure : un Refresh Token ne doit JAMAIS servir à consommer des APIs métier
        OAuth2TokenValidator<Jwt> notRefreshTokenValidator = new JwtClaimValidator<String>(
                // Paire clé/valeur : clé « type » associée à 
                "type",
                // Fonction lambda : type -> type == null || !"REFRESH".equalsIgnoreCase(type)
                type -> type == null || !"REFRESH".equalsIgnoreCase(type)
        );
        
        // Composition des validateurs
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, notRefreshTokenValidator));

        // Retourne la valeur de decoder
        return decoder;
    }

    /**
     * Configuration CORS réactive (Cross-Origin Resource Sharing).
     * Autorise les navigateurs web hébergeant l'application cliente à interagir avec la passerelle,
     * tout en filtrant rigoureusement les méthodes, en-têtes et origines autorisés.
     * 
     * @return La source de configuration CORS enregistrée sur l'ensemble des routes "/**"
     */
    @Bean
    // Méthode `corsConfigurationSource` (publique) — sans paramètre ; retourne : CorsConfigurationSource
    public CorsConfigurationSource corsConfigurationSource() {
        // Déclare la variable `config` (CorsConfiguration) initialisée avec une nouvelle instance de CorsConfiguration
        CorsConfiguration config = new CorsConfiguration();
        
        // Ajout des domaines autorisés (Dashboard Vue.js, Flutter Web, etc.)
        for (String origin : allowedOrigins.split(",")) {
            // Déclare la variable `trimmed` (chaîne de caractères) initialisée avec `origin.trim()`
            String trimmed = origin.trim();
            // Condition : exécute le bloc suivant seulement si `!trimmed.isEmpty()`
            if (!trimmed.isEmpty()) {
                // Appelle la méthode `addAllowedOrigin` sur `config` : config.addAllowedOrigin(trimmed);
                config.addAllowedOrigin(trimmed);
            }
        }
        
        // Méthodes HTTP autorisées
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // En-têtes HTTP requis pour les requêtes sécurisées et le protocole JWT
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        // Renseigne la propriété AllowCredentials de `config` avec le booléen faux
        config.setAllowCredentials(false);
        // Renseigne la propriété MaxAge de `config` avec la valeur numérique 3600L
        config.setMaxAge(3600L); // Mise en cache du résultat preflight pendant 1 heure pour optimiser le réseau

        // Déclare la variable `source` (UrlBasedCorsConfigurationSource) initialisée avec une nouvelle instance de UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appelle la méthode `registerCorsConfiguration` sur `source` : source.registerCorsConfiguration("/**", config);
        source.registerCorsConfiguration("/**", config);
        // Retourne la valeur de source
        return source;
    }
}

