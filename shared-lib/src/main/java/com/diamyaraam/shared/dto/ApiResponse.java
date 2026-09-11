package com.diamyaraam.shared.dto;

/**
 * DTO : ApiResponse — Réponse standard de tous les services
 *
 * Tous les endpoints retournent ce format standardisé.
 *
 * Exemple succès :
 * { "success": true, "message": "Connexion réussie", "data": {...} }
 *
 * Exemple erreur :
 * { "success": false, "message": "Numéro introuvable", "data": null }
 *
 * Note : pas de Lombok ici car shared-lib n'a pas de dépendance Lombok.
 * On utilise des champs avec constructeurs manuels.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Méthodes statiques utilitaires
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // Getters / Setters manuels (pas de Lombok dans shared-lib)
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
