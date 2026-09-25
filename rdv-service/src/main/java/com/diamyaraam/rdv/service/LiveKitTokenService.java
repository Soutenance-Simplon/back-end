package com.diamyaraam.rdv.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de génération des jetons d'accès WebRTC pour LiveKit Cloud.
 * Conforme à la spécification officielle des Access Tokens LiveKit (JWT signés en HS256).
 */
@Service
public class LiveKitTokenService {

    @Value("${livekit.url:wss://diamyaram-3e670ked.livekit.cloud}")
    private String livekitUrl;

    @Value("${livekit.api-key:APIHHuyhcofZoip}")
    private String apiKey;

    @Value("${livekit.api-secret:JDgn9j9M35DVRcnmAntd4An2vVfZ4e18QYYy8Ho4NOQ}")
    private String apiSecret;

    public String getLivekitUrl() {
        return livekitUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Génère un jeton d'accès LiveKit pour un participant à une salle de téléconsultation.
     *
     * @param identity    Identifiant unique du participant (ex: UUID du patient ou du médecin)
     * @param displayName Nom affiché dans la visio (ex: "Dr. Cheikh Fall" ou "Oumar Diallo")
     * @param roomName    Nom unique de la salle (ex: "dy_room_da6b717c...")
     * @return Le token JWT LiveKit signé
     */
    public String createToken(String identity, String displayName, String roomName) {
        // Durée de validité : 6 heures
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + (6 * 3600 * 1000));

        // Grants vidéo LiveKit
        Map<String, Object> videoGrants = new HashMap<>();
        videoGrants.put("room", roomName);
        videoGrants.put("roomJoin", true);
        videoGrants.put("canPublish", true);
        videoGrants.put("canSubscribe", true);
        videoGrants.put("canPublishData", true);

        // Clé de signature HMAC-SHA256 sécurisée
        SecretKey signingKey = getSigningKey(apiSecret);

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .issuer(apiKey)
                .subject(identity)
                .claim("name", displayName != null ? displayName : identity)
                .claim("video", videoGrants)
                .notBefore(now)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    private SecretKey getSigningKey(String secret) {
        try {
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (secretBytes.length < 32) {
                // Si la clé fournie fait moins de 32 octets, on utilise un digest SHA-256 pour satisfaire HMAC-SHA256
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                secretBytes = md.digest(secretBytes);
            }
            return Keys.hmacShaKeyFor(secretBytes);
        } catch (Exception e) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
