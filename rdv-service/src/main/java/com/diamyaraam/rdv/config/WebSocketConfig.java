// Déclaration du package Java : `com.diamyaraam.rdv.config`
package com.diamyaraam.rdv.config;

// Import de la classe `Claims` (paquet io.jsonwebtoken)
import io.jsonwebtoken.Claims;
// Import de la classe `Jwts` (paquet io.jsonwebtoken)
import io.jsonwebtoken.Jwts;
// Import de la classe `Keys` (paquet io.jsonwebtoken.security)
import io.jsonwebtoken.security.Keys;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `Value` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Value;
// Import de la classe `Configuration` (paquet org.springframework.context.annotation)
import org.springframework.context.annotation.Configuration;
// Import de la classe `Message` (paquet org.springframework.messaging)
import org.springframework.messaging.Message;
// Import de la classe `MessageChannel` (paquet org.springframework.messaging)
import org.springframework.messaging.MessageChannel;
// Import de la classe `ChannelRegistration` (paquet org.springframework.messaging.simp.config)
import org.springframework.messaging.simp.config.ChannelRegistration;
// Import de la classe `MessageBrokerRegistry` (paquet org.springframework.messaging.simp.config)
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
// Import de la classe `StompCommand` (paquet org.springframework.messaging.simp.stomp)
import org.springframework.messaging.simp.stomp.StompCommand;
// Import de la classe `StompHeaderAccessor` (paquet org.springframework.messaging.simp.stomp)
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
// Import de la classe `ChannelInterceptor` (paquet org.springframework.messaging.support)
import org.springframework.messaging.support.ChannelInterceptor;
// Import de la classe `MessageHeaderAccessor` (paquet org.springframework.messaging.support)
import org.springframework.messaging.support.MessageHeaderAccessor;
// Import de la classe `AccessDeniedException` (paquet org.springframework.security.access)
import org.springframework.security.access.AccessDeniedException;
// Import de la classe `EnableWebSocketMessageBroker` (paquet org.springframework.web.socket.config.annotation)
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// Import de la classe `StompEndpointRegistry` (paquet org.springframework.web.socket.config.annotation)
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
// Import de la classe `WebSocketMessageBrokerConfigurer` (paquet org.springframework.web.socket.config.annotation)
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Import de la classe `SecretKey` (paquet javax.crypto)
import javax.crypto.SecretKey;
// Import de la classe `StandardCharsets` (paquet java.nio.charset)
import java.nio.charset.StandardCharsets;
// Import de la classe `Arrays` (paquet java.util)
import java.util.Arrays;

/**
 * Configuration WebSocket STOMP.
 * - Endpoint de connexion : ws://localhost:8084/ws  (direct au service)
 *                           ws://localhost:8090/ws-rdv/** (via gateway)
 * - Topics (push serveur→client) : /topic/rdv-updates
 * - App (client→serveur)         : /app/...
 *
 * Sécurité STOMP :
 * - Le handshake HTTP/SockJS est autorisé sans token HTTP (pour compatibilité navigateur SockJS)
 * - La trame STOMP CONNECT est interceptée et authentifiée via le header 'Authorization: Bearer <jwt>'
 */
