package com.diamyaraam.auth.entity;

import jakarta.persistence.*;

/**
 * Rôle utilisateur : PATIENT, MEDECIN, ADMIN (RM004)
 */
@Entity
@Table(name = "role", schema = "auth_schema")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_role", unique = true, nullable = false, length = 50)
    private String nomRole;    // "PATIENT", "MEDECIN", "ADMIN"

    public Role() {}

    public Role(Long id, String nomRole) {
        this.id = id;
        this.nomRole = nomRole;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomRole() { return nomRole; }
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}
