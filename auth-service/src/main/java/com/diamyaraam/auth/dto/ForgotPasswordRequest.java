package com.diamyaraam.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ForgotPasswordRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    @Pattern(regexp = "^(\\+221|00221)?[76][0-9]{8}$", message = "Format de téléphone Sénégalais invalide.")
    private String telephone;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
