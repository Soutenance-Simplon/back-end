package com.diamyaraam.medecin.repository;

import com.diamyaraam.medecin.entity.OnmsReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OnmsReferenceRepository extends JpaRepository<OnmsReference, Long> {
    Optional<OnmsReference> findByNumeroOrdre(String numeroOrdre);
    boolean existsByNumeroOrdre(String numeroOrdre);
}
