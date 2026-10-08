// Déclaration du package Java : `com.diamyaraam.wallet.repository`
package com.diamyaraam.wallet.repository;

// Import de la classe `Transaction` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Transaction;
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
// Déclaration de l'interface `TransactionRepository` : repository Spring Data JPA de l'entité `Transaction` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    // Méthode abstraite (contrat) `findByPortefeuilleIdOrderByDateTransactionDesc` — paramètres : `portefeuilleId` (identifiant UUID) ; retourne : liste de Transaction ; intention : recherche par (find by portefeuille id order by date transaction desc)
    List<Transaction> findByPortefeuilleIdOrderByDateTransactionDesc(UUID portefeuilleId);
    // Méthode abstraite (contrat) `findByReferenceExterne` — paramètres : `referenceExterne` (chaîne de caractères) ; retourne : valeur optionnelle de Transaction ; intention : recherche par (find by reference externe)
    Optional<Transaction> findByReferenceExterne(String referenceExterne);
    // Méthode abstraite (contrat) `findByBeneficiaireUserId` — paramètres : `beneficiaireUserId` (identifiant UUID) ; retourne : liste de Transaction ; intention : recherche par (find by beneficiaire user id)
    List<Transaction> findByBeneficiaireUserId(UUID beneficiaireUserId);
}
