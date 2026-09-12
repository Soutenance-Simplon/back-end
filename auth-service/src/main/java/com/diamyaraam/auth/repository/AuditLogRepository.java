package com.diamyaraam.auth.repository;

import com.diamyaraam.auth.entity.AuditLog;
import com.diamyaraam.auth.entity.AuditLog.ActionType;
import com.diamyaraam.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
    List<AuditLog> findByUserAndActionType(User user, ActionType type);
    long countByUserAndActionTypeAndCreatedAtAfter(User user, ActionType type, LocalDateTime since);
    List<AuditLog> findByTelephoneTente(String telephone);
}
