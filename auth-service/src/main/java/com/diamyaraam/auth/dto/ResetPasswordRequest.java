// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de la classe `NotBlank` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.NotBlank;
// Import de la classe `Pattern` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.Pattern;
// Import de la classe `Size` (paquet jakarta.validation.constraints)
import jakarta.validation.constraints.Size;

// Déclaration de la classe `ResetPasswordRequest` (rôle : représente le corps d'une requête entrante)
public class ResetPasswordRequest {

    // Validation : valeur non vide — message d'erreur : « Le numéro de téléphone est obligatoire. »
    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    // Validation : doit respecter l'expression régulière — message d'erreur : « Format de téléphone Sénégalais invalide. »
    @Pattern(regexp = "^(\\+221|00221)?[76][0-9]{8}$", message = "Format de téléphone Sénégalais invalide.")
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Validation : valeur non vide — message d'erreur : « Le code OTP est obligatoire. »
    @NotBlank(message = "Le code OTP est obligatoire.")
    // Validation : taille limitée — message d'erreur : « Le code OTP doit comporter 6 chiffres. »
    @Size(min = 6, max = 6, message = "Le code OTP doit comporter 6 chiffres.")
    // Attribut `code` (code) de type chaîne de caractères [privée]
    private String code;

    // Validation : valeur non vide — message d'erreur : « Le nouveau mot de passe est obligatoire. »
    @NotBlank(message = "Le nouveau mot de passe est obligatoire.")
    // Validation : taille limitée — message d'erreur : « Le mot de passe doit comporter au moins 8 caractères. »
    @Size(min = 8, message = "Le mot de passe doit comporter au moins 8 caractères.")
    // Attribut `newPassword` de type chaîne de caractères [privée]
    private String newPassword;

    // Validation : valeur non vide — message d'erreur : « La confirmation du mot de passe est obligatoire. »
    @NotBlank(message = "La confirmation du mot de passe est obligatoire.")
    // Attribut `confirmNewPassword` de type chaîne de caractères [privée]
    private String confirmNewPassword;

    // Constructeur de `ResetPasswordRequest` sans paramètre
    public ResetPasswordRequest() {}

    // Accesseur (getter) : renvoie la valeur de l'attribut `telephone`
    public String getTelephone() { return telephone; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `telephone`
    public void setTelephone(String telephone) { this.telephone = telephone; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `code`
    public String getCode() { return code; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `code`
    public void setCode(String code) { this.code = code; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `newPassword`
    public String getNewPassword() { return newPassword; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `newPassword`
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `confirmNewPassword`
    public String getConfirmNewPassword() { return confirmNewPassword; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `confirmNewPassword`
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
