package com.diamyaraam.wallet.repository;

import com.diamyaraam.wallet.entity.AuditFinancier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditFinancierRepository extends JpaRepository<AuditFinancier, UUID> {
    List<AuditFinancier> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
