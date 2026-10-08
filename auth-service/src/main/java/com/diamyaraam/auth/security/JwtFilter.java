// Déclaration du package Java : `com.diamyaraam.auth.security`
package com.diamyaraam.auth.security;

// Import de la classe `Claims` (paquet io.jsonwebtoken)
import io.jsonwebtoken.Claims;
// Import de la classe `FilterChain` (paquet jakarta.servlet)
import jakarta.servlet.FilterChain;
// Import de la classe `ServletException` (paquet jakarta.servlet)
import jakarta.servlet.ServletException;
// Import de la classe `HttpServletRequest` (paquet jakarta.servlet.http)
import jakarta.servlet.http.HttpServletRequest;
// Import de la classe `HttpServletResponse` (paquet jakarta.servlet.http)
import jakarta.servlet.http.HttpServletResponse;
// Import de la classe `UsernamePasswordAuthenticationToken` (paquet org.springframework.security.authentication)
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// Import de la classe `SimpleGrantedAuthority` (paquet org.springframework.security.core.authority)
import org.springframework.security.core.authority.SimpleGrantedAuthority;
// Import de la classe `SecurityContextHolder` (paquet org.springframework.security.core.context)
import org.springframework.security.core.context.SecurityContextHolder;
// Import de la classe `WebAuthenticationDetailsSource` (paquet org.springframework.security.web.authentication)
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// Import de la classe `Component` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Component;
// Import de la classe `OncePerRequestFilter` (paquet org.springframework.web.filter)
import org.springframework.web.filter.OncePerRequestFilter;

// Import de la classe `IOException` (paquet java.io)
import java.io.IOException;
// Import de la classe `Collections` (paquet java.util)
import java.util.Collections;
// Import de la classe `List` (paquet java.util)
import java.util.List;

/**
 * Filtre de validation JWT pour auth-service.
 * Assure la défense en profondeur :
 * - Extrait le token Bearer de l'en-tête Authorization
 * - Valide la signature et l'expiration via JwtService
 * - Interdit l'utilisation d'un Refresh Token pour les appels d'API
 * - Alimente le SecurityContext avec le Principal et l'autorité ROLE_<ROLE>
 */
@Component
// Déclaration de la classe `JwtFilter`, qui hérite de OncePerRequestFilter (rôle : filtre les requêtes HTTP)
public class JwtFilter extends OncePerRequestFilter {

    // Attribut `jwtService` de type JwtService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final JwtService jwtService;

    // Constructeur de `JwtFilter` — paramètres : `jwtService` (JwtService) (injection des dépendances par Spring)
    public JwtFilter(JwtService jwtService) {
        // Initialise l'attribut `jwtService` avec la valeur de jwtService
        this.jwtService = jwtService;
    }

    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `doFilterInternal` (protégée) ; retourne : aucune valeur
    protected void doFilterInternal(HttpServletRequest request,
                                    // Paramètre `response` de type HttpServletResponse
                                    HttpServletResponse response,
                                    // Paramètre `filterChain` de type FilterChain
                                    FilterChain filterChain) throws ServletException, IOException {

        // Déclare la variable `authHeader` (chaîne de caractères) initialisée avec `request.getHeader("Authorization")`
        String authHeader = request.getHeader("Authorization");
        // Condition : exécute le bloc suivant seulement si `authHeader != null && authHeader.startsWith("Bearer ")`
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Déclare la variable `token` (chaîne de caractères) initialisée avec `authHeader.substring(7)`
            String token = authHeader.substring(7);
            // Condition : exécute le bloc suivant seulement si `jwtService.isTokenValid(token)`
            if (jwtService.isTokenValid(token)) {
                // Déclare la variable `claims` (Claims) initialisée avec `jwtService.extractAllClaims(token)`
                Claims claims = jwtService.extractAllClaims(token);
                // Déclare la variable `tokenType` (chaîne de caractères) initialisée avec `claims.get("type", String.class)`
                String tokenType = claims.get("type", String.class);
                // Condition : exécute le bloc suivant seulement si `!"REFRESH".equalsIgnoreCase(tokenType)`
                if (!"REFRESH".equalsIgnoreCase(tokenType)) {
                    // Déclare la variable `userId` (chaîne de caractères) initialisée avec la valeur de l'attribut Subject de claims
                    String userId = claims.getSubject();
                    // Déclare la variable `role` (chaîne de caractères) initialisée avec `claims.get("role", String.class)`
                    String role = claims.get("role", String.class);
                    // Déclare la variable `authorities` (liste de SimpleGrantedAuthority) initialisée avec `(role != null && !role.isBlank())`
                    List<SimpleGrantedAuthority> authorities = (role != null && !role.isBlank())
                            // Suite de l'expression (opérateur) : ? List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                            ? List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                            // Instruction : : Collections.emptyList();
                            : Collections.emptyList();

                    // Déclare la variable `authentication` (UsernamePasswordAuthenticationToken) initialisée avec 
                    UsernamePasswordAuthenticationToken authentication =
                            // Suite de l'instruction précédente : new UsernamePasswordAuthenticationToken(userId, null, authorities);
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    // Renseigne la propriété Details de `authentication` avec une nouvelle instance de WebAuthenticationDetailsSource
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Appelle la méthode `getContext` sur `SecurityContextHolder` : SecurityContextHolder.getContext().setAuthentication(authentication);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // Appelle la méthode `doFilter` sur `filterChain` : filterChain.doFilter(request, response);
        filterChain.doFilter(request, response);
    }
}
