package com.diamyaraam.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Configuration de sécurité réactive (WebFlux) pour l'API Gateway.
 * Point de contrôle centralisé (Policy Enforcement Point) :
 * - Validation cryptographique stricte des JWT HS256 signés par auth-service
 * - Interdiction de l'utilisation d'un Refresh Token comme Access Token d'API
 * - Contrôle d'accès basé sur les rôles (RBAC) : /api/auth/admin/** exige ROLE_ADMIN
 * - Exclusion des routes publiques (/login, /register, /otp, /password, /refresh)
 * - Exclusion des WebSockets (/ws-rdv/**, /ws/**) pour permettre le handshake STOMP
 * - Rejet strict (HTTP 401/403) de toute requête non conforme
 * - Gestion CORS configurable et sécurisée sans wildcard permissif
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Autoriser les requêtes préliminaires CORS (preflight OPTIONS)
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                // Endpoints d'authentification strictement publics (login, inscription, OTP, mot de passe)
                .pathMatchers(
                    "/api/auth/login",
                    "/api/auth/register/**",
                    "/api/auth/otp/**",
                    "/api/auth/password/**",
                    "/api/auth/refresh-token",
                    "/api/auth/verify-id"
                ).permitAll()
                // Monitoring Actuator public minimal (santé et infos)
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/health/**",
                    "/actuator/info"
                ).permitAll()
                // Handshake WebSockets / SockJS
                .pathMatchers("/ws-rdv/**", "/ws/**").permitAll()
                // Routes administratives : strictement réservées au rôle ADMIN (RBAC)
                .pathMatchers("/api/auth/admin/**", "/api/admin/**").hasRole("ADMIN")
                // Toutes les autres routes exigent un JWT Bearer valide
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .build();
    }

    /**
     * Convertisseur réactif extrayant le claim 'role' du JWT vers les GrantedAuthorities Spring Security (ROLE_<ROLE>).
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            String role = jwt.getClaimAsString("role");
            if (role != null && !role.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
            }
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

    /**
     * Décodeur JWT réactif pour validation de la signature HMAC-SHA256.
     * Valide l'algorithme, la clé et vérifie qu'un Refresh Token n'est pas utilisé comme Access Token.
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();

        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> notRefreshTokenValidator = new JwtClaimValidator<String>(
                "type",
                type -> type == null || !"REFRESH".equalsIgnoreCase(type)
        );
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, notRefreshTokenValidator));

        return decoder;
    }

    /**
     * Source de configuration CORS réactive.
     * Applique les origines autorisées explicitement configurées.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        for (String origin : allowedOrigins.split(",")) {
            String trimmed = origin.trim();
            if (!trimmed.isEmpty()) {
                config.addAllowedOrigin(trimmed);
            }
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
