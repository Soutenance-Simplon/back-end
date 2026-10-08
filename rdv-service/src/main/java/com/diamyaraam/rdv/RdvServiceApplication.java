// Déclaration du package Java : `com.diamyaraam.rdv`
package com.diamyaraam.rdv;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableDiscoveryClient` (paquet org.springframework.cloud.client.discovery)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// Import de la classe `EnableFeignClients` (paquet org.springframework.cloud.openfeign)
import org.springframework.cloud.openfeign.EnableFeignClients;
// Import de la classe `EnableScheduling` (paquet org.springframework.scheduling.annotation)
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ====================================================================================================
 * MICROSERVICE RENDEZ-VOUS & TÉLÉMÉDECINE WEBRTC : RDV-SERVICE (PORT 8084)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & INNOVATION TÉLÉMÉDICALE POUR LA SOUTENANCE :
 * Ce microservice est l'un des piliers technologiques majeurs de la plateforme Diam Yaraam.
 * 
 * 🩺 RESPONSABILITÉS MÉTIERS & TECHNIQUES :
 * 1. Cycle de Vie des Rendez-Vous (Prise de RDV, Confirmation, Annulation, Clôture) :
 *    - Réservation atomique d'un créneau pour éviter les collisions (double-booking).
 *    - Gestion des motifs de consultation et notifications automatiques.
 * 
 * 2. Plateforme de Téléconsultation Vidéo Haute Définition (LiveKit / WebRTC) :
 *    - LiveKit est une infrastructure WebRTC moderne open-source haute performance.
 *    - Ce microservice génère dynamiquement des jetons d'accès JWT chiffrés (LiveKit Access Tokens)
 *      avec droits d'entrée dans la salle de visioconférence privée (`SalleTeleconsultation`).
 *    - Chiffrement de bout en bout des flux audio/vidéo conforme aux exigences de confidentialité médicale.
 * 
 * 3. Tâches Planifiées en Arrière-Plan (@EnableScheduling) :
 *    - Exécution périodique de rappels automatiques (ex: 1 heure et 15 minutes avant le rendez-vous)
 *      pour lutter contre l'absentéisme médical (taux de no-show).
 * 
 * 4. Diffusion Temps Réel STOMP WebSockets :
 *    - Mise à jour instantanée du statut du RDV sur l'écran du médecin et du patient
 *      sans rechargement manuel de la page.
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Déclaration et découverte dynamique auprès d'Eureka (RDV-SERVICE)
// Active les clients HTTP déclaratifs OpenFeign pour appeler les autres microservices
@EnableFeignClients    // Communication déclarative OpenFeign avec auth-service, patient et medecin
// Active l'exécution des tâches planifiées (@Scheduled)
@EnableScheduling      // Active le moteur de planification Spring (@Scheduled) pour les rappels de consultation
// Déclaration de la classe `RdvServiceApplication`
public class RdvServiceApplication {

    /**
     * Démarrage du microservice de gestion des rendez-vous et téléconsultations.
     * 
     * @param args Paramètres d'exécution
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(RdvServiceApplication.class, args);
        SpringApplication.run(RdvServiceApplication.class, args);
    }
}

