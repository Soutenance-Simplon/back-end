// Déclaration du package Java : `com.diamyaraam.auth.security`
package com.diamyaraam.auth.security;

// Import de la classe `Bean` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Bean;
// Import de la classe `Configuration` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Configuration;
// Import de la classe `AuthenticationManager` (paquet org.springframework.security.authentication)
import org.springframework.security.authentication.AuthenticationManager;
// Import de la classe `AuthenticationProvider` (paquet org.springframework.security.authentication)
import org.springframework.security.authentication.AuthenticationProvider;
// Import de la classe `DaoAuthenticationProvider` (paquet org.springframework.security.authentication.dao)
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// Import de la classe `AuthenticationConfiguration` (paquet org.springframework.security.config.annotation.authentication.configuration)
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// Import de la classe `HttpSecurity` (paquet org.springframework.security.config.annotation.web.builders)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Import de la classe `EnableWebSecurity` (paquet org.springframework.security.config.annotation.web.configuration)
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import de la classe `AbstractHttpConfigurer` (paquet org.springframework.security.config.annotation.web.configurers)
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// Import de la classe `SessionCreationPolicy` (paquet org.springframework.security.config.http)
import org.springframework.security.config.http.SessionCreationPolicy;
// Import de la classe `BCryptPasswordEncoder` (paquet org.springframework.security.crypto.bcrypt)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Import de la classe `PasswordEncoder` (paquet org.springframework.security.crypto.password)
import org.springframework.security.crypto.password.PasswordEncoder;
// Import de la classe `SecurityFilterChain` (paquet org.springframework.security.web)
import org.springframework.security.web.SecurityFilterChain;
// Import de la classe `CorsConfiguration` (paquet org.springframework.web.cors)
import org.springframework.web.cors.CorsConfiguration;
// Import de la classe `CorsConfigurationSource` (paquet org.springframework.web.cors)
import org.springframework.web.cors.CorsConfigurationSource;
// Import de la classe `UrlBasedCorsConfigurationSource` (paquet org.springframework.web.cors)
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// Import de la classe `List` (paquet java.util)
import java.util.List;

// Classe de configuration Spring : déclare des beans gérés par le conteneur
@Configuration
// Active la sécurité web Spring Security
@EnableWebSecurity
// Déclaration de la classe `SecurityConfig` (rôle : porte la configuration)
public class SecurityConfig {

    // Attribut `userDetailsService` de type UserDetailsServiceImpl — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final UserDetailsServiceImpl userDetailsService;
    // Attribut `jwtFilter` de type JwtFilter — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final JwtFilter jwtFilter;

    // Injecte la valeur de la propriété de configuration indiquée : ${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}
    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    // Attribut `allowedOrigins` de type chaîne de caractères [privée]
    private String allowedOrigins;

    // Constructeur de `SecurityConfig` — paramètres : `userDetailsService` (UserDetailsServiceImpl), `jwtFilter` (JwtFilter) (injection des dépendances par Spring)
    public SecurityConfig(UserDetailsServiceImpl userDetailsService, JwtFilter jwtFilter) {
        // Initialise l'attribut `userDetailsService` avec la valeur de userDetailsService
        this.userDetailsService = userDetailsService;
        // Initialise l'attribut `jwtFilter` avec la valeur de jwtFilter
        this.jwtFilter = jwtFilter;
    }

