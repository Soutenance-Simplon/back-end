package com.diamyaraam.rdv.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8088,http://localhost:3000}")
    private String allowedOrigins;

    @Value("${websocket.auth.required:true}")
    private boolean wsAuthRequired;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker en mémoire (pas besoin de RabbitMQ/ActiveMQ)
        registry.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés par le client vers le serveur
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .withSockJS(); // Fallback SockJS pour les navigateurs incompatibles
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        String passcode = accessor.getFirstNativeHeader("passcode");
                        if (passcode != null && !passcode.isBlank()) {
                            authHeader = passcode.startsWith("Bearer ") ? passcode : "Bearer " + passcode.trim();
                        }
                    }

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                            Claims claims = Jwts.parser()
                                    .verifyWith(key)
                                    .build()
                                    .parseSignedClaims(token)
                                    .getPayload();

                            String tokenType = claims.get("type", String.class);
                            if ("REFRESH".equalsIgnoreCase(tokenType)) {
                                log.warn("Rejet connexion STOMP : tentative d'utilisation d'un Refresh Token");
                                throw new AccessDeniedException("Les refresh tokens ne sont pas autorisés pour la connexion WebSocket");
                            }

                            String userId = claims.getSubject();
                            accessor.setUser(() -> userId);
                            log.info("Client WebSocket STOMP authentifié : userId={}", userId);
                        } catch (AccessDeniedException ade) {
                            throw ade;
                        } catch (Exception e) {
                            log.warn("Rejet connexion STOMP : token JWT invalide ou expiré ({})", e.getMessage());
                            throw new AccessDeniedException("Token JWT WebSocket invalide ou expiré");
                        }
                    } else {
                        // Rejet inconditionnel : le serveur n'accepte jamais silencieusement une connexion non authentifiée
                        log.warn("Rejet connexion STOMP : aucun token JWT valide fourni (Authorization manquant ou non Bearer)");
                        throw new AccessDeniedException("Authentification requise pour la connexion WebSocket STOMP");
                    }
                }
                return message;
            }
        });
    }
}
