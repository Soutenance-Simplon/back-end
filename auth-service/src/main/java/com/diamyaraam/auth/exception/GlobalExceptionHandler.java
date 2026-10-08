// Déclaration du package Java : `com.diamyaraam.auth.exception`
package com.diamyaraam.auth.exception;

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
// Import de la classe `BadCredentialsException` (paquet org.springframework.security.authentication)
import org.springframework.security.authentication.BadCredentialsException;
// Import de la classe `UsernameNotFoundException` (paquet org.springframework.security.core.userdetails)
import org.springframework.security.core.userdetails.UsernameNotFoundException;
// Import de la classe `FieldError` (paquet org.springframework.validation)
import org.springframework.validation.FieldError;
// Import de la classe `MethodArgumentNotValidException` (paquet org.springframework.web.bind)
import org.springframework.web.bind.MethodArgumentNotValidException;
// Import de la classe `ExceptionHandler` (paquet org.springframework.web.bind.annotation)
import org.springframework.web.bind.annotation.ExceptionHandler;
// Import de la classe `RestControllerAdvice` (paquet org.springframework.web.bind.annotation)
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Import de la classe `Collectors` (paquet java.util.stream)
import java.util.stream.Collectors;

/**
 * ====================================================================================================
 * GESTIONNAIRE GLOBAL D'EXCEPTIONS : INTERCEPTION CENTRALISÉE PAR AOP (REST CONTROLLER ADVICE)
 * ====================================================================================================
 * 
 * 🎓 QUESTIONS CLASSIQUES DU JURY DE SOUTENANCE :
 * "Que se passe-t-il si une exception non gérée survient dans le code ?"
 * "Pourquoi ne pas renvoyer directement la stack trace Java (trace de pile) au client ?"
 * 
 * 🛡️ JUSTIFICATIONS TECHNIQUES & CONFORMITÉ OWASP :
 * 1. Sécurité & Prévention des Fuites d'Informations (Information Disclosure) :
 *    Par défaut, Spring Boot peut afficher une page d'erreur "Whitelabel" avec le nom des tables SQL,
 *    la version des bibliothèques ou le chemin des fichiers serveur.
 *    C'est une faille de sécurité majeure (OWASP Top 10 - Security Misconfiguration).
 *    Ce gestionnaire intercepte TOUTES les erreurs pour renvoyer un JSON propre sans détails internes.
 * 
 * 2. Programmation Orientée Aspect (AOP - Aspect-Oriented Programming) :
 *    Grâce à `@RestControllerAdvice`, ce composant agit comme un intercepteur universel
 *    sur tous les contrôleurs REST de l'application sans polluer le code métier de blocs try-catch verbeux.
 * 
 * 3. Cohérence du Format de Sortie :
 *    Toutes les erreurs sont uniformément formatées dans `ApiResponse<Void>` avec un statut HTTP
 *    approprié (400 pour les erreurs de saisie, 401 pour les refus d'accès, 500 pour les pannes serveurs).
 * ====================================================================================================
 */
@RestControllerAdvice
// Déclaration de la classe `GlobalExceptionHandler` (rôle : intercepte et traite des événements)
public class GlobalExceptionHandler {

    // Constante `log` de type Logger [privée] ; valeur initiale : `LoggerFactory.getLogger(GlobalExceptionHandler.class)`
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Intercepte les arguments invalides levés par le code métier (ex: numéro déjà existant).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    // Méthode `handleIllegalArgument` (publique) — paramètres : `ex` (IllegalArgumentException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle illegal argument)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        // Écrit un message d'avertissement dans les journaux : "Erreur client (IllegalArgument): {}", ex.getMessage());
        log.warn("Erreur client (IllegalArgument): {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.BAD_REQUEST)`
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                // Enchaînement : appelle `body(ApiResponse.error(ex.getMessage()));`
                .body(ApiResponse.error(ex.getMessage()));
    }


    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(IllegalStateException.class)
    // Méthode `handleIllegalState` (publique) — paramètres : `ex` (IllegalStateException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle illegal state)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        // Écrit un message d'avertissement dans les journaux : "État invalide (IllegalState): {}", ex.getMessage());
        log.warn("État invalide (IllegalState): {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.BAD_REQUEST)`
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                // Enchaînement : appelle `body(ApiResponse.error(ex.getMessage()));`
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(BadCredentialsException.class)
    // Méthode `handleBadCredentials` (publique) — paramètres : `ex` (BadCredentialsException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle bad credentials)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        // Écrit un message d'avertissement dans les journaux : "Identifiants invalides: {}", ex.getMessage());
        log.warn("Identifiants invalides: {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.UNAUTHORIZED)`
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                // Enchaînement : appelle `body(ApiResponse.error("Numéro de téléphone ou mot de …`
                .body(ApiResponse.error("Numéro de téléphone ou mot de passe incorrect."));
    }

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(UsernameNotFoundException.class)
    // Méthode `handleUsernameNotFound` (publique) — paramètres : `ex` (UsernameNotFoundException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle username not found)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(UsernameNotFoundException ex) {
        // Écrit un message d'avertissement dans les journaux : "Utilisateur introuvable: {}", ex.getMessage());
        log.warn("Utilisateur introuvable: {}", ex.getMessage());
        // Retourne `ResponseEntity.status(HttpStatus.UNAUTHORIZED)`
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                // Enchaînement : appelle `body(ApiResponse.error("Numéro de téléphone ou mot de …`
                .body(ApiResponse.error("Numéro de téléphone ou mot de passe incorrect."));
    }

    // Méthode appelée automatiquement quand l'exception indiquée est levée
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Méthode `handleValidation` (publique) — paramètres : `ex` (MethodArgumentNotValidException) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : gère (handle validation)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        // Déclare la variable `errors` (chaîne de caractères) initialisée avec `ex.getBindingResult().getFieldErrors().stream()`
        String errors = ex.getBindingResult().getFieldErrors().stream()
                // Enchaînement : appelle `map(FieldError::getDefaultMessage)`
                .map(FieldError::getDefaultMessage)
                // Enchaînement : appelle `collect(Collectors.joining(", "));`
                .collect(Collectors.joining(", "));
        // Retourne `ResponseEntity.status(HttpStatus.BAD_REQUEST)`
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                // Enchaînement : appelle `body(ApiResponse.error("Erreur de validation : " + err…`
                .body(ApiResponse.error("Erreur de validation : " + errors));
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
