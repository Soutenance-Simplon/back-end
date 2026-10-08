// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de la classe `NotBlank` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.NotBlank;

// Déclaration de la classe `RefreshTokenRequest` (rôle : représente le corps d'une requête entrante)
public class RefreshTokenRequest {

    // Validation : valeur non vide — message d'erreur : « Le jeton de rafraîchissement est obligatoire. »
    @NotBlank(message = "Le jeton de rafraîchissement est obligatoire.")
    // Attribut `refreshToken` de type chaîne de caractères [privée]
    private String refreshToken;

    // Constructeur de `RefreshTokenRequest` sans paramètre
    public RefreshTokenRequest() {}

    // Constructeur de `RefreshTokenRequest` — paramètres : `refreshToken` (chaîne de caractères) (injection des dépendances par Spring)
    public RefreshTokenRequest(String refreshToken) {
        // Initialise l'attribut `refreshToken` avec la valeur de refreshToken
        this.refreshToken = refreshToken;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `refreshToken`
    public String getRefreshToken() { return refreshToken; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `refreshToken`
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
