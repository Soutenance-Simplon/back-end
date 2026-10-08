// Déclaration du package Java : `com.diamyaraam.medecin.repository`
package com.diamyaraam.medecin.repository;

// Import de la classe `Medecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.Medecin;
// Import de la classe `StatutMedecin` (paquet com.diamyaraam.medecin.entity.Medecin)
import com.diamyaraam.medecin.entity.Medecin.StatutMedecin;
// Import de la classe `JpaRepository` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.JpaRepository;
// Import de la classe `Query` (paquet org.springframework.data.jpa.repository)
import org.springframework.data.jpa.repository.Query;
// Import de la classe `Param` (paquet org.springframework.data.repository.query)
import org.springframework.data.repository.query.Param;
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
// Déclaration de l'interface `MedecinRepository` : repository Spring Data JPA de l'entité `Medecin` (clé primaire `UUID`) — les opérations CRUD sont générées automatiquement
public interface MedecinRepository extends JpaRepository<Medecin, UUID> {

    // Méthode abstraite (contrat) `findByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : valeur optionnelle de Medecin ; intention : recherche par (find by user id)
    Optional<Medecin> findByUserId(UUID userId);
    // Méthode abstraite (contrat) `existsByUserId` — paramètres : `userId` (identifiant UUID) ; retourne : booléen ; intention : teste l'existence de (exists by user id)
    boolean existsByUserId(UUID userId);

    // Méthode abstraite (contrat) `findByStatutMedecinAndIsVerifiedTrue` — paramètres : `status` (StatutMedecin) ; retourne : liste de Medecin ; intention : recherche par (find by statut medecin and is verified true)
    List<Medecin> findByStatutMedecinAndIsVerifiedTrue(StatutMedecin status);

    // Requête JPQL/SQL personnalisée : SELECT m FROM Medecin m WHERE m.isVerified = true AND m.statutMedecin…
    @Query("SELECT m FROM Medecin m WHERE m.isVerified = true AND m.statutMedecin = 'ACTIF' " +
           // Valeur de paramètre d'annotation : "AND (:specialite IS NULL OR LOWER(m.onmsReference.specialite) LIKE L…
           "AND (:specialite IS NULL OR LOWER(m.onmsReference.specialite) LIKE LOWER(CONCAT('%', :specialite, '%'))) " +
           // Valeur de paramètre d'annotation : "AND (:region IS NULL OR LOWER(m.onmsReference.region) LIKE LOWER(CON…
           "AND (:region IS NULL OR LOWER(m.onmsReference.region) LIKE LOWER(CONCAT('%', :region, '%')))")
    // Méthode abstraite (contrat) `searchMedecins` — paramètres : `specialite` (chaîne de caractères), `region` (chaîne de caractères) ; retourne : liste de Medecin
    List<Medecin> searchMedecins(@Param("specialite") String specialite, @Param("region") String region);
}
