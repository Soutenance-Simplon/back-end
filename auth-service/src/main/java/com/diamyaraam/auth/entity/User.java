// Déclaration du package Java : `com.diamyaraam.auth.entity`
package com.diamyaraam.auth.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;
// Import de la classe `Pattern` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.Pattern;
// Import de la classe `CreationTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.CreationTimestamp;
// Import de la classe `UpdateTimestamp` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UpdateTimestamp;
// Import de la classe `UuidGenerator` (paquet org.hibernate.annotations)
import org.hibernate.annotations.UuidGenerator;

// Import de la classe `LocalDate` (paquet java.time)
import java.time.LocalDate;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * ENTITÉ JPA / MODÈLE DE DONNÉES : UTILISATEUR SYSTÈME (USER ENTITY)
 * ====================================================================================================
 * 
 *  CONCEPTS DE CONCEPTION DE BASE DE DONNÉES POUR LA SOUTENANCE :
 * Cette classe mappe la table relationnelle `auth_schema.users` sous PostgreSQL.
 * 
 *  CHOIX ARCHITECTURAUX REMARQUABLES :
 * 1. Isolation par Schéma SQL (`schema = "auth_schema"`) :
 *    Au lieu de mélanger toutes les tables dans le schéma public, chaque microservice
 *    possède son schéma dédié (`auth_schema`, `patient_schema`, `rdv_schema`, etc.).
 *    Cela garantit le découplage des données même en cas de base de données mutualisée.
 * 
 * 2. Clé Primaire UUID v4 (`@UuidGenerator`) :
 *    Génération côté application d'un UUID 128 bits non séquentiel.
 *    Évite les attaques par énumération d'identifiants (IDOR).
 * 
 * 3. Validation Regex du Format Téléphonique Sénégalais :
 *    `^\\+221\\d{9}$` : force le préfixe international +221 suivi de 9 chiffres (77, 78, 70, 76...).
 * 
 * 4. Cycle de Vie du Compte (AccountStatus) :
 *    - EN_ATTENTE : Inscription initiale, en attente de validation OTP.
 *    - ACTIF : Compte validé et pleinement opérationnel.
 *    - SUSPENDU : Désactivé temporairement par l'administrateur.
 *    - BLOQUE : Verrouillé automatiquement suite à 5 tentatives de mot de passe échouées.
 *    - SUPPRIME : Marqué pour suppression (Soft Delete).
 * ====================================================================================================
 */
// Indique à JPA qu'il s'agit d'une entité persistante mappée sur une table de base de données
@Entity
// Configuration de la table relationnelle cible dans PostgreSQL
@Table(
    // Nom de la table dans la base de données
    name = "users",
    // Schéma SQL d'isolation multi-tenant applicatif
    schema = "auth_schema",
    // Définition des contraintes d'unicité d'index
    uniqueConstraints = {
        // Contrainte d'unicité sur le numéro de téléphone mobile
        @UniqueConstraint(columnNames = "telephone", name = "uk_users_telephone"),
        // Contrainte d'unicité sur l'adresse email
        @UniqueConstraint(columnNames = "email", name = "uk_users_email")
    }
)
// Déclaration de la classe `User`
public class User {

    // Clé primaire de l'entité
    @Id
    // Génération automatique d'UUID v4 non prédictible (protection IDOR)
    @UuidGenerator
    // Mappage sur le type natif uuid de PostgreSQL en lecture seule
    @Column(columnDefinition = "uuid", updatable = false)
    // Attribut `id` (identifiant unique) de type identifiant UUID [privée]
    private UUID id;

    // Prénom de l'utilisateur
    @Column(name = "first_name", nullable = false, length = 150)
    // Attribut `firstName` (prénom) de type chaîne de caractères [privée]
    private String firstName;

    // Nom de famille de l'utilisateur
    @Column(name = "last_name", nullable = false, length = 150)
    // Attribut `lastName` (nom de famille) de type chaîne de caractères [privée]
    private String lastName;

    // Validation par expression régulière du format téléphonique international sénégalais
    @Pattern(
        // Doit débuter par +221 suivi de 9 chiffres
        regexp = "^\\+221\\d{9}$",
        // Message d'erreur renvoyé en cas de format invalide
        message = "Format : +221XXXXXXXXX"
    )
    // Colonne téléphone obligatoire, unique et indexée
    @Column(nullable = false, unique = true, length = 20)
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Adresse email facultative mais unique si renseignée
    @Column(unique = true)
    // Attribut `email` (adresse e-mail) de type chaîne de caractères [privée]
    private String email;

    // Empreinte hachée sécurisée du mot de passe (BCrypt avec salt)
    @Column(nullable = false)
    // Attribut `password` (mot de passe haché) de type chaîne de caractères [privée]
    private String password;

    // Date de naissance de l'utilisateur pour calcul d'âge clinique
    @Column(name = "date_naissance")
    // Attribut `dateNaissance` (date de naissance) de type date [privée]
    private LocalDate dateNaissance;

    // Énumération des genres civils autorisés
    public enum Genre { M, F }

    // Persistance de l'énumération sous forme de chaîne de caractères
    @Enumerated(EnumType.STRING)
    // Colonne d'un caractère (M ou F)
    @Column(length = 1)
    // Attribut `genre` (genre (M ou F)) de type Genre [privée]
    private Genre genre;

    // URL ou chemin vers l'image de profil hébergée
    @Column(name = "photo_profil", columnDefinition = "TEXT")
    // Attribut `photoProfil` (photo de profil) de type chaîne de caractères [privée]
    private String photoProfil;

