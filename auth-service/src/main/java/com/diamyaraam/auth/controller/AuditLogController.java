package com.diamyaraam.auth.controller;

import com.diamyaraam.auth.entity.AuditLog;
import com.diamyaraam.auth.repository.AuditLogRepository;
import com.diamyaraam.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs() {
        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(100)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Derniers journaux d'audit", logs));
    }
}
