package com.diamyaraam.auth.security;

import com.diamyaraam.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * SERVICE : JWT — génération et validation des tokens
 *
 * Utilisé par :
 *   - AuthService pour générer un token après login
 *   - JwtFilter pour valider le token sur chaque requête
 *   - API Gateway pour rejeter les requêtes non authentifiées
 *
 * Structure du JWT généré :
 *   Header  : { "alg": "HS256" }
 *   Payload : { "sub": "user-uuid", "role": "PATIENT",
 *               "telephone": "+221771234567", "iat": ..., "exp": ... }
 *   Signature : HMAC-SHA256(secret)
 */
@Service
public class JwtService {

    // Injecté depuis application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // Génère la clé de signature à partir de la chaîne secrète
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Génère un token JWT pour l'utilisateur connecté.
     *
     * @param user l'utilisateur authentifié
     * @return le token JWT signé
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        // On ajoute des claims supplémentaires au payload
        claims.put("role", user.getRole() != null ? user.getRole().getNomRole() : "");
        claims.put("telephone", user.getTelephone());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())   // "sub" = UUID de l'utilisateur
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Génère un refresh token de longue durée (ex: 7 jours).
     */
    public String generateRefreshToken(User user) {
        long refreshExpirationMs = expirationMs * 7;
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait tous les claims du token JWT.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrait le subject (UUID utilisateur) du token.
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrait le rôle du token.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Vérifie si le token est encore valide (non expiré, bien signé).
     *
     * @return true si valide
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
