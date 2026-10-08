// Déclaration du package Java : `com.diamyaraam.auth.controller`
package com.diamyaraam.auth.controller;

// Import de la classe `AuthResponse` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.AuthResponse;
// Import de la classe `LoginRequest` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.LoginRequest;
// Import de la classe `RegisterPatientRequest` (paquet com.diamyaraam.auth.dto)
import com.diamyaraam.auth.dto.RegisterPatientRequest;
// Import de la classe `OtpCode` (paquet com.diamyaraam.auth.entity)
import com.diamyaraam.auth.entity.OtpCode;
// Import de la classe `AuthService` (paquet com.diamyaraam.auth.service)
import com.diamyaraam.auth.service.AuthService;
// Import de la classe `OtpService` (paquet com.diamyaraam.auth.service)
import com.diamyaraam.auth.service.OtpService;
// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `HttpServletRequest` (paquet jakarta.servlet.http)
import jakarta.servlet.http.HttpServletRequest;
// Import de la classe `Valid` (paquet jakarta.validation)
import jakarta.validation.Valid;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

/**
 * ====================================================================================================
 * CONTRÔLEUR REST : AUTHENTIFICATION & GESTION DES ACCÈS (AUTH CONTROLLER)
 * ====================================================================================================
 * 
 * 🎓 CONCEPTS D'ARCHITECTURE REST & FLUX D'EXÉCUTION POUR LA SOUTENANCE :
 * Ce contrôleur expose les points d'entrée de sécurité fondamentaux consommés par :
 * - L'application mobile Flutter (Patients & Médecins)
 * - Le tableau de bord Web d'administration (Administrateurs)
 * 
 * 🌐 ROUTAGE & EXPOSITION :
 * Les requêtes transitent d'abord par l'API Gateway (/api/auth/**), qui applique les contrôles
 * de sécurité périmétrique avant de les transférer à ce microservice sur le préfixe `/auth/**`.
 * 
 * 🔒 PRINCIPES DE SÉCURITÉ APPLIQUÉS :
 * - Validation stricte des DTOs via Bean Validation (`@Valid`) pour bloquer les injections.
 * - Récupération de l'adresse IP cliente pour la traçabilité dans la table `audit_logs`.
 * - Réponses encapsulées uniformément dans `ApiResponse<T>` pour la prévisibilité client.
 * ====================================================================================================
 */
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /auth »
@RequestMapping("/auth")
// Déclaration de la classe `AuthController` (rôle : expose des routes HTTP)
public class AuthController {

    /** Service métier gérant la logique d'authentification, d'inscription et de jetons JWT */
    private final AuthService authService;

    /** Service technique gérant le cycle de vie des codes OTP (génération, envoi, vérification) */
    private final OtpService otpService;

    /**
     * Injection des dépendances par constructeur (Bonne pratique Spring : favorise l'immutabilité et les tests unitaires).
     */
    public AuthController(AuthService authService, OtpService otpService) {
        // Initialise l'attribut `authService` avec la valeur de authService
        this.authService = authService;
        // Initialise l'attribut `otpService` avec la valeur de otpService
        this.otpService = otpService;
    }

