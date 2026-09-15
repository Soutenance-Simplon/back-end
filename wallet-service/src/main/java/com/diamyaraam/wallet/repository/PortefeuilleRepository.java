package com.diamyaraam.wallet.repository;

import com.diamyaraam.wallet.entity.Portefeuille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortefeuilleRepository extends JpaRepository<Portefeuille, UUID> {
    Optional<Portefeuille> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
