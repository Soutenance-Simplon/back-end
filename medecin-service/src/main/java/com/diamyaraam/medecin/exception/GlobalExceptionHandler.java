// Déclaration du package Java : `com.diamyaraam.medecin.exception`
package com.diamyaraam.medecin.exception;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `Logger` (paquet org.slf4j)
import org.slf4j.Logger;
// Import de la classe `LoggerFactory` (paquet org.slf4j)
import org.slf4j.LoggerFactory;
// Import de la classe `HttpStatus` (paquet org.springframework.http)
import org.springframework.http.HttpStatus;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de la classe `ExceptionHandler` (paquet org.springframework.web.bind.annotation)
import org.springframework.web.bind.annotation.ExceptionHandler;
// Import de la classe `RestControllerAdvice` (paquet org.springframework.web.bind.annotation)
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Gestionnaire global d'exceptions pour tous les contrôleurs REST
@RestControllerAdvice
// Déclaration de la classe `GlobalExceptionHandler` (rôle : intercepte et traite des événements)
public class GlobalExceptionHandler {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(GlobalExceptionHandler.class)`
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(IllegalStateException.class)
    // Méthode `handleIllegalStateException` (publique) — paramètres : `ex` (IllegalStateException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle illegal state exception)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        // Écrit un message d'avertissement dans les journaux : "Conflit de données : {}", ex.getMessage());
        log.warn("Conflit de données : {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.CONFLICT)`
        return ResponseEntity.status(HttpStatus.CONFLICT)
                // Enchaînement : appelle `body(ApiResponse.error(ex.getMessage()));`
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(IllegalArgumentException.class)
    // Méthode `handleIllegalArgumentException` (publique) — paramètres : `ex` (IllegalArgumentException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle illegal argument exception)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Écrit un message d'avertissement dans les journaux : "Requête invalide : {}", ex.getMessage());
        log.warn("Requête invalide : {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.BAD_REQUEST)`
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                // Enchaînement : appelle `body(ApiResponse.error(ex.getMessage()));`
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(Exception.class)
    // Méthode `handleGeneralException` (publique) — paramètres : `ex` (Exception) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle general exception)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        // Écrit un message d'erreur dans les journaux : "Erreur interne inattendue", ex);
        log.error("Erreur interne inattendue", ex);
        // Retourne `ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)`
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                // Enchaînement : appelle `body(ApiResponse.error("Une erreur serveur est survenu…`
                .body(ApiResponse.error("Une erreur serveur est survenue : " + ex.getMessage()));
    }
}
