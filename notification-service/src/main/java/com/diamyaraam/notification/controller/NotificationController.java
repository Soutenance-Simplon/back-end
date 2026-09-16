package com.diamyaraam.notification.controller;

import com.diamyaraam.notification.entity.Notification;
import com.diamyaraam.notification.service.NotificationService;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Notification>> send(
            @RequestParam UUID destinataireId,
            @RequestParam String type,
            @RequestParam String titre,
            @RequestParam String message,
            @RequestParam(required = false) UUID rdvId,
            @RequestParam(defaultValue = "IN_APP") String canal) {
        Notification.TypeNotification t = Notification.TypeNotification.valueOf(type);
        Notification.Canal c = Notification.Canal.valueOf(canal);
        Notification notif = notificationService.sendNotification(destinataireId, t, titre, message, rdvId, c);
        return ResponseEntity.ok(ApiResponse.success("Notification envoyée", notif));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUserId(@PathVariable UUID userId) {
        List<Notification> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Notifications utilisateur", list));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marquée comme lue."));
    }
}
