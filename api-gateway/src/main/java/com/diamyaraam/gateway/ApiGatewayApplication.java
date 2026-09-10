package com.diamyaraam.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API GATEWAY — Port 8080
 *
 * Point d'entrée unique pour tous les clients (mobile, web).
 * Route les requêtes vers les microservices appropriés.
 * Valide le JWT avant de laisser passer les requêtes protégées.
 *
 * Ordre de démarrage recommandé :
 *   1. discovery-server (8761)
 *   2. api-gateway (8080)
 *   3. auth-service (8081)   
 *   4. Les autres services
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
