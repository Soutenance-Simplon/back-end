// Déclaration du package Java : `com.diamyaraam.auth.entity`
package com.diamyaraam.auth.entity;

// Import de toutes les classes du paquet `jakarta.persistence`
import jakarta.persistence.*;

/**
 * Rôle utilisateur : PATIENT, MEDECIN, ADMIN (RM004)
 */
@Entity
// Table SQL associée à l'entité : table « role », schéma « auth_schema »
@Table(name = "role", schema = "auth_schema")
// Déclaration de la classe `Role`
public class Role {

    // Clé primaire de l'entité
    @Id
    // Valeur de la clé primaire générée automatiquement
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Attribut `id` (identifiant unique) de type entier long [privée]
    private Long id;

    // Colonne SQL associée à l'attribut : nom « nom_role », obligatoire (NOT NULL), valeur unique, longueur max 50
    @Column(name = "nom_role", unique = true, nullable = false, length = 50)
    // Attribut `nomRole` de type chaîne de caractères [privée]
    private String nomRole;    // "PATIENT", "MEDECIN", "ADMIN"

    // Constructeur de `Role` sans paramètre
    public Role() {}

    // Constructeur de `Role` — paramètres : `id` (entier long), `nomRole` (chaîne de caractères) (injection des dépendances par Spring)
    public Role(Long id, String nomRole) {
        // Initialise l'attribut `id` avec la valeur de id
        this.id = id;
        // Initialise l'attribut `nomRole` avec la valeur de nomRole
        this.nomRole = nomRole;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `id`
    public Long getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(Long id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nomRole`
    public String getNomRole() { return nomRole; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nomRole`
    public void setNomRole(String nomRole) { this.nomRole = nomRole; }
}