    // Association Plusieurs-à-Un vers le rôle utilisateur (chargement immédiat)
    @ManyToOne(fetch = FetchType.EAGER)
    // Clé étrangère pointant vers la table role
    @JoinColumn(name = "role_id")
    // Attribut `role` (rôle de l'utilisateur) de type Role [privée]
    private Role role;

    // Énumération des états du cycle de vie du compte utilisateur
    public enum AccountStatus {
        // En attente de vérification OTP WhatsApp
        EN_ATTENTE,
        // Compte actif et validé
        ACTIF,
        // Compte suspendu manuellement par l'administrateur
        SUSPENDU,
        // Compte verrouillé temporairement après échecs de connexion répétés
        BLOQUE,
        // Compte marqué pour archivage / suppression douce
        SUPPRIME
    }

    // Persistance du libellé d'état sous forme de chaîne de caractères
    @Enumerated(EnumType.STRING)
    // Colonne d'état du compte obligatoire
    @Column(name = "account_status", length = 15, nullable = false)
    // Valeur par défaut à la création : EN_ATTENTE de validation OTP
    private AccountStatus accountStatus = AccountStatus.EN_ATTENTE;

    // Indicateur attestant que le numéro de téléphone a été vérifié par OTP
    @Column(name = "phone_verified", nullable = false)
    // Attribut `phoneVerified` (téléphone vérifié) de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean phoneVerified = false;

    // Compteur de tentatives consécutives de connexion infructueuses (anti brute-force)
    @Column(name = "failed_login_attempts", nullable = false)
    // Attribut `failedLoginAttempts` (compteur d'échecs de connexion) de type entier [privée] ; valeur initiale : la valeur numérique 0
    private Integer failedLoginAttempts = 0;

    // Date et heure jusqu'à laquelle le compte reste temporairement verrouillé
    @Column(name = "locked_until")
    // Attribut `lockedUntil` (date de fin de verrouillage) de type date-heure [privée]
    private LocalDateTime lockedUntil;

    // Droit d'accès au portail du personnel de santé / équipe interne
    @Column(name = "is_staff", nullable = false)
    // Attribut `isStaff` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean isStaff = false;

    // Droits d'administration suprême (Super Administrateur)
    @Column(name = "is_superuser", nullable = false)
    // Attribut `isSuperuser` de type booléen [privée] ; valeur initiale : le booléen faux
    private Boolean isSuperuser = false;

    // Horodatage automatique géré par Hibernate à la création de l'enregistrement
    @CreationTimestamp
    // Colonne non modifiable après insertion
    @Column(name = "created_at", updatable = false)
    // Attribut `createdAt` (date de création) de type date-heure [privée]
    private LocalDateTime createdAt;

    // Horodatage automatique géré par Hibernate à chaque mise à jour de l'enregistrement
    @UpdateTimestamp
    // Colonne SQL associée à l'attribut : nom « updated_at »
    @Column(name = "updated_at")
    // Attribut `updatedAt` (date de dernière modification) de type date-heure [privée]
    private LocalDateTime updatedAt;

    // Constructeur sans argument exigé par la spécification JPA
    public User() {}

    // Méthode métier vérifiant si le compte est actuellement sous verrouillage de sécurité
    public boolean isAccountLocked() {
        // Si le statut n'est pas BLOQUE, le compte n'est pas verrouillé
        if (!AccountStatus.BLOQUE.equals(this.accountStatus)) return false;
        // Si aucune date de fin de verrouillage n'est définie, le blocage est permanent
        if (this.lockedUntil == null) return true;
        // Le compte est verrouillé si l'instant présent est antérieur à l'échéance de déblocage
        return LocalDateTime.now().isBefore(this.lockedUntil);
    }

    // Méthode métier vérifiant si le compte est pleinement actif pour se connecter
    public boolean isActive() {
        // Renvoie vrai si et seulement si le statut correspond à ACTIF
        return AccountStatus.ACTIF.equals(this.accountStatus);
    }

    // --- Accesseurs (Getters) et Mutateurs (Setters) ---
    // Getter de l'identifiant primaire
    public UUID getId() { return id; }
    // Setter de l'identifiant primaire
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

    // Accesseur (getter) : renvoie la valeur de l'attribut `password`
    public String getPassword() { return password; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `password`
    public void setPassword(String password) { this.password = password; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateNaissance`
    public LocalDate getDateNaissance() { return dateNaissance; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateNaissance`
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `genre`
    public Genre getGenre() { return genre; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `genre`
    public void setGenre(Genre genre) { this.genre = genre; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoProfil`
    public String getPhotoProfil() { return photoProfil; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoProfil`
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `role`
    public Role getRole() { return role; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `role`
    public void setRole(Role role) { this.role = role; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `accountStatus`
    public AccountStatus getAccountStatus() { return accountStatus; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `accountStatus`
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `phoneVerified`
    public Boolean getPhoneVerified() { return phoneVerified; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `phoneVerified`
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `failedLoginAttempts`
    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `failedLoginAttempts`
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lockedUntil`
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lockedUntil`
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `isStaff`
    public Boolean getIsStaff() { return isStaff; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `isStaff`
    public void setIsStaff(Boolean isStaff) { this.isStaff = isStaff; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `isSuperuser`
    public Boolean getIsSuperuser() { return isSuperuser; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `isSuperuser`
    public void setIsSuperuser(Boolean isSuperuser) { this.isSuperuser = isSuperuser; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `createdAt`
    public LocalDateTime getCreatedAt() { return createdAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `createdAt`
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `updatedAt`
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `updatedAt`
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
