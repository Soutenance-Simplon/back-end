package com.diamyaraam.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    @Pattern(regexp = "^(\\+221|00221)?[76][0-9]{8}$", message = "Format de téléphone Sénégalais invalide.")
    private String telephone;

    @NotBlank(message = "Le code OTP est obligatoire.")
    @Size(min = 6, max = 6, message = "Le code OTP doit comporter 6 chiffres.")
    private String code;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit comporter au moins 8 caractères.")
    private String newPassword;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire.")
    private String confirmNewPassword;

    public ResetPasswordRequest() {}

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
