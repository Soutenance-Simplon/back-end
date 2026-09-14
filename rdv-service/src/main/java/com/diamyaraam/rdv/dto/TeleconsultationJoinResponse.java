package com.diamyaraam.rdv.dto;

public class TeleconsultationJoinResponse {
    private String roomName;
    private String serverUrl;
    private String displayName;
    private String token;
    private String role; // "MEDECIN" ou "PATIENT"

    public TeleconsultationJoinResponse() {}

    public TeleconsultationJoinResponse(String roomName, String serverUrl, String displayName, String token, String role) {
        this.roomName = roomName;
        this.serverUrl = serverUrl;
        this.displayName = displayName;
        this.token = token;
        this.role = role;
    }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getServerUrl() { return serverUrl; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
