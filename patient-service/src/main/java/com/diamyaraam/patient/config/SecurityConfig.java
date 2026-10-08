// Déclaration du package Java : `com.diamyaraam.patient.config`
package com.diamyaraam.patient.config;

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
// Import de la classe `SessionCreationPolicy` (paquet org.springframework.security.config.http)
import org.springframework.security.config.http.SessionCreationPolicy;
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

    // Injecte la valeur de la propriété de configuration indiquée : ${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}
    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    // Attribut `allowedOrigins` de type chaîne de caractères [privée]
    private String allowedOrigins;

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
                // Enchaînement : appelle `authorizeHttpRequests(auth -> auth.anyRequest().permitAll())`
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
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
        // Renseigne la propriété AllowedMethods de `config` avec `List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")`
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
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
}
