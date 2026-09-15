package com.diamyaraam.wallet.repository;

import com.diamyaraam.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByPortefeuilleIdOrderByDateTransactionDesc(UUID portefeuilleId);
    Optional<Transaction> findByReferenceExterne(String referenceExterne);
    List<Transaction> findByBeneficiaireUserId(UUID beneficiaireUserId);
}
