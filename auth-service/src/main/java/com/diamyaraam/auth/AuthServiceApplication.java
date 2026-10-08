// Déclaration du package Java : `com.diamyaraam.auth`
package com.diamyaraam.auth;

// Import de la classe `SpringApplication` (paquet org.springframework.boot)
import org.springframework.boot.SpringApplication;
// Import de la classe `SpringBootApplication` (paquet org.springframework.boot.autoconfigure)
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Import de la classe `EnableDiscoveryClient` (paquet org.springframework.cloud.client.discovery)
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ====================================================================================================
 * MICROSERVICE D'AUTHENTIFICATION & SÉCURITÉ : AUTH-SERVICE (PORT 8081)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & SÉCURITÉ POUR LA SOUTENANCE :
 * Le microservice `auth-service` est le garant absolu de l'identité numérique de la plateforme.
 * 
 * 🔐 RESPONSABILITÉS MÉTIERS & TECHNIQUES MAJEURES :
 * 1. Gestion des Identités & Rôles (IAM - Identity & Access Management) :
 *    - Inscription sécurisée des patients et praticiens de santé.
 *    - Gestion du modèle RBAC (Role-Based Access Control) : PATIENT, MEDECIN, ADMIN, SUPERADMIN.
 * 
 * 2. Authentification Forte par Téléphone & Mot de Passe :
 *    - Au Sénégal, le numéro de téléphone est le vecteur d'identité privilégié (taux de pénétration mobile élevé).
 *    - Hachage cryptographique irréversible avec sel aléatoire via l'algorithme BCrypt.
 * 
 * 3. Double Authentification (2FA) & Vérification OTP :
 *    - Génération de codes temporaires à usage unique (OTP) à 6 chiffres avec expiration stricte (5 minutes).
 *    - Envoi automatisé par WhatsApp Cloud API (Meta) ou SMS.
 * 
 * 4. Émission et Signature de Jetons Cryptographiques JWT (JSON Web Tokens) :
 *    - Access Token signé en HMAC-SHA512 avec durée de vie courte pour minimiser l'impact d'un vol de jeton.
 *    - Refresh Token avec rotation pour renouveler la session sans redemander le mot de passe.
 * 
 * 5. Piste d'Audit & Conformité Légale (RGPD / CDP Sénégal) :
 *    - Journalisation systématique des connexions, échecs d'authentification, adresses IP et User-Agents.
 * ====================================================================================================
 */
@SpringBootApplication
// Enregistre ce microservice auprès du serveur de découverte Eureka
@EnableDiscoveryClient // Déclaration et découverte dynamique auprès du registre Eureka
// Déclaration de la classe `AuthServiceApplication`
public class AuthServiceApplication {

    /**
     * Point d'entrée de démarrage de l'auth-service.
     * Lance le conteneur Spring Boot sur le port 8081.
     * 
     * @param args Paramètres d'exécution
     */
    public static void main(String[] args) {
        // Appelle la méthode `run` sur `SpringApplication` : SpringApplication.run(AuthServiceApplication.class, args);
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

