package com.diamyaraam.rdv.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur de diffusion temps réel universel.
 * Permet aux autres microservices (notification, wallet, dossier) de diffuser
 * des événements STOMP vers les clients Flutter via un simple POST HTTP.
 */
@RestController
@RequestMapping
public class RealtimeBroadcastController {

    private static final Logger log = LoggerFactory.getLogger(RealtimeBroadcastController.class);
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeBroadcastController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public record BroadcastRequest(String destination, String type, Object data) {}

    @PostMapping({"/ws-broadcast", "/api/realtime/broadcast"})
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody BroadcastRequest request) {
        if (request.destination() != null && !request.destination().isBlank()) {
            try {
                messagingTemplate.convertAndSend(request.destination(), request);
                log.info("📡 [WebSocket Broadcast] Destination: {}, Type: {}", request.destination(), request.type());
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "destination", request.destination()));
            } catch (Exception e) {
                log.error("❌ [WebSocket Broadcast] Erreur lors de l'envoi: {}", e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", e.getMessage()));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "destination is required"));
    }
}
