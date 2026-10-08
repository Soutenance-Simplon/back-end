// Déclaration du package Java : `com.diamyaraam.notification.controller`
package com.diamyaraam.notification.controller;

// Import de la classe `Notification` (paquet com.diamyaraam.notification.entity)
import com.diamyaraam.notification.entity.Notification;
// Import de la classe `NotificationService` (paquet com.diamyaraam.notification.service)
import com.diamyaraam.notification.service.NotificationService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Contrôleur REST : les valeurs retournées sont sérialisées en JSON
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /notifications »
@RequestMapping("/notifications")
// Déclaration de la classe `NotificationController` (rôle : expose des routes HTTP)
public class NotificationController {

    // Attribut `notificationService` de type NotificationService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final NotificationService notificationService;

    // Constructeur de `NotificationController` — paramètres : `notificationService` (NotificationService) (injection des dépendances par Spring)
    public NotificationController(NotificationService notificationService) {
        // Initialise l'attribut `notificationService` avec la valeur de notificationService
        this.notificationService = notificationService;
    }

    // Route HTTP POST sur le chemin « /send »
    @PostMapping("/send")
    // Méthode `send` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Notification ; intention : envoie (send)
    public ResponseEntity<ApiResponse<Notification>> send(
            // Paramètre `destinataireId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID destinataireId,
            // Paramètre `type` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String type,
            // Paramètre `titre` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String titre,
            // Paramètre `message` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String message,
            // Paramètre `rdvId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) UUID rdvId,
            // Paramètre `canal` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "IN_APP") String canal) {
        // Déclare la variable `t` (Notification.TypeNotification) initialisée avec `Notification.TypeNotification.valueOf(type)`
        Notification.TypeNotification t = Notification.TypeNotification.valueOf(type);
        // Déclare la variable `c` (Notification.Canal) initialisée avec `Notification.Canal.valueOf(canal)`
        Notification.Canal c = Notification.Canal.valueOf(canal);
        // Déclare la variable `notif` (Notification) initialisée avec `notificationService.sendNotification(destinataireId, t, titre, message, rdvId, …`
        Notification notif = notificationService.sendNotification(destinataireId, t, titre, message, rdvId, c);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Notification envoyée", notif))
        return ResponseEntity.ok(ApiResponse.success("Notification envoyée", notif));
    }

    // Route HTTP GET sur le chemin « /user/{userId} »
    @GetMapping("/user/{userId}")
    // Méthode `getByUserId` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Notification ; intention : récupère (get by user id)
    public ResponseEntity<ApiResponse<List<Notification>>> getByUserId(@PathVariable UUID userId) {
        // Déclare la variable `list` (liste de Notification) initialisée avec `notificationService.getUserNotifications(userId)`
        List<Notification> list = notificationService.getUserNotifications(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Notifications utilisateur", li…
        return ResponseEntity.ok(ApiResponse.success("Notifications utilisateur", list));
    }

    // Route HTTP PUT sur le chemin « /{id}/read »
    @PutMapping({"/{id}/read", "/{id}/lire"})
    // Méthode `markAsRead` (publique) — paramètres : `id` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        // Appelle la méthode `markAsRead` sur `notificationService` : notificationService.markAsRead(id);
        notificationService.markAsRead(id);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue…
        return ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue."));
    }
}
