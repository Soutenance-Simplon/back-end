// Déclaration du package Java : `com.diamyaraam.wallet.repository`
package com.diamyaraam.wallet.repository;

// Import de la classe `Beneficiaire` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Beneficiaire;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Repository` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Repository;

// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Interface d'accès aux données (couche repository Spring Data)
@Repository
// Déclaration de l'interface `BeneficiaireRepository` : repository Spring Data JPA de l'entité `Beneficiaire` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, UUID> {
    // Méthode abstraite (contrat) `findByPortefeuilleId` — paramètres : `portefeuilleId` (identifiant UUID) ; retourne : liste de Beneficiaire ; intention : recherche par (find by portefeuille id)
    List<Beneficiaire> findByPortefeuilleId(UUID portefeuilleId);
    // Méthode abstraite (contrat) `findByPortefeuilleIdAndBeneficiaireUserId` — paramètres : `portefeuilleId` (identifiant UUID), `beneficiaireUserId` (identifiant UUID) ; retourne : valeur optionnelle de Beneficiaire ; intention : recherche par (find by portefeuille id and beneficiaire user id)
    Optional<Beneficiaire> findByPortefeuilleIdAndBeneficiaireUserId(UUID portefeuilleId, UUID beneficiaireUserId);
    // Méthode abstraite (contrat) `findByBeneficiaireUserIdAndStatut` — paramètres : `beneficiaireUserId` (identifiant UUID), `statut` (Beneficiaire.StatutBeneficiaire) ; retourne : liste de Beneficiaire ; intention : recherche par (find by beneficiaire user id and statut)
    List<Beneficiaire> findByBeneficiaireUserIdAndStatut(UUID beneficiaireUserId, Beneficiaire.StatutBeneficiaire statut);
}
