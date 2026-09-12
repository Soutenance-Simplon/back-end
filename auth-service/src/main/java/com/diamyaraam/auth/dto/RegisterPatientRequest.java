package com.diamyaraam.auth.dto;

import jakarta.validation.constraints.*;

public class RegisterPatientRequest {

    @NotBlank @Size(max = 150)
    private String firstName;

    @NotBlank @Size(max = 150)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^\\+221\\d{9}$", message = "Format : +221XXXXXXXXX")
    private String telephone;

    @Email
    private String email;

    @NotBlank
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    private String confirmPassword;

    public RegisterPatientRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
