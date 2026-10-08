// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de la classe `NotBlank` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.NotBlank;
// Import de la classe `Pattern` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.Pattern;

// Déclaration de la classe `ForgotPasswordRequest` (rôle : représente le corps d'une requête entrante)
public class ForgotPasswordRequest {

    // Validation : valeur non vide — message d'erreur : « Le numéro de téléphone est obligatoire. »
    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    // Validation : doit respecter l'expression régulière — message d'erreur : « Format de téléphone Sénégalais invalide. »
    @Pattern(regexp = "^(\\+221|00221)?[76][0-9]{8}$", message = "Format de téléphone Sénégalais invalide.")
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Constructeur de `ForgotPasswordRequest` sans paramètre
    public ForgotPasswordRequest() {}

    // Constructeur de `ForgotPasswordRequest` — paramètres : `telephone` (chaîne de caractères) (injection des dépendances par Spring)
    public ForgotPasswordRequest(String telephone) {
        // Initialise l'attribut `telephone` avec la valeur de telephone
        this.telephone = telephone;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
