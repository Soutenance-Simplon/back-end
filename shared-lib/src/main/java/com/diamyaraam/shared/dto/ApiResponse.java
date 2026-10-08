// Déclaration du package Java : `com.diamyaraam.shared.dto`
package com.diamyaraam.shared.dto;

/**
 * ====================================================================================================
 * DTO PARTAGÉ : CONTENEUR UNIFIÉ DE RÉPONSE HTTP (API RESPONSE WRAPPER)
 * ====================================================================================================
 * 
 * 🎓 PATRON DE CONCEPTION & JUSTIFICATION POUR LA SOUTENANCE :
 * Pourquoi unifier toutes les réponses REST de l'architecture microservices ?
 * 
 * 1. Cohérence & Prévisibilité pour les Applications Clientes :
 *    Que ce soit l'application mobile Flutter ou le dashboard web Vue.js, chaque requête API reçoit
 *    une enveloppe JSON prévisible au format :
 *    {
 *       "success": boolean,
 *       "message": string,
 *       "data": T (objet, liste, ou null)
 *    }
 * 
 * 2. Traitement Centralisé des Erreurs :
 *    Les intercepteurs HTTP des clients (Dio/Http en Flutter, Axios en Vue) peuvent systématiquement
 *    vérifier `response.data.success` et afficher directement le message informatif à l'utilisateur
 *    sans devoir deviner la structure de la charge utile selon le microservice appelé.
 * 
 * 3. Typage Générique Fort en Java (<T>) :
 *    L'utilisation du type générique Java permet de conserver le typage strict à la compilation
 *    tout en réutilisant la même enveloppe pour un UserDto, un MedecinDto, une List<RendezVous>, etc.
 * 
 * 4. Découplage de Dépendances :
 *    Ce module `shared-lib` est conçu pour être ultra-léger et n'embarque délibérément aucune
 *    dépendance externe (pas de Lombok, pas de Spring Web). Les constructeurs, accesseurs et mutateurs
 *    sont donc codés en Java pur pour une portabilité maximale entre projets.
 * ====================================================================================================
 */
public class ApiResponse<T> {

    /** Indicateur booléen de succès métier (true = opération réussie, false = anomalie ou échec) */
    private boolean success;

    /** Message textuel explicatif en français (affichable directement à l'écran de l'utilisateur) */
    private String message;

    /** Charge utile de données générique typée (Payload métier) */
    private T data;

    /**
     * Constructeur par défaut sans argument (requis pour les mécanismes de désérialisation JSON Jackson).
     */
    public ApiResponse() {}

    /**
     * Constructeur complet paramétré.
     * 
     * @param success Vrai si l'opération a réussi, faux sinon
     * @param message Message d'accompagnement
     * @param data Données associées à la réponse
     */
    public ApiResponse(boolean success, String message, T data) {
        // Initialise l'attribut `success` avec la valeur de success
        this.success = success;
        // Initialise l'attribut `message` avec la valeur de message
        this.message = message;
        // Initialise l'attribut `data` avec la valeur de data
        this.data = data;
    }

    /**
     * Fabrique statique pour créer une réponse de succès contenant des données.
     * 
     * @param <T> Type de la donnée retournée
     * @param message Message de succès en français (ex: "Connexion réussie")
     * @param data Objet résultat
     * @return Nouvelle instance ApiResponse initialisée avec success = true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        // Retourne une nouvelle instance de ApiResponse<>
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Fabrique statique pour créer une réponse de succès sans donnée (ex: validation d'action, suppression).
     * 
     * @param <T> Type de donnée (null)
     * @param message Message de succès (ex: "Profil mis à jour")
     * @return Nouvelle instance ApiResponse avec data = null
     */
    public static <T> ApiResponse<T> success(String message) {
        // Retourne une nouvelle instance de ApiResponse<>
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Fabrique statique pour créer une réponse d'erreur métier ou de validation.
     * 
     * @param <T> Type de donnée (null)
     * @param message Message d'erreur explicatif (ex: "Numéro de téléphone déjà utilisé")
     * @return Nouvelle instance ApiResponse initialisée avec success = false
     */
    public static <T> ApiResponse<T> error(String message) {
        // Retourne une nouvelle instance de ApiResponse<>
        return new ApiResponse<>(false, message, null);
    }

    // ================================================================================================
    // ACCESSEURS (GETTERS) ET MUTATEURS (SETTERS) EXPLICITES
    // ================================================================================================

    // Méthode `isSuccess` (publique) — sans paramètre ; retourne : booléen ; intention : teste si (is success)
    public boolean isSuccess() { 
        // Retourne la valeur de success
        return success; 
    }
    
    // Méthode `setSuccess` (publique) — paramètres : `success` (booléen) ; retourne : aucune valeur ; intention : modifie (set success)
    public void setSuccess(boolean success) { 
        // Initialise l'attribut `success` avec la valeur de success
        this.success = success; 
    }

    // Méthode `getMessage` (publique) — sans paramètre ; retourne : chaîne de caractères ; intention : récupère (get message)
    public String getMessage() { 
        // Retourne la valeur de message
        return message; 
    }
    
    // Méthode `setMessage` (publique) — paramètres : `message` (chaîne de caractères) ; retourne : aucune valeur ; intention : modifie (set message)
    public void setMessage(String message) { 
        // Initialise l'attribut `message` avec la valeur de message
        this.message = message; 
    }

    // Méthode `getData` (publique) — sans paramètre ; retourne : T ; intention : récupère (get data)
    public T getData() { 
        // Retourne la valeur de data
        return data; 
    }
    
    // Méthode `setData` (publique) — paramètres : `data` (T) ; retourne : aucune valeur ; intention : modifie (set data)
    public void setData(T data) { 
        // Initialise l'attribut `data` avec la valeur de data
        this.data = data; 
    }
}

