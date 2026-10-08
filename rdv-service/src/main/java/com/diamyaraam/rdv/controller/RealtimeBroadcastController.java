// Déclaration du package Java : `com.diamyaraam.rdv.controller`
package com.diamyaraam.rdv.controller;

// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de la classe `SimpMessagingTemplate` (paquet org.springframework.messaging.simp)
import org.springframework.messaging.simp.SimpMessagingTemplate;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `Map` (paquet java.util)
import java.util.Map;

/**
 * Contrôleur de diffusion temps réel universel.
 * Permet aux autres microservices (notification, wallet, dossier) de diffuser
 * des événements STOMP vers les clients Flutter via un simple POST HTTP.
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode
@RequestMapping
// Déclaration de la classe `RealtimeBroadcastController` (rôle : expose des routes HTTP)
public class RealtimeBroadcastController {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(RealtimeBroadcastController.class)`
    private static final Logger log = LoggerFactory.getLogger(RealtimeBroadcastController.class);
    // Attribut `messagingTemplate` de type SimpMessagingTemplate — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final SimpMessagingTemplate messagingTemplate;

    // Constructeur de `RealtimeBroadcastController` — paramètres : `messagingTemplate` (SimpMessagingTemplate) (injection des dépendances par Spring)
    public RealtimeBroadcastController(SimpMessagingTemplate messagingTemplate) {
        // Initialise l'attribut `messagingTemplate` avec la valeur de messagingTemplate
        this.messagingTemplate = messagingTemplate;
    }

    // Déclaration de l'enregistrement (record) `BroadcastRequest`
    public record BroadcastRequest(String destination, String type, Object data) {}

    // Route HTTP POST sur le chemin « /ws-broadcast »
    @PostMapping({"/ws-broadcast", "/api/realtime/broadcast"})
    // Méthode `broadcast` (publique) — paramètres : `request` (BroadcastRequest) ; retourne : réponse HTTP contenant dictionnaire clé/valeur
    public ResponseEntity<Map<String, Object>> broadcast(@RequestBody BroadcastRequest request) {
        // Condition : exécute le bloc suivant seulement si `request.destination() != null && !request.destination().isBlank()`
        if (request.destination() != null && !request.destination().isBlank()) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Appelle la méthode `convertAndSend` sur `messagingTemplate` : messagingTemplate.convertAndSend(request.destination(), request);
                messagingTemplate.convertAndSend(request.destination(), request);
                // Écrit un message informatif dans les journaux : "📡 [WebSocket Broadcast] Destination: {}, Type: {}", request.destinat…
                log.info("📡 [WebSocket Broadcast] Destination: {}, Type: {}", request.destination(), request.type());
                // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(Map.of("status", "SUCCESS", "destination", request.…
                return ResponseEntity.ok(Map.of("status", "SUCCESS", "destination", request.destination()));
            // Interception de l'exception Exception e
            } catch (Exception e) {
                // Écrit un message d'erreur dans les journaux : "❌ [WebSocket Broadcast] Erreur lors de l'envoi: {}", e.getMessage());
                log.error("❌ [WebSocket Broadcast] Erreur lors de l'envoi: {}", e.getMessage());
                // Retourne `ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", …`
                return ResponseEntity.internalServerError().body(Map.of("status", "ERROR", "message", e.getMessage()));
            }
        }
        // Retourne `ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "destinat…`
        return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", "destination is required"));
    }
}