@Configuration
// Active le courtier de messages WebSocket (STOMP)
@EnableWebSocketMessageBroker
// Déclaration de la classe `WebSocketConfig`, et implémente WebSocketMessageBrokerConfigurer (rôle : porte la configuration)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(WebSocketConfig.class)`
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    // Injecte la valeur de la propriété de configuration indiquée : ${jwt.secret}
    @Value("${jwt.secret}")
    // Attribut `jwtSecret` de type chaîne de caractères [privée]
    private String jwtSecret;

    // Injecte la valeur de la propriété de configuration indiquée : ${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    // Attribut `allowedOrigins` de type chaîne de caractères [privée]
    private String allowedOrigins;

    // Injecte la valeur de la propriété de configuration indiquée : ${websocket.auth.required:true}
    @Value("${websocket.auth.required:true}")
    // Attribut `wsAuthRequired` de type booléen [privée]
    private boolean wsAuthRequired;

    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `configureMessageBroker` (publique) — paramètres : `registry` (MessageBrokerRegistry) ; retourne : aucune valeur
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker en mémoire (pas besoin de RabbitMQ/ActiveMQ)
        registry.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés par le client vers le serveur
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `registerStompEndpoints` (publique) — paramètres : `registry` (StompEndpointRegistry) ; retourne : aucune valeur ; intention : inscrit (register stomp endpoints)
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Déclare la variable `origins` (String[]) initialisée avec `Arrays.stream(allowedOrigins.split(","))`
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                // Enchaînement : appelle `map(String::trim)`
                .map(String::trim)
                // Enchaînement : appelle `filter(s -> !s.isEmpty())`
                .filter(s -> !s.isEmpty())
                // Enchaînement : appelle `toArray(String[]::new);`
                .toArray(String[]::new);
        // Appelle la méthode `addEndpoint` sur `registry` : registry.addEndpoint("/ws")
        registry.addEndpoint("/ws")
                // Enchaînement : appelle `setAllowedOrigins(origins)`
                .setAllowedOrigins(origins)
                // Enchaînement : appelle `withSockJS();`
                .withSockJS(); // Fallback SockJS pour les navigateurs incompatibles
    }

    // Redéfinit une méthode héritée de la classe parente ou de l'interface
    @Override
    // Méthode `configureClientInboundChannel` (publique) — paramètres : `registration` (ChannelRegistration) ; retourne : aucune valeur
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Appelle la méthode `interceptors` sur `registration` : registration.interceptors(new ChannelInterceptor() {
        registration.interceptors(new ChannelInterceptor() {
            // Redéfinit une méthode héritée de la classe parente ou de l'interface
            @Override
            // Instruction : public Message<?> preSend(Message<?> message, MessageChannel channel) {
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // Déclare la variable `accessor` (StompHeaderAccessor) initialisée avec 
                StompHeaderAccessor accessor =
                        // Argument/valeur : `MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class`
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Condition : exécute le bloc suivant seulement si `accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())`
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Déclare la variable `authHeader` (chaîne de caractères) initialisée avec `accessor.getFirstNativeHeader("Authorization")`
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    // Condition : exécute le bloc suivant seulement si `authHeader == null || !authHeader.startsWith("Bearer ")`
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        // Déclare la variable `passcode` (chaîne de caractères) initialisée avec `accessor.getFirstNativeHeader("passcode")`
                        String passcode = accessor.getFirstNativeHeader("passcode");
                        // Condition : exécute le bloc suivant seulement si `passcode != null && !passcode.isBlank()`
                        if (passcode != null && !passcode.isBlank()) {
                            // Affecte à `authHeader` `passcode.startsWith("Bearer ") ? passcode : "Bearer " + passcode.trim()`
                            authHeader = passcode.startsWith("Bearer ") ? passcode : "Bearer " + passcode.trim();
                        }
                    }

                    // Condition : exécute le bloc suivant seulement si `authHeader != null && authHeader.startsWith("Bearer ")`
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        // Déclare la variable `token` (chaîne de caractères) initialisée avec `authHeader.substring(7)`
                        String token = authHeader.substring(7);
                        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                        try {
                            // Déclare la variable `key` (SecretKey) initialisée avec `Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))`
                            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                            // Déclare la variable `claims` (Claims) initialisée avec `Jwts.parser()`
                            Claims claims = Jwts.parser()
                                    // Enchaînement : appelle `verifyWith(key)`
                                    .verifyWith(key)
                                    // Enchaînement : appelle `build()`
                                    .build()
                                    // Enchaînement : appelle `parseSignedClaims(token)`
                                    .parseSignedClaims(token)
                                    // Enchaînement : appelle `getPayload();`
                                    .getPayload();

                            // Déclare la variable `tokenType` (chaîne de caractères) initialisée avec `claims.get("type", String.class)`
                            String tokenType = claims.get("type", String.class);
                            // Condition : exécute le bloc suivant seulement si `"REFRESH".equalsIgnoreCase(tokenType)`
                            if ("REFRESH".equalsIgnoreCase(tokenType)) {
                                // Écrit un message d'avertissement dans les journaux : "Rejet connexion STOMP : tentative d'utilisation d'un Refresh Token");
                                log.warn("Rejet connexion STOMP : tentative d'utilisation d'un Refresh Token");
                                // Lève l'exception AccessDeniedException avec le message « Les refresh tokens ne sont pas autorisés pour la connexion WebSocket »
                                throw new AccessDeniedException("Les refresh tokens ne sont pas autorisés pour la connexion WebSocket");
                            }

                            // Déclare la variable `userId` (chaîne de caractères) initialisée avec la valeur de l'attribut Subject de claims
                            String userId = claims.getSubject();
                            // Renseigne la propriété User de `accessor` avec `() -> userId`
                            accessor.setUser(() -> userId);
                            // Écrit un message informatif dans les journaux : "Client WebSocket STOMP authentifié : userId={}", userId);
                            log.info("Client WebSocket STOMP authentifié : userId={}", userId);
                        // Interception de l'exception AccessDeniedException ade
                        } catch (AccessDeniedException ade) {
                            // Lève (relance) une exception : throw ade;
                            throw ade;
                        // Interception de l'exception Exception e
                        } catch (Exception e) {
                            // Écrit un message d'avertissement dans les journaux : "Rejet connexion STOMP : token JWT invalide ou expiré ({})", e.getMes…
                            log.warn("Rejet connexion STOMP : token JWT invalide ou expiré ({})", e.getMessage());
                            // Lève l'exception AccessDeniedException avec le message « Token JWT WebSocket invalide ou expiré »
                            throw new AccessDeniedException("Token JWT WebSocket invalide ou expiré");
                        }
                    // Sinon (cas contraire de la condition précédente)
                    } else {
                        // Rejet inconditionnel : le serveur n'accepte jamais silencieusement une connexion non authentifiée
                        log.warn("Rejet connexion STOMP : aucun token JWT valide fourni (Authorization manquant ou non Bearer)");
                        // Lève l'exception AccessDeniedException avec le message « Authentification requise pour la connexion WebSocket STOMP »
                        throw new AccessDeniedException("Authentification requise pour la connexion WebSocket STOMP");
                    }
                }
                // Retourne la valeur de message
                return message;
            }
        });
    }
}
