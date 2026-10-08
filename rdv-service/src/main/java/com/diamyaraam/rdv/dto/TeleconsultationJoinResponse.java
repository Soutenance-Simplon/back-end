// Déclaration du package Java : `com.diamyaraam.rdv.dto`
package com.diamyaraam.rdv.dto;

// Déclaration de la classe `TeleconsultationJoinResponse` (rôle : représente une réponse sortante)
public class TeleconsultationJoinResponse {
    // Attribut `roomName` de type chaîne de caractères [privée]
    private String roomName;
    // Attribut `serverUrl` de type chaîne de caractères [privée]
    private String serverUrl;
    // Attribut `displayName` de type chaîne de caractères [privée]
    private String displayName;
    // Attribut `token` (jeton) de type chaîne de caractères [privée]
    private String token;
    // Attribut `role` (rôle de l'utilisateur) de type chaîne de caractères [privée]
    private String role; // "MEDECIN" ou "PATIENT"

    // Constructeur de `TeleconsultationJoinResponse` sans paramètre
    public TeleconsultationJoinResponse() {}

    // Constructeur de `TeleconsultationJoinResponse` — paramètres : `roomName` (chaîne de caractères), `serverUrl` (chaîne de caractères), `displayName` (chaîne de caractères), `token` (chaîne de caractères), `role` (chaîne de caractères) (injection des dépendances par Spring)
    public TeleconsultationJoinResponse(String roomName, String serverUrl, String displayName, String token, String role) {
        // Initialise l'attribut `roomName` avec la valeur de roomName
        this.roomName = roomName;
        // Initialise l'attribut `serverUrl` avec la valeur de serverUrl
        this.serverUrl = serverUrl;
        // Initialise l'attribut `displayName` avec la valeur de displayName
        this.displayName = displayName;
        // Initialise l'attribut `token` avec la valeur de token
        this.token = token;
        // Initialise l'attribut `role` avec la valeur de role
        this.role = role;
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `roomName`
    public String getRoomName() { return roomName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `roomName`
    public void setRoomName(String roomName) { this.roomName = roomName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `serverUrl`
    public String getServerUrl() { return serverUrl; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `serverUrl`
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `displayName`
    public String getDisplayName() { return displayName; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `displayName`
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `token`
    public String getToken() { return token; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `token`
    public void setToken(String token) { this.token = token; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `role`
    public String getRole() { return role; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `role`
    public void setRole(String role) { this.role = role; }
}
