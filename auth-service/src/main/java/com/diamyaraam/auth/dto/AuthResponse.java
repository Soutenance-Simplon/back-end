// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Déclaration de la classe `AuthResponse` (rôle : représente une réponse sortante)
public class AuthResponse {

    // Attribut `accessToken` de type chaîne de caractères [privée]
    private String accessToken;
    // Attribut `refreshToken` de type chaîne de caractères [privée]
    private String refreshToken;
    // Attribut `tokenType` de type chaîne de caractères [privée] ; valeur initiale : le texte "Bearer"
    private String tokenType = "Bearer";
    // Attribut `expiresIn` de type entier long [privée]
    private long expiresIn;

    // Attribut `userId` (identifiant de l'utilisateur) de type identifiant UUID [privée]
    private UUID userId;
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;
    // Attribut `firstName` (prénom) de type chaîne de caractères [privée]
    private String firstName;
    // Attribut `lastName` (nom de famille) de type chaîne de caractères [privée]
    private String lastName;
    // Attribut `role` (rôle de l'utilisateur) de type chaîne de caractères [privée]
    private String role;
    // Attribut `accountStatus` (statut du compte) de type chaîne de caractères [privée]
    private String accountStatus;
    // Attribut `photoProfil` (photo de profil) de type chaîne de caractères [privée]
    private String photoProfil;

    // Constructeur de `AuthResponse` sans paramètre
    public AuthResponse() {}

    // Accesseur (getter) : renvoie la valeur de l'attribut `accessToken`
    public String getAccessToken() { return accessToken; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `accessToken`
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `refreshToken`
    public String getRefreshToken() { return refreshToken; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `refreshToken`
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tokenType`
    public String getTokenType() { return tokenType; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tokenType`
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `expiresIn`
    public long getExpiresIn() { return expiresIn; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `expiresIn`
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `firstName`
    public String getFirstName() { return firstName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `firstName`
    public void setFirstName(String firstName) { this.firstName = firstName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `lastName`
    public String getLastName() { return lastName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `lastName`
    public void setLastName(String lastName) { this.lastName = lastName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `role`
    public String getRole() { return role; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `role`
    public void setRole(String role) { this.role = role; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `accountStatus`
    public String getAccountStatus() { return accountStatus; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `accountStatus`
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoProfil`
    public String getPhotoProfil() { return photoProfil; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoProfil`
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }
}
