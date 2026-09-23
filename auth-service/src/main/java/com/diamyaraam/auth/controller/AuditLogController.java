package com.diamyaraam.auth.controller;

import com.diamyaraam.auth.entity.AuditLog;
import com.diamyaraam.auth.repository.AuditLogRepository;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.diamyaraam.auth.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogController(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public record AuditLogDto(
        UUID id,
        String actionType,
        String telephoneTente,
        String ipAddress,
        String details,
        Boolean success,
        LocalDateTime createdAt,
        UUID userId,
        String userName,
        String userTelephone,
        String userRole
    ) {}

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getRecentAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findTop100AuditLogs().stream()
                .limit(100)
                .toList();

        List<AuditLogDto> dtos = logs.stream().map(log -> {
            UUID uId = null;
            String uName = null;
            String uTel = null;
            String uRole = null;
            try {
                if (log.getUser() != null) {
                    uId = log.getUser().getId();
                    String fn = log.getUser().getFirstName() != null ? log.getUser().getFirstName() : "";
                    String ln = log.getUser().getLastName() != null ? log.getUser().getLastName() : "";
                    uName = (fn + " " + ln).trim();
                    uTel = log.getUser().getTelephone();
                    if (log.getUser().getRole() != null) {
                        uRole = log.getUser().getRole().getNomRole();
                    }
                }
            } catch (Exception ignored) {
            }
            return new AuditLogDto(
                log.getId(),
                log.getActionType() != null ? log.getActionType().name() : null,
                log.getTelephoneTente(),
                log.getIpAddress(),
                log.getDetails(),
                log.getSuccess(),
                log.getCreatedAt(),
                uId,
                uName != null && !uName.isBlank() ? uName : null,
                uTel,
                uRole
            );
        }).toList();

        return ResponseEntity.ok(ApiResponse.success("Derniers journaux d'audit", dtos));
    }

    @PostMapping("/log")
    public ResponseEntity<ApiResponse<AuditLogDto>> createAuditLog(
            @RequestParam String actionType,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String telephoneTente,
            @RequestParam(defaultValue = "true") Boolean success) {

        AuditLog log = new AuditLog();
        try {
            log.setActionType(AuditLog.ActionType.valueOf(actionType));
        } catch (Exception e) {
            log.setActionType(AuditLog.ActionType.ACCES_DOSSIER_MEDECIN);
        }
        if (userId != null) {
            userRepository.findById(userId).ifPresent(log::setUser);
        }
        log.setDetails(details);
        log.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
        log.setTelephoneTente(telephoneTente);
        log.setSuccess(success != null ? success : true);
        AuditLog saved = auditLogRepository.save(log);

        String uName = null;
        String uTel = null;
        String uRole = null;
        if (saved.getUser() != null) {
            String fn = saved.getUser().getFirstName() != null ? saved.getUser().getFirstName() : "";
            String ln = saved.getUser().getLastName() != null ? saved.getUser().getLastName() : "";
            uName = (fn + " " + ln).trim();
            uTel = saved.getUser().getTelephone();
            if (saved.getUser().getRole() != null) {
                uRole = saved.getUser().getRole().getNomRole();
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Événement consigné avec succès", new AuditLogDto(
            saved.getId(),
            saved.getActionType().name(),
            saved.getTelephoneTente(),
            saved.getIpAddress(),
            saved.getDetails(),
            saved.getSuccess(),
            saved.getCreatedAt(),
            userId,
            uName,
            uTel,
            uRole
        )));
    }
}

