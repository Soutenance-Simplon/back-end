// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de la classe `NotBlank` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.NotBlank;
// Import de la classe `Pattern` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.Pattern;

// Déclaration de la classe `LoginRequest` (rôle : représente le corps d'une requête entrante)
public class LoginRequest {

    // Validation : valeur non vide — message d'erreur : « Le numéro de téléphone est obligatoire »
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    // Validation : doit respecter l'expression régulière — message d'erreur : « Format : +221XXXXXXXXX »
    @Pattern(regexp = "^\\+221\\d{9}$", message = "Format : +221XXXXXXXXX")
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Validation : valeur non vide — message d'erreur : « Le mot de passe est obligatoire »
    @NotBlank(message = "Le mot de passe est obligatoire")
    // Attribut `password` (mot de passe haché) de type chaîne de caractères [privée]
    private String password;

    // Constructeur de `LoginRequest` sans paramètre
    public LoginRequest() {}

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `password`
    public String getPassword() { return password; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `password`
    public void setPassword(String password) { this.password = password; }
}
