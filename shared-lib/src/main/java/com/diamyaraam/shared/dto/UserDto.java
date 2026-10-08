// Déclaration du package Java : `com.diamyaraam.shared.dto`
package com.diamyaraam.shared.dto;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * DTO PARTAGÉ : UTILISATEUR DU SYSTÈME (USER DTO)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE & SÉCURITÉ POUR LA SOUTENANCE :
 * Pourquoi UserDto est-il un composant fondamental du système ?
 * 
 * 1. Non-Divulgation des Secrets d'Authentification (Security by Design) :
 *    Dans l'entité JPA `User` (auth-service), le mot de passe est haché avec BCrypt (`passwordHash`).
 *    En transférant systématiquement les données utilisateurs via `UserDto`, nous garantissons
 *    qu'AUCUN hash de mot de passe ne fuite sur le réseau ou vers d'autres microservices.
 * 
 * 2. Communication Inter-Microservices :
 *    Lorsqu'un service comme `patient-service` ou `wallet-service` a besoin d'afficher l'identité
 *    ou le contact d'un utilisateur, il fait appel à ce DTO normalisé.
 * 
 * 3. Identifiants Non-Prédictibles (UUID v4) :
 *    Au lieu d'IDs auto-incrémentés (1, 2, 3...) vulnérables à l'attaque IDOR (Insecure Direct Object
 *    References), tous les utilisateurs sont identifiés par un UUID de 128 bits garantissant
 *    l'unicité globale et l'impossibilité de deviner l'ID d'un autre utilisateur.
 * ====================================================================================================
 */
public class UserDto {

    /** Identifiant unique universel (UUID) de l'utilisateur */
    private UUID id;

    /** Prénom de l'utilisateur */
    private String firstName;

    /** Nom de famille de l'utilisateur */
    private String lastName;

    /** Numéro de téléphone au format international (identifiant unique de connexion) */
    private String telephone;

    /** Adresse email (optionnelle pour les patients, requise pour les administrateurs) */
    private String email;

    /** URL ou chemin d'accès vers la photo de profil */
    private String photoProfil;

    /** Nom du rôle système attribué (ex: "PATIENT", "MEDECIN", "ADMIN", "SUPERADMIN") */
    private String roleName;

    /** État du compte dans le cycle de vie (ex: "EN_ATTENTE_OTP", "ACTIF", "SUSPENDU", "BLOQUE") */
    private String accountStatus;

    /** Horodatage exact de création du compte en base de données */
    private LocalDateTime createdAt;

    /** Constructeur par défaut requis pour Jackson et la sérialisation JSON */
    public UserDto() {}

    /**
     * Constructeur complet paramétré pour instanciation directe.
     */
    public UserDto(UUID id, String firstName, String lastName, String telephone, String email, String photoProfil, String roleName, String accountStatus, LocalDateTime createdAt) {
        // Initialise l'attribut `id` avec la valeur de id
        this.id = id;
        // Initialise l'attribut `firstName` avec la valeur de firstName
        this.firstName = firstName;
        // Initialise l'attribut `lastName` avec la valeur de lastName
        this.lastName = lastName;
        // Initialise l'attribut `telephone` avec la valeur de telephone
        this.telephone = telephone;
        // Initialise l'attribut `email` avec la valeur de email
        this.email = email;
        // Initialise l'attribut `photoProfil` avec la valeur de photoProfil
        this.photoProfil = photoProfil;
        // Initialise l'attribut `roleName` avec la valeur de roleName
        this.roleName = roleName;
        // Initialise l'attribut `accountStatus` avec la valeur de accountStatus
        this.accountStatus = accountStatus;
        // Initialise l'attribut `createdAt` avec la valeur de createdAt
        this.createdAt = createdAt;
    }

    // ================================================================================================
    // ACCESSEURS (GETTERS) ET MUTATEURS (SETTERS)
    // ================================================================================================

    // Accesseur (getter) : renvoie la valeur de l'attribut `id`
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `firstName`
    public String getFirstName() { return firstName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `firstName`
    public void setFirstName(String firstName) { this.firstName = firstName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lastName`
    public String getLastName() { return lastName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lastName`
    public void setLastName(String lastName) { this.lastName = lastName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `email`
    public String getEmail() { return email; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `email`
    public void setEmail(String email) { this.email = email; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoProfil`
    public String getPhotoProfil() { return photoProfil; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoProfil`
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `roleName`
    public String getRoleName() { return roleName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `roleName`
    public void setRoleName(String roleName) { this.roleName = roleName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `accountStatus`
    public String getAccountStatus() { return accountStatus; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `accountStatus`
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

