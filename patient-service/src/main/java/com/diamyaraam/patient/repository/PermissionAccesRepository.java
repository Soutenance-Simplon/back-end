package com.diamyaraam.patient.repository;

import com.diamyaraam.patient.entity.PermissionAcces;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionAccesRepository extends JpaRepository<PermissionAcces, UUID> {
    List<PermissionAcces> findByDossierMedicalId(UUID dossierMedicalId);
    List<PermissionAcces> findByUtilisateurAutoriseId(UUID utilisateurAutoriseId);
}
