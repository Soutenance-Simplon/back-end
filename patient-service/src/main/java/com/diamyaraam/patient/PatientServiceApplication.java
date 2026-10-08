// Déclaration du package Java : `com.diamyaraam.patient`
package com.diamyaraam.patient;

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
 * MICROSERVICE PATIENT & SANTÉ FAMILIALE : PATIENT-SERVICE (PORT 8082)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & PATRONS DE CONCEPTION POUR LA SOUTENANCE :
 * Ce microservice gère le cycle de vie du dossier administratif et d'urgence du patient.
 * 
 * 🩺 RESPONSABILITÉS MÉTIERS FONDAMENTALES :
 * 1. Gestion des Profils Patients & Données Administratives :
 *    - Groupe sanguin, rhésus, allergies majeures, antécédents médicaux immédiats.
 *    - Contacts d'urgence (proches ou référents désignés).
 * 
 * 2. Gestion de la Santé Familiale (Membres de la Famille) :
 *    - Permet à un chef de famille d'enregistrer et de gérer les profils de ses ayants droit
 *      (enfants, conjoints, parents dépendants) sous un compte unifié.
 * 
 * 3. Module d'Urgence par Code QR Vital (QR Urgence SAMU / Pompiers) :
 *    - Génération de badges et cartes physiques avec QR code chiffré.
 *    - En cas d'accident sur la voie publique, les secouristes scannent le QR code pour accéder
 *      instantanément aux constantes vitales sans déverrouiller le smartphone du patient.
 * 
 * 🔌 COMMUNICATION INTER-SERVICES DECLARATIVE (@EnableFeignClients) :
 *    - Grâce à Spring Cloud OpenFeign, ce microservice peut consommer les APIs d'autres services
 *      (ex: auth-service pour les coordonnées, dossier-service pour l'historique médical)
 *      via de simples interfaces Java déclaratives avec Load Balancing intégré.
 * ====================================================================================================
 */
// Annotation fondamentale composant @Configuration, @EnableAutoConfiguration et @ComponentScan
@SpringBootApplication
// Déclaration du client d'enregistrement dynamique auprès du serveur Eureka Discovery
@EnableDiscoveryClient
// Activation du mécanisme d'appels RPC déclaratifs inter-microservices via OpenFeign
@EnableFeignClients
// Déclaration de la classe `PatientServiceApplication`
public class PatientServiceApplication {

    /**
     * Point d'entrée principal de la machine virtuelle Java (JVM) pour lancer le microservice.
     * 
     * @param args Arguments passés en ligne de commande
     */
    public static void main(String[] args) {
        // Initialisation du contexte applicatif Spring Boot et démarrage du serveur Tomcat embarqué sur le port 8082
        SpringApplication.run(PatientServiceApplication.class, args);
    }
}

