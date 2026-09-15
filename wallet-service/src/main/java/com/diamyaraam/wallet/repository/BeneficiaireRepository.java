package com.diamyaraam.wallet.repository;

import com.diamyaraam.wallet.entity.Beneficiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, UUID> {
    List<Beneficiaire> findByPortefeuilleId(UUID portefeuilleId);
    Optional<Beneficiaire> findByPortefeuilleIdAndBeneficiaireUserId(UUID portefeuilleId, UUID beneficiaireUserId);
    List<Beneficiaire> findByBeneficiaireUserIdAndStatut(UUID beneficiaireUserId, Beneficiaire.StatutBeneficiaire statut);
}
