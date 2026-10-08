// Déclaration du package Java : `com.diamyaraam.wallet`
package com.diamyaraam.wallet;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableDiscoveryClient` (paquet org.springframework.cloud.client.discovery)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ====================================================================================================
 * MICROSERVICE PORTEFEUILLE VIRTUEL & FINTECH SANTÉ : WALLET-SERVICE (PORT 8086)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE FINANCIÈRE & IMPACT SOCIAL POUR LA SOUTENANCE :
 * Au Sénégal et en Afrique de l'Ouest, le taux de bancarisation classique reste faible (< 25%),
 * tandis que le Mobile Money (Wave, Orange Money, Free Money) est massivement adopté.
 * 
 * 💳 RESPONSABILITÉS MÉTIERS DU WALLET DE SANTÉ :
 * 1. Inclusion Financière & Paiement Mobile Money Dématérialisé :
 *    - Portefeuille électronique dédié aux soins de santé (`Portefeuille`).
 *    - Rechargement instantané via Wave et Orange Money sans carte bancaire requise.
 * 
 * 2. Paiement Atomique Sécurisé des Consultations :
 *    - Débit du patient et crédit du praticien en une transaction isolée ACID.
 *    - Évite les contestations et sécurise les honoraires médicaux.
 * 
 * 3. Épargne Santé Familiale & Bénéficiaires :
 *    - Possibilité pour la diaspora ou un chef de famille d'approvisionner le compte santé
 *      d'un proche parent au Sénégal (`Beneficiaire`).
 * 
 * 4. Piste d'Audit Financier Inviolable (`AuditFinancier`) :
 *    - Enregistrement immuable de chaque centime échangé avec solde avant/après et horodatage strict.
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Enregistre le service financier auprès du registre Eureka (WALLET-SERVICE)
// Déclaration de la classe `WalletServiceApplication`
public class WalletServiceApplication {

    /**
     * Démarrage du microservice de gestion des paiements et portefeuilles santé.
     * 
     * @param args Paramètres d'exécution
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(WalletServiceApplication.class, args);
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}

