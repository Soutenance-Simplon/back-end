// Déclaration du package Java : `com.diamyaraam.gateway`
package com.diamyaraam.gateway;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableDiscoveryClient` (paquet org.springframework.cloud.client.discovery)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ====================================================================================================
 * MODULE D'INFRASTRUCTURE : PASSERELLE D'API (SPRING CLOUD GATEWAY - PORT 8080)
 * ====================================================================================================
 * 
 *  CONCEPTS CLÉS POUR LA SOUTENANCE :
 * L'API Gateway implémente le patron architectural fondamental "API Gateway Pattern" (ou Front-Door Pattern).
 * 
 *  POURQUOI UNE PASSERELLE UNIQUE PLUTÔT QU'UN ACCÈS DIRECT AUX MICROSERVICES ?
 * 1. Découplage et Sécurité (Point d'Entrée Unique) :
 *    Les clients (Application Flutter, Dashboard Admin, Navigateurs web) n'interagissent qu'avec un
 *    seul hôte et port (ex: http://localhost:8080). Les microservices internes (ports 8081, 8082, 8083...)
 *    restent masqués et protégés dans le réseau interne.
 * 
 * 2. Point Central de Contrôle de Sécurité (PEP - Policy Enforcement Point) :
 *    Au lieu de dupliquer la validation cryptographique des jetons JWT et la politique CORS
 *    dans chaque microservice, la Gateway vérifie le jeton JWT, contrôle les permissions RBAC
 *    (ex: exiger le rôle ADMIN pour /api/auth/admin/**) et rejette immédiatement les requêtes illégitimes.
 * 
 * 3. Routage Réactif Non-Bloquant (Spring WebFlux / Project Reactor) :
 *    Construite sur Netty et Project Reactor, la passerelle gère un très grand nombre de connexions
 *    concurrentes avec une empreinte mémoire minime par rapport au modèle basé sur les threads bloquants (Tomcat).
 * 
 * 4. Découverte de Services & Équilibrage de Charge (Client-side Load Balancing) :
 *    Grâce à l'annotation `@EnableDiscoveryClient`, la Gateway se connecte à Eureka pour résoudre
 *    dynamiquement les URI logiques de type `lb://PATIENT-SERVICE` ou `lb://AUTH-SERVICE`.
 * 
 *  ORDRE DE DÉMARRAGE RECOMMANDÉ :
 *   1. discovery-server (port 8761)
 *   2. api-gateway (port 8080)
 *   3. auth-service (port 8081)
 *   4. Autres microservices métiers (patient, medecin, rdv, dossier, wallet, notification, ia)
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Enregistre la passerelle auprès d'Eureka et active la résolution de nom dynamique
// Déclaration de la classe `ApiGatewayApplication`
public class ApiGatewayApplication {

    /**
     * Point d'entrée principal de l'API Gateway.
     * Démarre le serveur réactif Netty sur le port configuré (8080).
     * 
     * @param args Paramètres d'exécution CLI
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(ApiGatewayApplication.class, args);
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

