package com.diamyaraam.shared.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String telephone;
    private String email;
    private String photoProfil;
    private String roleName;
    private String accountStatus;
    private LocalDateTime createdAt;

    public UserDto() {}

    public UserDto(UUID id, String firstName, String lastName, String telephone, String email, String photoProfil, String roleName, String accountStatus, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.email = email;
        this.photoProfil = photoProfil;
        this.roleName = roleName;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
