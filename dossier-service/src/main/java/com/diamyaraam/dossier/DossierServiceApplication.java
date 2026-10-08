// Déclaration du package Java : `com.diamyaraam.dossier`
package com.diamyaraam.dossier;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableDiscoveryClient` (paquet org.springframework.cloud.client.discovery)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// Import de la classe `EnableFeignClients` (paquet org.springframework.cloud.openfeign)
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ====================================================================================================
 * MICROSERVICE DOSSIER MÉDICAL PARTAGÉ (DMP) : DOSSIER-SERVICE (PORT 8087)
 * ====================================================================================================
 * 
 *  CONCEPTS CLÉS D'INGÉNIERIE CLINIQUE & RÉGLEMENTAIRE POUR LA SOUTENANCE :
 * Ce microservice héberge le patrimoine informationnel le plus sensible de Diam Yaraam :
 * les Données Personnelles de Santé (DPS) protégées par le secret médical.
 * 
 *  MODULES CLINIQUES INTÉGRÉS :
 * 1. Dossier Médical Informatisé Partagé (`DossierMedical`) :
 *    - Fiche synthétique d'orientation clinique, groupe sanguin, statut vaccinal.
 *    - Journal chronologique des consultations médicales passées et téléconsultations.
 * 
 * 2. Ordonnances & Prescriptions Numériques Sécurisées (`Prescription`) :
 *    - Édition d'ordonnances dématérialisées avec posologie, durée de traitement et mentions obligatoires.
 *    - Signature numérique du médecin et code unique anti-falsification en pharmacie.
 * 
 * 3. Gestion des Cartes Physiques & Badges Patients (`CartePhysique`) :
 *    - Cartes plastiques physiques dotées d'un QR code sécurisé imprimé.
 *    - Permet l'identification immédiate du patient au guichet hospitalier ou par le médecin
 *      même si le patient n'a pas de smartphone ou n'a plus de batterie.
 * 
 * 4. Traçabilité des Antécédents, Allergies & Hospitalisations :
 *    - Alerte automatique en cas d'incompatibilité ou d'antécédents cardiovasculaires majeurs.
 * ====================================================================================================
 */


@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Déclaration au registre Eureka (DOSSIER-SERVICE)
// Active les clients HTTP déclaratifs OpenFeign pour appeler les autres microservices
@EnableFeignClients    // Permet d'interroger les microservices auth, patient et medecin
// Déclaration de la classe `DossierServiceApplication`
public class DossierServiceApplication {

    /**
     * Démarrage du microservice de gestion des dossiers médicaux.
     * 
     * @param args Paramètres d'exécution
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(DossierServiceApplication.class, args);
        SpringApplication.run(DossierServiceApplication.class, args);
    }
}

