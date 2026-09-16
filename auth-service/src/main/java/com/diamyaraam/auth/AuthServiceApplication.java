package com.diamyaraam.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * AUTH SERVICE — Port 8081
 *
 * @EnableDiscoveryClient : s'enregistre automatiquement sur Eureka.
 * Les autres services peuvent alors le trouver par son nom : "AUTH-SERVICE"
 *
 * Responsabilités :
 *   - Inscription patient / médecin
 *   - Login (téléphone + mot de passe)
 *   - Génération JWT
 *   - Gestion OTP (RM026-RM028)
 *   - Journalisation (RM029)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
