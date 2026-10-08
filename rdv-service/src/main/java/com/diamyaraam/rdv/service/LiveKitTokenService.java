// Déclaration du package Java : `com.diamyaraam.rdv.service`
package com.diamyaraam.rdv.service;

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
// Import de la classe `MessageDigest` (paquet java.security)
import java.security.MessageDigest;
// Import de la classe `Date` (paquet java.util)
import java.util.Date;
// Import de la classe `HashMap` (paquet java.util)
import java.util.HashMap;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;

/**
 * ====================================================================================================
 * SERVICE CRYPTOGRAPHIQUE WEBRTC : ACCÈS AUX SALLES DE TÉLÉCONSULTATION (LIVEKIT TOKEN SERVICE)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE WEBRTC POUR LA SOUTENANCE :
 * Comment fonctionne la visioconférence médicale dans Diam Yaraam sans passer par Zoom ou Google Meet ?
 * 
 * 📹 INFRASTRUCTURE WEBRTC & LIVEKIT CLOUD :
 * 1. WebRTC Peer-to-Peer & SFU (Selective Forwarding Unit) :
 *    LiveKit utilise un serveur SFU pour relayer les flux vidéo chiffrés avec une latence quasi-nulle (< 100ms)
 *    même sur les réseaux mobiles 3G/4G au Sénégal.
 * 
 * 2. Authentification par Jeton Décentralisé (LiveKit JWT Access Token) :
 *    Au lieu de transmettre des identifiants au serveur vidéo, le client Flutter reçoit un jeton JWT
 *    signé par notre backend avec la clé secrète partagée LiveKit.
 * 
 * 3. Permissions Granulaires (Video Grants) :
 *    - `room` : nom unique de la consultation (cloisonnement strict entre patients).
 *    - `roomJoin` : droit d'accéder à la salle.
 *    - `canPublish` : autorisation d'émettre le flux caméra et microphone.
 *    - `canSubscribe` : autorisation de recevoir le flux vidéo de l'interlocuteur.
 *    - `canPublishData` : canal de données textuelles pour le chat sécurisé durant la séance.
 * ====================================================================================================
 */
// Annotation Spring marquant la classe comme composant de service injectable
@Service
// Déclaration de la classe `LiveKitTokenService` (rôle : porte la logique métier)
public class LiveKitTokenService {

    // Injection de l'URL du serveur SFU LiveKit depuis les propriétés applicatives (ex: wss://diam-yaraam.livekit.cloud)
    @Value("${livekit.url:}")
    // Attribut `livekitUrl` de type chaîne de caractères [privée]
    private String livekitUrl;

    // Injection de la clé d'API publique émise par la console LiveKit
    @Value("${livekit.api-key:}")
    // Attribut `apiKey` de type chaîne de caractères [privée]
    private String apiKey;

    // Injection du secret cryptographique de signature partagé avec le serveur LiveKit
    @Value("${livekit.api-secret:}")
    // Attribut `apiSecret` de type chaîne de caractères [privée]
    private String apiSecret;

    // Accesseur public pour exposer l'URL du cluster vidéo aux clients mobiles
    public String getLivekitUrl() {
        // Renvoie l'URL WebSocket configurée
        return livekitUrl;
    }

    // Accesseur public pour exposer la clé d'API LiveKit
    public String getApiKey() {
        // Renvoie l'identifiant de la clé d'API
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
        // Capture du temps système courant en millisecondes
        long nowMillis = System.currentTimeMillis();
        // Date d'émission du jeton (Not Before & Issued At)
        Date now = new Date(nowMillis);
        // Date d'expiration fixée à 6 heures après l'émission (6h * 3600s * 1000ms)
        Date exp = new Date(nowMillis + (6 * 3600 * 1000));

        // Définition de la carte des habilitations vidéo (Video Grants) attendues par LiveKit
        Map<String, Object> videoGrants = new HashMap<>();
        // Délimitation de la visio au salon unique correspondant au rendez-vous
        videoGrants.put("room", roomName);
        // Autorisation formelle de se connecter à la salle
        videoGrants.put("roomJoin", true);
        // Droit d'émettre des flux audio (micro) et vidéo (caméra)
        videoGrants.put("canPublish", true);
        // Droit de souscrire aux flux multimédias des autres participants
        videoGrants.put("canSubscribe", true);
        // Droit d'échanger des paquets de données arbitraires (chat textuel, synchronisation)
        videoGrants.put("canPublishData", true);

        // Dérivation de la clé symétrique de signature HMAC-SHA256
        SecretKey signingKey = getSigningKey(apiSecret);

        // Construction du jeton JWT selon la spécification officielle LiveKit
        return Jwts.builder()
                // Définition de l'en-tête JWT typique
                .header().add("typ", "JWT").and()
                // L'émetteur (issuer) doit correspondre à la clé d'API LiveKit
                .issuer(apiKey)
                // Le sujet (subject) identifie de manière unique le participant
                .subject(identity)
                // Revendication personnalisée pour le libellé affiché au correspondant
                .claim("name", displayName != null ? displayName : identity)
                // Injection du dictionnaire des permissions vidéo
                .claim("video", videoGrants)
                // Validation temporelle de début de validité
                .notBefore(now)
                // Date d'émission officielle
                .issuedAt(now)
                // Date de péremption après 6 heures
                .expiration(exp)
                // Signature cryptographique avec la clé HMAC-SHA256
                .signWith(signingKey)
                // Sérialisation compacte du jeton en chaîne encodée en Base64URL
                .compact();
    }

    // Dérivation et sécurisation de la clé de signature binaire HMAC
    private SecretKey getSigningKey(String secret) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Conversion de la chaîne secrète en tableau d'octets UTF-8
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            // Si la clé fournie comporte moins de 256 bits (32 octets), hachage SHA-256 pour conformité RFC 7518
            if (secretBytes.length < 32) {
                // Instanciation de l'algorithme de hachage cryptographique SHA-256
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                // Dérivation d'une clé binaire robuste de 32 octets
                secretBytes = md.digest(secretBytes);
            }
            // Génération de la clé secrète HMAC pour la bibliothèque JJWT
            return Keys.hmacShaKeyFor(secretBytes);
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Repli direct sur les octets bruts en cas d'exception inattendue
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
