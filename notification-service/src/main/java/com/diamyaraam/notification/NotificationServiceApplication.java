// Déclaration du package Java : `com.diamyaraam.notification`
package com.diamyaraam.notification;

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
 * MICROSERVICE DE NOTIFICATIONS MULTI-CANAL : NOTIFICATION-SERVICE (PORT 8085)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & EXPÉRIENCE PATIENT POUR LA SOUTENANCE :
 * Ce microservice assure l'engagement et l'information continue des utilisateurs de Diam Yaraam.
 * 
 * 📢 CANAUX DE DIFFUSION SUPPORTÉS (MULTI-CHANNEL DISPATCHER) :
 * 1. Notifications Push Mobiles (Firebase Cloud Messaging - FCM) :
 *    - Alertes instantanées sur smartphone Android et iOS (ex: "Le Dr. Diallo a validé votre RDV").
 * 
 * 2. Événements Temps Réel In-App (WebSockets / STOMP) :
 *    - Mise à jour en direct des badges de notifications sur l'interface sans rafraîchissement.
 * 
 * 3. Alertes Critiques SMS & WhatsApp :
 *    - Envoi d'OTP, confirmations de paiement Mobile Money et rappels d'ordonnances expirées.
 * 
 * 4. Piste d'Historique & Acquittement :
 *    - Suivi des statuts de lecture (`isRead`), archivage et consultation par l'utilisateur.
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Déclaration dynamique auprès du serveur Eureka (NOTIFICATION-SERVICE)
// Active les clients HTTP déclaratifs OpenFeign pour appeler les autres microservices
@EnableFeignClients    // Permet d'appeler auth-service pour résoudre les destinataires
// Déclaration de la classe `NotificationServiceApplication`
public class NotificationServiceApplication {

    /**
     * Démarrage du microservice de notifications.
     * 
     * @param args Paramètres CLI
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(NotificationServiceApplication.class, args);
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

