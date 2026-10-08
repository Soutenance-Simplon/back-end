// Déclaration du package Java : `com.diamyaraam.discovery`
package com.diamyaraam.discovery;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableEurekaServer` (paquet org.springframework.cloud.netflix.eureka.server)
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * ====================================================================================================
 * MODULE D'INFRASTRUCTURE : SERVEUR DE DÉCOUVERTE (SERVICE DISCOVERY - NETFLIX EUREKA)
 * ====================================================================================================
 * 
 *  CONTEXTE POUR LA SOUTENANCE :
 * Dans une architecture distribuée en microservices, les instances de services peuvent changer
 * dynamiquement d'adresse IP ou de port (scaling horizontal, conteneurisation Docker, pannes temporaires).
 * 
 * Plutôt que de coder en dur les URLs des services (ce qui créerait un couplage fort et fragile),
 * nous utilisons le patron d'architecture "Service Registry & Discovery" via Netflix Eureka Server.
 * 
 *  RÔLE & FONCTIONNEMENT SYSTÈME :
 * 1. Enregistrement automatique (Self-Registration) :
 *    Au démarrage, chaque microservice métier (auth-service, patient-service, rdv-service, etc.)
 *    envoie une requête HTTP POST à ce serveur Eureka pour déclarer son nom logique (spring.application.name)
 *    et son adresse réseau (IP + port).
 * 
 * 2. Maintien de l'état de santé (Heartbeat) :
 *    Chaque microservice envoie périodiquement (toutes les 30 secondes par défaut) un battement de cœur
 *    (heartbeat) pour confirmer qu'il est opérationnel. Si un service ne répond plus, Eureka l'évince
 *    du registre pour éviter d'y router des requêtes.
 * 
 * 3. Routage dynamique par l'API Gateway :
 *    La passerelle (API Gateway) interroge ce registre pour traduire les requêtes entrantes
 *    (ex: lb://AUTH-SERVICE) vers une instance saine disponible (Load Balancing côté client).
 * 
 *  ORDRE D'EXÉCUTION DU SYSTÈME :
 * Ce microservice DOIT impérativement être démarré EN PREMIER, avant l'API Gateway et les microservices métiers.
 * Interface web d'administration : http://localhost:8761
 * ====================================================================================================
 */
@SpringBootApplication
// Transforme l'application en serveur d'enregistrement Eureka
@EnableEurekaServer // Active le rôle de serveur de registre Eureka au sein du contexte Spring Boot
// Déclaration de la classe `DiscoveryServerApplication`
public class DiscoveryServerApplication {

    /**
     * Point d'entrée principal (Main Entry Point) de la JVM.
     * Démarre le serveur embarqué (Tomcat) et initialise le registre Eureka.
     * 
     * @param args Arguments de la ligne de commande (ex: profils Spring, surcharges de ports)
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(DiscoveryServerApplication.class, args);
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}

