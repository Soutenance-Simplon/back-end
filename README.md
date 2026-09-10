# Plateforme Médicale Diam-Yaraam - Backend Microservices

Architecture microservices distribuée pour la plateforme de santé et de télémédecine **Diam-Yaraam** au Sénégal. Le système interconnecte 10 microservices résilients orchestrés via Spring Cloud (Eureka + API Gateway) et une IA d'orientation clinique basée sur FastAPI et Python.

---

## 🏗️ Architecture des Microservices

| Service | Port | Technologie | Description |
| :--- | :--- | :--- | :--- |
| **Discovery Server** | `8761` | Spring Cloud Netflix Eureka | Enregistrement et découverte automatique des instances |
| **API Gateway** | `8090` | Spring Cloud Gateway | Point d'entrée unique, routage dynamique, filtre JWT & CORS |
| **Shared Library** | N/A | Maven Module | DTOs communs, énumérations métier, exceptions partagées |
| **Auth Service** | `8081` | Spring Boot 3, Spring Security 6, JWT | Gestion des comptes, rôles, sessions, OTP et WhatsApp API |
| **Medecin Service** | `8082` | Spring Boot 3, Spring Data JPA | Annuaire des praticiens, ordre ONMS, disponibilités & créneaux |
| **Patient Service** | `8083` | Spring Boot 3, Spring Data JPA | Fiche patient, Pass Vital d'urgence, QR code sécurisé, famille |
| **Dossier Service** | `8084` | Spring Boot 3, Spring Data JPA | Dossier médical partagé, allergies, antécédents, ordonnances |
| **RDV Service** | `8085` | Spring Boot 3, WebSocket STOMP, LiveKit | Planification des consultations, flux vidéo LiveKit WebRTC |
| **Notification Service** | `8086` | Spring Boot 3, JavaMail, Firebase FCM | Notifications push mobiles, courriels de confirmation et rappels |
| **Wallet Service** | `8087` | Spring Boot 3, Spring Data JPA | Portefeuille santé en FCFA, recharges Wave / OM, paiements |
| **IA Service** | `8089` | FastAPI, Python 3, OpenRouter, RAG | Triage clinique, vérification pharmacologique Dorosz |

---

## ⚙️ Prérequis & Environnement

- **Java JDK** : Version 21 (LTS)
- **Maven** : Version 3.9+ (ou utiliser le wrapper `mvnw.cmd`)
- **Python** : Version 3.10+ (pour `ia-service`)
- **PostgreSQL** : Version 15+ avec base `diam_yaraam_db`
- **Serveur LiveKit** : Instance WebRTC pour la téléconsultation (port 7880)

---

## 🚀 Démarrage Rapide

### 1. Compilation globale
```bash
./mvnw clean compile
```

### 2. Démarrage séquentiel recommandé
1. **Discovery Server** (`discovery-server`) : `mvn spring-boot:run` sur le port 8761
2. **API Gateway** (`api-gateway`) : `mvn spring-boot:run` sur le port 8090
3. **Services Métier** : Démarrer `auth-service`, `medecin-service`, `patient-service`, `dossier-service`, `rdv-service`, `notification-service`, `wallet-service`
4. **Service IA** (`ia-service`) : Exécuter `start_ia_service.bat`

---

## 🌿 Méthodologie Git Flow

Le dépôt applique rigoureusement la méthodologie **Git Flow** :
- `main` : Version stable de production, étiquetée avec des tags sémantiques (`v1.0.0`)
- `develop` : Branche principale d'intégration
- `feature/*` : Branches de fonctionnalités dédiées par microservice ou composant
- `release/*` : Branches de stabilisation et de préparation de livraison
