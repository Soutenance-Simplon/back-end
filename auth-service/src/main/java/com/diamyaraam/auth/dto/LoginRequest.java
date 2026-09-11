package com.diamyaraam.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\+221\\d{9}$", message = "Format : +221XXXXXXXXX")
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    public LoginRequest() {}

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
