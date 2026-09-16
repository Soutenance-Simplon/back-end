package com.diamyaraam.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * DISCOVERY SERVER — Serveur Eureka
 *
 * @EnableEurekaServer : active le serveur de registre de services.
 * Tous les autres microservices s'enregistrent ici au démarrage.
 *
 * Démarrer EN PREMIER avant tout autre service.
 * URL dashboard : http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
