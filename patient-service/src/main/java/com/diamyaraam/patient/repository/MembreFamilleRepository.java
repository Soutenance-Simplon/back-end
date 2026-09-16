package com.diamyaraam.patient.repository;

import com.diamyaraam.patient.entity.MembreFamille;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MembreFamilleRepository extends JpaRepository<MembreFamille, UUID> {
    List<MembreFamille> findByParentUserId(UUID parentUserId);
}
