// Déclaration du package Java : `com.diamyaraam.medecin`
package com.diamyaraam.medecin;

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
 * MICROSERVICE MÉDECINS & GESTION DES AGENDAS : MEDECIN-SERVICE (PORT 8083)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & INNOVATION POUR LA SOUTENANCE :
 * Ce microservice gère l'annuaire médical officiel, les certifications professionnelles et les plannings.
 * 
 * 🩺 RESPONSABILITÉS MÉTIERS MAJEURES :
 * 1. Certification & Vérification ONDMS (Ordre National des Médecins du Sénégal) :
 *    - Authentification rigoureuse des diplômes et numéros d'enregistrement à l'Ordre (`OnmsReference`).
 *    - Empêche l'exercice illégal de la médecine et protège les patients contre les faux profils.
 * 
 * 2. Annuaire Géolocalisé des Professionnels de Santé :
 *    - Recherche multi-critères : spécialité médicale (cardiologie, pédiatrie, gynécologie...),
 *      région (Dakar, Thiès, Ziguinchor...), établissement d'attache et tarif de consultation.
 * 
 * 3. Moteur de Gestion des Disponibilités & Créneaux (Planning Engine) :
 *    - Définition des plages horaires récurrentes (ex: Lundi 09h00-13h00).
 *    - Découpage automatique en créneaux unitaires de consultation (slots de 30 min).
 *    - Activation / désactivation de la téléconsultation vidéo à distance.
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Déclaration dynamique dans le registre Eureka (MEDECIN-SERVICE)
// Active les clients HTTP déclaratifs OpenFeign pour appeler les autres microservices
@EnableFeignClients    // Active OpenFeign pour les requêtes inter-services (vers auth et rdv)
// Déclaration de la classe `MedecinServiceApplication`
public class MedecinServiceApplication {

    /**
     * Démarrage du microservice medecin-service sur le port configuré.
     * 
     * @param args Paramètres CLI
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(MedecinServiceApplication.class, args);
        SpringApplication.run(MedecinServiceApplication.class, args);
    }
}

