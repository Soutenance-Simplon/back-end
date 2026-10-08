// Déclaration du package Java : `com.diamyaraam.auth.security`
package com.diamyaraam.auth.security;

// Import de la classe `User` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.User;
// Import de la classe `Claims` (paquet io.jsonwebtoken)
import io.jsonwebtoken.Claims;
// Import de la classe `Jwts` (paquet io.jsonwebtoken)
import io.jsonwebtoken.Jwts;
// Import de la classe `Keys` (paquet io.jsonwebtoken.security)
import io.jsonwebtoken.security.Keys;
// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;

// Import de la classe `SecretKey` (paquet javax.crypto)
import javax.crypto.SecretKey;
// Import de la classe `StandardCharsets` (paquet java.nio.charset)
import java.nio.charset.StandardCharsets;
// Import de la classe `Date` (paquet java.util)
import java.util.Date;
// Import de la classe `HashMap` (paquet java.util)
import java.util.HashMap;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;

/**
 * ====================================================================================================
 * SERVICE DE SÉCURITÉ CRYPTOGRAPHIQUE : GESTION DES JETONS JWT (JWT SERVICE)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS CRYPTOGRAPHIQUES & ARCHITECTURE POUR LA SOUTENANCE :
 * Pourquoi utiliser le standard JWT (RFC 7519) dans Diam Yaraam ?
 * 
 * 1. Architecture Stateless (Sans État) :
 *    Dans un écosystème de microservices hautement disponible, stocker les sessions en mémoire
 *    côté serveur (HttpSession) obligerait à partager un cache Redis de session ou à utiliser
 *    du sticky-session fragile. Avec JWT, l'état de l'utilisateur (identifiant, rôle, statut)
 *    est auto-contenu (self-contained) dans le payload du jeton.
 * 
 * 2. Signature Cryptographique Inviolable :
 *    Le jeton est signé avec une clé secrète partagée via l'algorithme HMAC (Hash-based Message
 *    Authentication Code). Toute tentative de falsification du rôle (ex: changer PATIENT en ADMIN)
 *    invalide immédiatement la signature mathématique lors du contrôle.
 * 
 * 3. Stratégie à Deux Jetons (Access Token + Refresh Token) :
 *    - Access Token : Durée de vie courte (ex: 24 heures ou 1 heure) pour réduire la fenêtre de vulnérabilité.
 *    - Refresh Token : Durée de vie plus longue (7 jours) avec claim spécial `type: REFRESH`,
 *      conservé de manière sécurisée pour renouveler la session sans redemander les identifiants.
 * ====================================================================================================
 */
@Service
// Déclaration de la classe `JwtService` (rôle : porte la logique métier)
public class JwtService {

    /** Clé secrète binaire configurée dans les variables d'environnement */
    @Value("${jwt.secret}")
    // Attribut `secretKey` de type chaîne de caractères [privée]
    private String secretKey;

    /** Durée de validité nominale de l'Access Token en millisecondes */
    @Value("${jwt.expiration-ms}")
    // Attribut `expirationMs` de type entier long [privée]
    private long expirationMs;

    /**
     * Génère une clé cryptographique conforme pour l'algorithme HMAC.
     * 
     * @return Clé secrète sous forme de SecretKey Java Cryptography Architecture
     */
    private SecretKey getSigningKey() {
        // Retourne `Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8))`
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Génère un token JWT pour l'utilisateur connecté.
     *
     * @param user l'utilisateur authentifié
     * @return le token JWT signé
     */
    public String generateToken(User user) {
        // Déclare la variable `claims` (dictionnaire clé/valeur) initialisée avec une nouvelle instance de HashMap<>
        Map<String, Object> claims = new HashMap<>();

        // On ajoute des claims supplémentaires au payload
        claims.put("role", user.getRole() != null ? user.getRole().getNomRole() : "");
        // Appelle la méthode `put` sur `claims` : claims.put("telephone", user.getTelephone());
        claims.put("telephone", user.getTelephone());
        // Appelle la méthode `put` sur `claims` : claims.put("firstName", user.getFirstName());
        claims.put("firstName", user.getFirstName());
        // Appelle la méthode `put` sur `claims` : claims.put("lastName", user.getLastName());
        claims.put("lastName", user.getLastName());

        // Retourne `Jwts.builder()`
        return Jwts.builder()
                // Enchaînement : appelle `claims(claims)`
                .claims(claims)
                // Enchaînement : appelle `subject(user.getId().toString())`
                .subject(user.getId().toString())   // "sub" = UUID de l'utilisateur
                // Enchaînement : appelle `issuedAt(new Date())`
                .issuedAt(new Date())
                // Enchaînement : appelle `expiration(new Date(System.currentTimeMillis() + expirationM…`
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                // Enchaînement : appelle `signWith(getSigningKey())`
                .signWith(getSigningKey())
                // Enchaînement : appelle `compact();`
                .compact();
    }

    /**
     * Génère un refresh token de longue durée (ex: 7 jours).
     */
    public String generateRefreshToken(User user) {
        // Déclare la variable `refreshExpirationMs` (entier long) initialisée avec `expirationMs * 7`
        long refreshExpirationMs = expirationMs * 7;
        // Retourne `Jwts.builder()`
        return Jwts.builder()
                // Enchaînement : appelle `subject(user.getId().toString())`
                .subject(user.getId().toString())
                // Enchaînement : appelle `claim("type", "REFRESH")`
                .claim("type", "REFRESH")
                // Enchaînement : appelle `issuedAt(new Date())`
                .issuedAt(new Date())
                // Enchaînement : appelle `expiration(new Date(System.currentTimeMillis() + refreshExpi…`
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                // Enchaînement : appelle `signWith(getSigningKey())`
                .signWith(getSigningKey())
                // Enchaînement : appelle `compact();`
                .compact();
    }

    /**
     * Extrait tous les claims du token JWT.
     */
    public Claims extractAllClaims(String token) {
        // Retourne `Jwts.parser()`
        return Jwts.parser()
                // Enchaînement : appelle `verifyWith(getSigningKey())`
                .verifyWith(getSigningKey())
                // Enchaînement : appelle `build()`
                .build()
                // Enchaînement : appelle `parseSignedClaims(token)`
                .parseSignedClaims(token)
                // Enchaînement : appelle `getPayload();`
                .getPayload();
    }

    /**
     * Extrait le subject (UUID utilisateur) du token.
     */
    public String extractUserId(String token) {
        // Retourne `extractAllClaims(token).getSubject()`
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrait le rôle du token.
     */
    public String extractRole(String token) {
        // Retourne `extractAllClaims(token).get("role", String.class)`
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Vérifie si le token est encore valide (non expiré, bien signé).
     *
     * @return true si valide
     */
    public boolean isTokenValid(String token) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `claims` (Claims) initialisée avec `extractAllClaims(token)`
            Claims claims = extractAllClaims(token);
            // Retourne `!claims.getExpiration().before(new Date())`
            return !claims.getExpiration().before(new Date());
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne le booléen faux
            return false;
        }
    }

    // Méthode `getExpirationMs` (publique) — sans paramètre ; retourne : entier long ; intention : récupère (get expiration ms)
    public long getExpirationMs() {
        // Retourne la valeur de expirationMs
        return expirationMs;
    }
}
