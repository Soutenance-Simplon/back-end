package com.diamyaraam.rdv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket STOMP.
 * - Endpoint de connexion : ws://localhost:8084/ws  (direct au service)
 *                           ws://localhost:8090/ws-rdv/** (via gateway)
 * - Topics (push serveur→client) : /topic/rdv-updates
 * - App (client→serveur)         : /app/...
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker en mémoire (pas besoin de RabbitMQ/ActiveMQ)
        registry.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés par le client vers le serveur
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback SockJS pour les navigateurs incompatibles
    }
}
