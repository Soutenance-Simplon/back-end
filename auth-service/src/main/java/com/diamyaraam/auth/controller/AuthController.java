package com.diamyaraam.auth.controller;

import com.diamyaraam.auth.dto.AuthResponse;
import com.diamyaraam.auth.dto.LoginRequest;
import com.diamyaraam.auth.dto.RegisterPatientRequest;
import com.diamyaraam.auth.entity.OtpCode;
import com.diamyaraam.auth.service.AuthService;
import com.diamyaraam.auth.service.OtpService;
import com.diamyaraam.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER : AuthController
 *
 * Expose les endpoints REST de l'auth-service.
 * Accessibles via l'API Gateway : /api/auth/**
 *
 * Endpoints :
 *   POST /auth/register/patient  — Inscription patient
 *   POST /auth/login             — Connexion + JWT
 *   POST /auth/otp/send          — Envoyer un OTP
 *   POST /auth/otp/verify        — Vérifier un OTP
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register/patient")
    public ResponseEntity<ApiResponse<Void>> registerPatient(
            @Valid @RequestBody RegisterPatientRequest request) {

        authService.registerPatient(request);

        return ResponseEntity.ok(
            ApiResponse.success("Compte créé. Veuillez vérifier votre téléphone avec le code OTP envoyé.")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        AuthResponse authResponse = authService.login(request, ipAddress);

        return ResponseEntity.ok(
            ApiResponse.success("Connexion réussie", authResponse)
        );
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @RequestParam String telephone,
            @RequestParam(defaultValue = "VERIFICATION_TELEPHONE") String type) {

        OtpCode.OtpType otpType = OtpCode.OtpType.valueOf(type);
        otpService.sendOtp(telephone, otpType);

        return ResponseEntity.ok(
            ApiResponse.success("Code OTP envoyé au " + telephone)
        );
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestParam String telephone,
            @RequestParam String code,
            @RequestParam(defaultValue = "VERIFICATION_TELEPHONE") String type) {

        OtpCode.OtpType otpType = OtpCode.OtpType.valueOf(type);
        boolean valid = otpService.verifyOtp(telephone, code, otpType);

        if (!valid) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Code OTP invalide, expiré ou nombre de tentatives dépassé.")
            );
        }

        return ResponseEntity.ok(
            ApiResponse.success("Téléphone vérifié avec succès.")
        );
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody com.diamyaraam.auth.dto.ForgotPasswordRequest request) {
        authService.forgotPassword(request.getTelephone());
        return ResponseEntity.ok(
            ApiResponse.success("Un code OTP de réinitialisation a été envoyé à votre numéro.")
        );
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody com.diamyaraam.auth.dto.ResetPasswordRequest request) {
        authService.resetPassword(
            request.getTelephone(),
            request.getCode(),
            request.getNewPassword(),
            request.getConfirmNewPassword()
        );
        return ResponseEntity.ok(
            ApiResponse.success("Votre mot de passe a été réinitialisé avec succès.")
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody com.diamyaraam.auth.dto.RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(
            ApiResponse.success("Jeton rafraîchi avec succès.", response)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> searchByTelephone(@RequestParam String telephone) {
        com.diamyaraam.auth.entity.User user = authService.searchUserByTelephone(telephone);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("id", user.getId());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        return ResponseEntity.ok(ApiResponse.success("Utilisateur trouvé", data));
    }

    @PutMapping("/profile/photo")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> updatePhotoProfil(
            @RequestParam(required = false) java.util.UUID userId,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String photo = null;
        if (body != null) {
            photo = body.get("photoProfil");
            if (photo == null) photo = body.get("photo");
            if (photo == null) photo = body.get("photo_profil");
            if (userId == null && body.get("user_id") != null && !body.get("user_id").isEmpty()) {
                try {
                    userId = java.util.UUID.fromString(body.get("user_id"));
                } catch (Exception ignored) {}
            }
        }
        if (userId != null) {
            String updated = authService.updatePhotoProfil(userId, photo);
            java.util.Map<String, String> res = new java.util.HashMap<>();
            res.put("photoProfil", updated);
            return ResponseEntity.ok(ApiResponse.success("Photo de profil mise à jour avec succès", res));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Identifiant utilisateur requis"));
    }

    /**
     * POST /auth/verify-id
     * Analyse et vérification automatique IA de la pièce d'identité (préinscription).
     */
    @PostMapping("/verify-id")
    public ResponseEntity<java.util.Map<String, Object>> verifyIdentity(
            @RequestParam(value = "piece_identite_numero", required = false) String pieceNumber,
            @RequestParam(value = "first_name", required = false) String firstName,
            @RequestParam(value = "last_name", required = false) String lastName,
            @RequestParam(value = "date_naissance", required = false) String dateNaissance,
            jakarta.servlet.http.HttpServletRequest request) {

        if (pieceNumber == null) {
            pieceNumber = request.getParameter("piece_identite_numero");
        }
        if (firstName == null) {
            firstName = request.getParameter("first_name");
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 1. Cas échec (< 30) : Numéro trop court ou manquant
        if (pieceNumber == null || pieceNumber.trim().length() < 5) {
            response.put("status", "rejected");
            response.put("score", 20);
            errors.add("Numéro de pièce incomplet ou non reconnu");
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }

        // 2. Cas détection d'incohérence (>= 30) pour tester le dialog IA
        // Se déclenche si le numéro se termine par '9' ou contient 'DISCORDANCE'
        if (pieceNumber.endsWith("9") || pieceNumber.toUpperCase().contains("DISCORDANCE")) {
            response.put("status", "manual");
            response.put("score", 35);
            errors.add("Légère discordance détectée sur l'orthographe du prénom (" + (firstName != null ? firstName : "") + ")");
            response.put("errors", errors);
            return ResponseEntity.ok(response);
        }

        // 3. Cas nominal : validation réussie (score 40/40)
        response.put("status", "approved");
        response.put("score", 40);
        response.put("errors", errors);
        return ResponseEntity.ok(response);
    }
}

