package com.diamyaraam.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
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
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isTokenValid(token)) {
                Claims claims = jwtService.extractAllClaims(token);
                String tokenType = claims.get("type", String.class);
                if (!"REFRESH".equalsIgnoreCase(tokenType)) {
                    String userId = claims.getSubject();
                    String role = claims.get("role", String.class);
                    List<SimpleGrantedAuthority> authorities = (role != null && !role.isBlank())
                            ? List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
                            : Collections.emptyList();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