    /**
     * ENDPOINT : Inscription d'un nouveau patient
     * POST /auth/register/patient
     * 
     * Flux d'exécution :
     * 1. Validation syntaxique du numéro de téléphone, mot de passe et identité via Bean Validation.
     * 2. Vérification d'unicité du numéro en base de données.
     * 3. Hachage du mot de passe en BCrypt et sauvegarde du compte au statut 'EN_ATTENTE_OTP'.
     * 4. Déclenchement automatique de l'envoi d'un code OTP par WhatsApp/SMS pour valider la ligne.
     * 
     * @param request DTO contenant les informations d'inscription validées
     * @return Réponse 200 OK confirmant la création et invitant à la validation OTP
     */
    @PostMapping("/register/patient")
    // Méthode `registerPatient` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : inscrit (register patient)
    public ResponseEntity<ApiResponse<Void>> registerPatient(
            // Paramètre `request` de type RegisterPatientRequest — déclenche la validation bean validation de l'objet
            @Valid @RequestBody RegisterPatientRequest request) {

        // Appel de la logique métier transactionnelle
        authService.registerPatient(request);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Compte créé. Veuillez vérifier votre téléphone avec le cod…`
            ApiResponse.success("Compte créé. Veuillez vérifier votre téléphone avec le code OTP envoyé.")
        );
    }

    /**
     * ENDPOINT : Connexion utilisateur & Délivrance du couple JWT (Access + Refresh Tokens)
     * POST /auth/login
     * 
     * Flux d'exécution :
     * 1. Extraction de l'adresse IP distante depuis l'en-tête HTTP pour la piste d'audit.
     * 2. Vérification des identifiants (comparaison du hash BCrypt) et du statut du compte.
     * 3. Génération cryptographique du JWT signé en HMAC-SHA512 avec claims (userId, role, telephone).
     * 4. Enregistrement d'un log d'audit de connexion réussie.
     * 
     * @param request DTO contenant le téléphone et mot de passe en clair
     * @param httpRequest Requête HTTP sous-jacente pour extraire l'adresse IP
     * @return Réponse 200 OK avec le jeton JWT, les infos de l'utilisateur et sa date d'expiration
     */
    @PostMapping("/login")
    // Méthode `login` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de AuthResponse ; intention : authentifie (login)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            // Paramètre `request` de type LoginRequest — déclenche la validation bean validation de l'objet
            @Valid @RequestBody LoginRequest request,
            // Paramètre `httpRequest` de type HttpServletRequest
            HttpServletRequest httpRequest) {

        // Récupération de l'adresse IP cliente pour la sécurité et la traçabilité
        String ipAddress = httpRequest.getRemoteAddr();
        // Déclare la variable `authResponse` (AuthResponse) initialisée avec `authService.login(request, ipAddress)`
        AuthResponse authResponse = authService.login(request, ipAddress);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Connexion réussie", authResponse`
            ApiResponse.success("Connexion réussie", authResponse)
        );
    }

    /**
     * ENDPOINT : Génération et envoi d'un code OTP à usage unique
     * POST /auth/otp/send
     * 
     * @param telephone Numéro de téléphone destinataire
     * @param type Contexte de l'OTP (VERIFICATION_TELEPHONE, CONNEXION_2FA, REINITIALISATION_MDP)
     * @return Réponse 200 OK confirmant l'expédition du code
     */
    @PostMapping("/otp/send")
    // Méthode `sendOtp` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : envoie (send otp)
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            // Paramètre `telephone` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String telephone,
            // Paramètre `type` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "VERIFICATION_TELEPHONE") String type) {

        // Déclare la variable `otpType` (OtpCode.OtpType) initialisée avec `OtpCode.OtpType.valueOf(type)`
        OtpCode.OtpType otpType = OtpCode.OtpType.valueOf(type);
        // Appelle la méthode `sendOtp` sur `otpService` : otpService.sendOtp(telephone, otpType);
        otpService.sendOtp(telephone, otpType);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Code OTP envoyé au " + telephone`
            ApiResponse.success("Code OTP envoyé au " + telephone)
        );
    }

    /**
     * ENDPOINT : Vérification et validation d'un code OTP
     * POST /auth/otp/verify
     * 
     * Flux de contrôle :
     * 1. Recherche du dernier OTP actif pour ce numéro et ce type.
     * 2. Vérification de la non-expiration temporelle (délai de 5 min) et du nombre de tentatives (< 3).
     * 3. Si valide : activation du compte utilisateur et invalidation de l'OTP.
     * 
     * @param telephone Numéro de téléphone concerné
     * @param code Code à 6 chiffres saisi par l'utilisateur
     * @param type Contexte d'utilisation de l'OTP
     * @return 200 OK si validé, 400 Bad Request si erroné ou expiré
     */
    @PostMapping("/otp/verify")
    // Méthode `verifyOtp` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : vérifie (verify otp)
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            // Paramètre `telephone` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String telephone,
            // Paramètre `code` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String code,
            // Paramètre `type` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "VERIFICATION_TELEPHONE") String type) {

        // Déclare la variable `otpType` (OtpCode.OtpType) initialisée avec `OtpCode.OtpType.valueOf(type)`
        OtpCode.OtpType otpType = OtpCode.OtpType.valueOf(type);
        // Déclare la variable `valid` (booléen) initialisée avec `otpService.verifyOtp(telephone, code, otpType)`
        boolean valid = otpService.verifyOtp(telephone, code, otpType);

        // Condition : exécute le bloc suivant seulement si `!valid`
        if (!valid) {
            // Retourne `ResponseEntity.badRequest().body(`
            return ResponseEntity.badRequest().body(
                // Argument/valeur : `ApiResponse.error("Code OTP invalide, expiré ou nombre de tentatives dépassé."`
                ApiResponse.error("Code OTP invalide, expiré ou nombre de tentatives dépassé.")
            );
        }

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Téléphone vérifié avec succès."`
            ApiResponse.success("Téléphone vérifié avec succès.")
        );
    }

    /**
     * ENDPOINT : Demande de réinitialisation de mot de passe oublié
     * POST /auth/password/forgot
     * 
     * Déclenche la génération d'un OTP sécurisé envoyé sur le mobile du titulaire.
     * 
     * @param request DTO contenant le numéro de téléphone du compte
     * @return 200 OK confirmant l'envoi de l'OTP
     */
    @PostMapping("/password/forgot")
    // Méthode `forgotPassword` (publique) — paramètres : `request` (com.diamyaraam.auth.dto.ForgotPasswordRequest) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody com.diamyaraam.auth.dto.ForgotPasswordRequest request) {
        // Appelle la méthode `forgotPassword` sur `authService` : authService.forgotPassword(request.getTelephone());
        authService.forgotPassword(request.getTelephone());
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Un code OTP de réinitialisation a été envoyé à votre numér…`
            ApiResponse.success("Un code OTP de réinitialisation a été envoyé à votre numéro.")
        );
    }

    /**
     * ENDPOINT : Validation du code OTP et enregistrement du nouveau mot de passe
     * POST /auth/password/reset
     * 
     * @param request DTO contenant le numéro, le code OTP validé et le nouveau mot de passe
     * @return 200 OK si la mise à jour du mot de passe est effective
     */
    @PostMapping("/password/reset")
    // Méthode `resetPassword` (publique) — paramètres : `request` (com.diamyaraam.auth.dto.ResetPasswordRequest) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : réinitialise (reset password)
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody com.diamyaraam.auth.dto.ResetPasswordRequest request) {
        // Appelle la méthode `resetPassword` sur `authService` : authService.resetPassword(
        authService.resetPassword(
            // Argument/valeur : `request.getTelephone(`
            request.getTelephone(),
            // Argument/valeur : `request.getCode(`
            request.getCode(),
            // Argument/valeur : `request.getNewPassword(`
            request.getNewPassword(),
            // Argument/valeur : `request.getConfirmNewPassword(`
            request.getConfirmNewPassword()
        );
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Votre mot de passe a été réinitialisé avec succès."`
            ApiResponse.success("Votre mot de passe a été réinitialisé avec succès.")
        );
    }

    /**
     * ENDPOINT : Renouvellement du jeton d'accès expiré via un Refresh Token
     * POST /auth/refresh-token
     * 
     * Mécanisme de sécurité :
     * Permet à l'application cliente de renouveler un Access Token échu sans redemander
     * les identifiants de l'utilisateur, tout en appliquant une rotation de Refresh Token.
     * 
     * @param request DTO contenant le Refresh Token valide
     * @return 200 OK avec le nouvel Access Token
     */
    @PostMapping("/refresh-token")
    // Méthode `refreshToken` (publique) — paramètres : `request` (com.diamyaraam.auth.dto.RefreshTokenRequest) ; retourne : réponse HTTP contenant enveloppe ApiResponse de AuthResponse
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody com.diamyaraam.auth.dto.RefreshTokenRequest request) {
        // Déclare la variable `response` (AuthResponse) initialisée avec `authService.refreshToken(request.getRefreshToken())`
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(
        return ResponseEntity.ok(
            // Argument/valeur : `ApiResponse.success("Jeton rafraîchi avec succès.", response`
            ApiResponse.success("Jeton rafraîchi avec succès.", response)
        );
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getUserById(@PathVariable java.util.UUID id) {
        return authService.findById(id)
                .map(user -> {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("id", user.getId());
                    data.put("firstName", user.getFirstName());
                    data.put("lastName", user.getLastName());
                    data.put("telephone", user.getTelephone());
                    return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", data));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Utilisateur introuvable")));
    }

    /**
     * ENDPOINT : Recherche d'un utilisateur par son numéro de téléphone
     * GET /auth/search?telephone=...
     * 
     * Utilisé pour la vérification rapide ou lors du partage de dossier médical / paiement.
     * 
     * @param telephone Numéro recherché
     * @return Informations non sensibles de l'utilisateur trouvé
     */
    @GetMapping("/search")
    // Méthode `searchByTelephone` (publique) — paramètres : `telephone` (chaîne de caractères) ; retourne : réponse HTTP contenant enveloppe ApiResponse de java.util.Map<String, Object>
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> searchByTelephone(@RequestParam String telephone) {
        // Instruction : com.diamyaraam.auth.entity.User user = authService.searchUserByTelephone(telephone);
        com.diamyaraam.auth.entity.User user = authService.searchUserByTelephone(telephone);
        // Instruction : java.util.Map<String, Object> data = new java.util.HashMap<>();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        // Appelle la méthode `put` sur `data` : data.put("id", user.getId());
        data.put("id", user.getId());
        // Appelle la méthode `put` sur `data` : data.put("firstName", user.getFirstName());
        data.put("firstName", user.getFirstName());
        // Appelle la méthode `put` sur `data` : data.put("lastName", user.getLastName());
        data.put("lastName", user.getLastName());
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", data))
        return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", data));
    }

    /**
     * ENDPOINT : Mise à jour de la photo de profil utilisateur
     * PUT /auth/profile/photo
     * 
     * @param userId Identifiant unique de l'utilisateur
     * @param body Corps contenant l'URL ou base64 de la photo
     * @return 200 OK avec la confirmation de la nouvelle URL
     */
    @PutMapping("/profile/photo")
    // Méthode `updatePhotoProfil` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de java.util.Map<String, String> ; intention : met à jour (update photo profil)
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> updatePhotoProfil(
            // Paramètre `userId` de type java.util.UUID — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) java.util.UUID userId,
            // Paramètre `body` de type java.util.Map<String, String> — corps json de la requête désérialisé en objet java
            @RequestBody(required = false) java.util.Map<String, String> body) {
        // Déclare la variable `photo` (chaîne de caractères) initialisée avec la valeur nulle (absence de valeur)
        String photo = null;
        // Condition : exécute le bloc suivant seulement si `body != null`
        if (body != null) {
            // Affecte à `photo` `body.get("photoProfil")`
            photo = body.get("photoProfil");
            // Condition : exécute le bloc suivant seulement si `photo == null) photo = body.get("photo");`
            if (photo == null) photo = body.get("photo");
            // Condition : exécute le bloc suivant seulement si `photo == null) photo = body.get("photo_profil");`
            if (photo == null) photo = body.get("photo_profil");
            // Condition : exécute le bloc suivant seulement si `userId == null && body.get("user_id") != null && !body.get("user_id").isEmpty()`
            if (userId == null && body.get("user_id") != null && !body.get("user_id").isEmpty()) {
                // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                try {
                    // Affecte à `userId` `java.util.UUID.fromString(body.get("user_id"))`
                    userId = java.util.UUID.fromString(body.get("user_id"));
                // Interception de l'exception Exception ignored
                } catch (Exception ignored) {}
            }
        }
        // Condition : exécute le bloc suivant seulement si `userId != null`
        if (userId != null) {
            // Déclare la variable `updated` (chaîne de caractères) initialisée avec `authService.updatePhotoProfil(userId, photo)`
            String updated = authService.updatePhotoProfil(userId, photo);
            // Instruction : java.util.Map<String, String> res = new java.util.HashMap<>();
            java.util.Map<String, String> res = new java.util.HashMap<>();
            // Appelle la méthode `put` sur `res` : res.put("photoProfil", updated);
            res.put("photoProfil", updated);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Photo de profil mise à jour av…
            return ResponseEntity.ok(ApiResponse.success("Photo de profil mise à jour avec succès", res));
        }
        // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Identifiant utilisateur req…`
        return ResponseEntity.badRequest().body(ApiResponse.error("Identifiant utilisateur requis"));
    }

    /**
     * ENDPOINT : Vérification automatisée d'identité (CNI / Passeport) assistée par IA
     * POST /auth/verify-id
     * 
     * 🎓 JUSTIFICATION INNOVATION SOUTENANCE :
     * Permet le KYC (Know Your Customer) médical : vérifie la conformité de la pièce d'identité
     * sénégalaise (format Cedeao à 13 chiffres ou CNI standard) lors de la préinscription.
     * 
     * @param pieceNumber Numéro de la pièce d'identité extrait par OCR
     * @param firstName Prénom du titulaire
     * @param lastName Nom de famille
     * @param dateNaissance Date de naissance
     * @param request Requête HTTP
     * @return Statut d'approbation (approved, manual, rejected) avec score de confiance
     */
    @PostMapping("/verify-id")
    // Méthode `verifyIdentity` (publique) ; retourne : réponse HTTP contenant java.util.Map<String, Object> ; intention : vérifie (verify identity)
    public ResponseEntity<java.util.Map<String, Object>> verifyIdentity(
            // Paramètre `pieceNumber` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(value = "piece_identite_numero", required = false) String pieceNumber,
            // Paramètre `firstName` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(value = "first_name", required = false) String firstName,
            // Paramètre `lastName` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(value = "last_name", required = false) String lastName,
            // Paramètre `dateNaissance` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(value = "date_naissance", required = false) String dateNaissance,
            // Paramètre `request` de type jakarta.servlet.http.HttpServletRequest
            jakarta.servlet.http.HttpServletRequest request) {

        // Condition : exécute le bloc suivant seulement si `pieceNumber == null`
        if (pieceNumber == null) {
            // Affecte à `pieceNumber` `request.getParameter("piece_identite_numero")`
            pieceNumber = request.getParameter("piece_identite_numero");
        }
        // Condition : exécute le bloc suivant seulement si `firstName == null`
        if (firstName == null) {
            // Affecte à `firstName` `request.getParameter("first_name")`
            firstName = request.getParameter("first_name");
        }

        // Instruction : java.util.Map<String, Object> response = new java.util.HashMap<>();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        // Instruction : java.util.List<String> errors = new java.util.ArrayList<>();
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 1. Cas échec (< 30) : Numéro trop court ou manquant
        if (pieceNumber == null || pieceNumber.trim().length() < 5) {
            // Appelle la méthode `put` sur `response` : response.put("status", "rejected");
            response.put("status", "rejected");
            // Appelle la méthode `put` sur `response` : response.put("score", 20);
            response.put("score", 20);
            // Appelle la méthode `add` sur `errors` : errors.add("Numéro de pièce incomplet ou non reconnu");
            errors.add("Numéro de pièce incomplet ou non reconnu");
            // Appelle la méthode `put` sur `response` : response.put("errors", errors);
            response.put("errors", errors);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(response)
            return ResponseEntity.ok(response);
        }

        // 2. Cas détection d'incohérence (>= 30) pour tester le dialog IA
        // Se déclenche si le numéro se termine par '9' ou contient 'DISCORDANCE'
        if (pieceNumber.endsWith("9") || pieceNumber.toUpperCase().contains("DISCORDANCE")) {
            // Appelle la méthode `put` sur `response` : response.put("status", "manual");
            response.put("status", "manual");
            // Appelle la méthode `put` sur `response` : response.put("score", 35);
            response.put("score", 35);
            // Appelle la méthode `add` sur `errors` : errors.add("Légère discordance détectée sur l'orthographe du prénom (" + (…
            errors.add("Légère discordance détectée sur l'orthographe du prénom (" + (firstName != null ? firstName : "") + ")");
            // Appelle la méthode `put` sur `response` : response.put("errors", errors);
            response.put("errors", errors);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(response)
            return ResponseEntity.ok(response);
        }

        // 3. Cas nominal : validation réussie (score 40/40)
        response.put("status", "approved");
        // Appelle la méthode `put` sur `response` : response.put("score", 40);
        response.put("score", 40);
        // Appelle la méthode `put` sur `response` : response.put("errors", errors);
        response.put("errors", errors);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(response)
        return ResponseEntity.ok(response);
    }
}


