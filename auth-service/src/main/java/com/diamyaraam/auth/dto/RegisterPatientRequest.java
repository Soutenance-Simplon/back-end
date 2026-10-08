// Déclaration du package Java : `com.diamyaraam.auth.dto`
package com.diamyaraam.auth.dto;

// Import de toutes les classes du paquet `jakarta.validation.constraints`
import jakarta.validation.constraints.*;

// Déclaration de la classe `RegisterPatientRequest` (rôle : représente le corps d'une requête entrante)
public class RegisterPatientRequest {

    // Validation : valeur non vide
    @NotBlank @Size(max = 150)
    // Attribut `firstName` (prénom) de type chaîne de caractères [privée]
    private String firstName;

    // Validation : valeur non vide
    @NotBlank @Size(max = 150)
    // Attribut `lastName` (nom de famille) de type chaîne de caractères [privée]
    private String lastName;

    // Validation : valeur non vide
    @NotBlank
    // Validation : doit respecter l'expression régulière — message d'erreur : « Format : +221XXXXXXXXX »
    @Pattern(regexp = "^\\+221\\d{9}$", message = "Format : +221XXXXXXXXX")
    // Attribut `telephone` (numéro de téléphone) de type chaîne de caractères [privée]
    private String telephone;

    // Validation : format d'e-mail correct
    @Email
    // Attribut `email` (adresse e-mail) de type chaîne de caractères [privée]
    private String email;

    // Validation : valeur non vide
    @NotBlank
    // Validation : taille limitée — message d'erreur : « Le mot de passe doit contenir au moins 6 caractères »
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    // Attribut `password` (mot de passe haché) de type chaîne de caractères [privée]
    private String password;

    // Attribut `confirmPassword` de type chaîne de caractères [privée]
    private String confirmPassword;

    // Constructeur de `RegisterPatientRequest` sans paramètre
    public RegisterPatientRequest() {}

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

    // Accesseur (getter) : renvoie la valeur de l'attribut `confirmPassword`
    public String getConfirmPassword() { return confirmPassword; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `confirmPassword`
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