    // Déclare un bean géré par le conteneur Spring
    @Bean
    // Méthode `securityFilterChain` (publique) — paramètres : `http` (HttpSecurity) ; retourne : SecurityFilterChain
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Retourne la valeur de http
        return http
            // Enchaînement : appelle `cors(cors -> cors.configurationSource(corsConfiguratio…`
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Enchaînement : appelle `csrf(csrf -> csrf.ignoringRequestMatchers("/**"))`
            .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
            // Enchaînement : appelle `sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolic…`
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Enchaînement : appelle `authorizeHttpRequests(auth -> auth`
            .authorizeHttpRequests(auth -> auth
                // Endpoints d'authentification strictement publics
                .requestMatchers(
                    // Paire clé/valeur : clé « /auth/login » associée à 
                    "/auth/login",
                    // Paire clé/valeur : clé « /auth/register/** » associée à 
                    "/auth/register/**",
                    // Paire clé/valeur : clé « /auth/otp/** » associée à 
                    "/auth/otp/**",
                    // Paire clé/valeur : clé « /auth/password/** » associée à 
                    "/auth/password/**",
                    // Paire clé/valeur : clé « /auth/refresh-token » associée à 
                    "/auth/refresh-token",
                    // Paire clé/valeur : clé « /auth/verify-id » associée à 
                    "/auth/verify-id",
                    // Paire clé/valeur : clé « /actuator/health » associée à 
                    "/actuator/health",
                    // Paire clé/valeur : clé « /actuator/health/** » associée à 
                    "/actuator/health/**",
                    // Texte « /actuator/info »
                    "/actuator/info"
                // Enchaînement : appelle `permitAll()`
                ).permitAll()
                // Routes d'administration et d'audit réservées strictement aux administrateurs
                .requestMatchers(
                    // Paire clé/valeur : clé « /auth/admin/** » associée à 
                    "/auth/admin/**",
                    // Texte « /auth/audit/** »
                    "/auth/audit/**"
                // Enchaînement : appelle `hasRole("ADMIN")`
                ).hasRole("ADMIN")
                // Tout autre endpoint requiert une authentification valide
                .anyRequest().authenticated()
            )
            // Enchaînement : appelle `authenticationProvider(authenticationProvider())`
            .authenticationProvider(authenticationProvider())
            // Enchaînement : appelle `addFilterBefore(jwtFilter, org.springframework.security.web.authe…`
            .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            // Enchaînement : appelle `build();`
            .build();
    }

    // Déclare un bean géré par le conteneur Spring
    @Bean
    // Méthode `corsConfigurationSource` (publique) — sans paramètre ; retourne : CorsConfigurationSource
    public CorsConfigurationSource corsConfigurationSource() {
        // Déclare la variable `config` (CorsConfiguration) initialisée avec une nouvelle instance de CorsConfiguration
        CorsConfiguration config = new CorsConfiguration();
        // Boucle `for` : String origin : allowedOrigins.split(","))
        for (String origin : allowedOrigins.split(",")) {
            // Déclare la variable `trimmed` (chaîne de caractères) initialisée avec `origin.trim()`
            String trimmed = origin.trim();
            // Condition : exécute le bloc suivant seulement si `!trimmed.isEmpty()`
            if (!trimmed.isEmpty()) {
                // Appelle la méthode `addAllowedOrigin` sur `config` : config.addAllowedOrigin(trimmed);
                config.addAllowedOrigin(trimmed);
            }
        }
        // Renseigne la propriété AllowedMethods de `config` avec `List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")`
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Renseigne la propriété AllowedHeaders de `config` avec `List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")`
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        // Renseigne la propriété AllowCredentials de `config` avec le booléen faux
        config.setAllowCredentials(false);
        // Renseigne la propriété MaxAge de `config` avec la valeur numérique 3600L
        config.setMaxAge(3600L);

        // Déclare la variable `source` (UrlBasedCorsConfigurationSource) initialisée avec une nouvelle instance de UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appelle la méthode `registerCorsConfiguration` sur `source` : source.registerCorsConfiguration("/**", config);
        source.registerCorsConfiguration("/**", config);
        // Retourne la valeur de source
        return source;
    }

    // Déclare un bean géré par le conteneur Spring
    @Bean
    // Méthode `authenticationProvider` (publique) — sans paramètre ; retourne : AuthenticationProvider
    public AuthenticationProvider authenticationProvider() {
        // Déclare la variable `provider` (DaoAuthenticationProvider) initialisée avec une nouvelle instance de DaoAuthenticationProvider
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Renseigne la propriété UserDetailsService de `provider` avec la valeur de userDetailsService
        provider.setUserDetailsService(userDetailsService);
        // Renseigne la propriété PasswordEncoder de `provider` avec `passwordEncoder()`
        provider.setPasswordEncoder(passwordEncoder());
        // Retourne la valeur de provider
        return provider;
    }

    // Déclare un bean géré par le conteneur Spring
    @Bean
    // Méthode `passwordEncoder` (publique) — sans paramètre ; retourne : PasswordEncoder
    public PasswordEncoder passwordEncoder() {
        // Retourne une nouvelle instance de BCryptPasswordEncoder
        return new BCryptPasswordEncoder();
    }

    // Déclare un bean géré par le conteneur Spring
    @Bean
    // Méthode `authenticationManager` (publique) ; retourne : AuthenticationManager
    public AuthenticationManager authenticationManager(
            // Paramètre `config` de type AuthenticationConfiguration
            AuthenticationConfiguration config) throws Exception {
        // Retourne la valeur de l'attribut AuthenticationManager de config
        return config.getAuthenticationManager();
    }
}
