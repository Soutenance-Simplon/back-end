// Déclaration du package Java : `com.diamyaraam.wallet.controller`
package com.diamyaraam.wallet.controller;

// Import de la classe `ApiResponse` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.ApiResponse;
// Import de la classe `Beneficiaire` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Beneficiaire;
// Import de la classe `Portefeuille` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Portefeuille;
// Import de la classe `Transaction` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Transaction;
// Import de la classe `WalletService` (paquet com.diamyaraam.wallet.service)
import com.diamyaraam.wallet.service.WalletService;
// Import de la classe `ResponseEntity` (paquet org.springframework.http)
import org.springframework.http.ResponseEntity;
// Import de toutes les classes du paquet `org.springframework.web.bind.annotation`
import org.springframework.web.bind.annotation.*;

// Import de la classe `BigDecimal` (paquet java.math)
import java.math.BigDecimal;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Contrôleur REST : les valeurs retournées sont sérialisées en JSON
@RestController
// Associe une URL (préfixe de route) à ce contrôleur ou à cette méthode sur le chemin « /wallet »
@RequestMapping({"/wallet", "/portefeuilles"})
// Déclaration de la classe `WalletController` (rôle : expose des routes HTTP)
public class WalletController {

    // Attribut `walletService` de type WalletService — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final WalletService walletService;

    // Constructeur de `WalletController` — paramètres : `walletService` (WalletService) (injection des dépendances par Spring)
    public WalletController(WalletService walletService) {
        // Initialise l'attribut `walletService` avec la valeur de walletService
        this.walletService = walletService;
    }

    // Route HTTP GET sur le chemin « /user/{userId} »
    @GetMapping("/user/{userId}")
    // Méthode `getPortefeuille` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Portefeuille ; intention : récupère (get portefeuille)
    public ResponseEntity<ApiResponse<Portefeuille>> getPortefeuille(@PathVariable UUID userId) {
        // Déclare la variable `p` (Portefeuille) initialisée avec `walletService.getOrCreatePortefeuille(userId)`
        Portefeuille p = walletService.getOrCreatePortefeuille(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Portefeuille Santé", p))
        return ResponseEntity.ok(ApiResponse.success("Portefeuille Santé", p));
    }

    // Route HTTP POST sur le chemin « /deposer »
    @PostMapping("/deposer")
    // Méthode `deposer` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Transaction ; intention : dépose (deposer)
    public ResponseEntity<ApiResponse<Transaction>> deposer(
            // Paramètre `userId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID userId,
            // Paramètre `montant` de type montant décimal précis — paramètre de requête http (?clé=valeur)
            @RequestParam BigDecimal montant,
            // Paramètre `moyen` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "MOBILE_MONEY_ORANGE") String moyen,
            // Paramètre `reference` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String reference) {
        // Déclare la variable `m` (Transaction.MoyenPaiement) initialisée avec `Transaction.MoyenPaiement.valueOf(moyen)`
        Transaction.MoyenPaiement m = Transaction.MoyenPaiement.valueOf(moyen);
        // Déclare la variable `tx` (Transaction) initialisée avec `walletService.deposerFonds(userId, montant, m, reference)`
        Transaction tx = walletService.deposerFonds(userId, montant, m, reference);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Dépôt effectué avec succès", t…
        return ResponseEntity.ok(ApiResponse.success("Dépôt effectué avec succès", tx));
    }

    // Route HTTP POST sur le chemin « /payer-service »
    @PostMapping("/payer-service")
    // Méthode `payerService` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Transaction ; intention : règle (payer service)
    public ResponseEntity<ApiResponse<Transaction>> payerService(
            // Paramètre `userId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID userId,
            // Paramètre `montant` de type montant décimal précis — paramètre de requête http (?clé=valeur)
            @RequestParam BigDecimal montant,
            // Paramètre `type` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "PAIEMENT_TELECONSULTATION") String type,
            // Paramètre `serviceId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) UUID serviceId,
            // Paramètre `description` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String description,
            // Paramètre `medecinUserId` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String medecinUserId,
            // Paramètre `medecinId` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String medecinId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `targetMedecin` (identifiant UUID) initialisée avec la valeur nulle (absence de valeur)
            UUID targetMedecin = null;
            // Déclare la variable `rawMed` (chaîne de caractères) initialisée avec `(medecinUserId != null && !medecinUserId.trim().isEmpty()) ? medecinUserId.trim…`
            String rawMed = (medecinUserId != null && !medecinUserId.trim().isEmpty()) ? medecinUserId.trim() : (medecinId != null ? medecinId.trim() : null);
            // Condition : exécute le bloc suivant seulement si `rawMed != null && !rawMed.isEmpty()`
            if (rawMed != null && !rawMed.isEmpty()) {
                // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                try {
                    // Affecte à `targetMedecin` `UUID.fromString(rawMed)`
                    targetMedecin = UUID.fromString(rawMed);
                // Interception de l'exception IllegalArgumentException e
                } catch (IllegalArgumentException e) {
                    // Affecte à `targetMedecin` `UUID.nameUUIDFromBytes(rawMed.getBytes(java.nio.charset.StandardCharsets.UTF_8))`
                    targetMedecin = UUID.nameUUIDFromBytes(rawMed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }

            // Déclare la variable `t` (Transaction.TypeTransaction) initialisée avec `Transaction.TypeTransaction.valueOf(type)`
            Transaction.TypeTransaction t = Transaction.TypeTransaction.valueOf(type);
            // Déclare la variable `tx` (Transaction) initialisée avec `walletService.payerService(userId, montant, t, serviceId, description, targetMe…`
            Transaction tx = walletService.payerService(userId, montant, t, serviceId, description, targetMedecin);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Paiement effectué avec succès"…
            return ResponseEntity.ok(ApiResponse.success("Paiement effectué avec succès", tx));
        // Interception de l'exception IllegalStateException e
        } catch (IllegalStateException e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()))`
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.status(500).body(ApiResponse.error("Erreur paiement : " + e.getM…`
            return ResponseEntity.status(500).body(ApiResponse.error("Erreur paiement : " + e.getMessage()));
        }
    }

    // Route HTTP POST sur le chemin « /beneficiaire/inviter »
    @PostMapping("/beneficiaire/inviter")
    // Méthode `inviterBeneficiaire` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Beneficiaire ; intention : invite (inviter beneficiaire)
    public ResponseEntity<ApiResponse<Beneficiaire>> inviterBeneficiaire(
            // Paramètre `tuteurUserId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID tuteurUserId,
            // Paramètre `beneficiaireUserId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID beneficiaireUserId,
            // Paramètre `lien` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(defaultValue = "ENFANT") String lien,
            // Paramètre `plafond` de type montant décimal précis — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) BigDecimal plafond) {
        // Déclare la variable `l` (Beneficiaire.LienParente)
        Beneficiaire.LienParente l;
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `clean` (chaîne de caractères) initialisée avec `lien.toUpperCase().trim()`
            String clean = lien.toUpperCase().trim();
            // Condition : exécute le bloc suivant seulement si `clean.contains("CONJOINT")`
            if (clean.contains("CONJOINT")) {
                // Affecte à `l` la valeur de Beneficiaire.LienParente.CONJOINT
                l = Beneficiaire.LienParente.CONJOINT;
            // Sinon, si la condition `clean.contains("PARENT")` est vraie
            } else if (clean.contains("PARENT")) {
                // Affecte à `l` la valeur de Beneficiaire.LienParente.PARENT
                l = Beneficiaire.LienParente.PARENT;
            // Sinon, si la condition `clean.contains("ENFANT")` est vraie
            } else if (clean.contains("ENFANT")) {
                // Affecte à `l` la valeur de Beneficiaire.LienParente.ENFANT
                l = Beneficiaire.LienParente.ENFANT;
            // Sinon (cas contraire de la condition précédente)
            } else {
                // Affecte à `l` la valeur de Beneficiaire.LienParente.AUTRE
                l = Beneficiaire.LienParente.AUTRE;
            }
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Affecte à `l` la valeur de Beneficiaire.LienParente.AUTRE
            l = Beneficiaire.LienParente.AUTRE;
        }
        // Déclare la variable `b` (Beneficiaire) initialisée avec `walletService.inviterBeneficiaire(tuteurUserId, beneficiaireUserId, l, plafond)`
        Beneficiaire b = walletService.inviterBeneficiaire(tuteurUserId, beneficiaireUserId, l, plafond);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Invitation envoyée au bénéfici…
        return ResponseEntity.ok(ApiResponse.success("Invitation envoyée au bénéficiaire", b));
    }

    // Route HTTP GET sur le chemin « /beneficiaire/invitations/{userId} »
    @GetMapping("/beneficiaire/invitations/{userId}")
    // Méthode `getInvitations` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Beneficiaire ; intention : récupère (get invitations)
    public ResponseEntity<ApiResponse<List<Beneficiaire>>> getInvitations(@PathVariable UUID userId) {
        // Déclare la variable `list` (liste de Beneficiaire) initialisée avec `walletService.getInvitationsEnAttente(userId)`
        List<Beneficiaire> list = walletService.getInvitationsEnAttente(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Invitations en attente", list))
        return ResponseEntity.ok(ApiResponse.success("Invitations en attente", list));
    }

    // Route HTTP POST sur le chemin « /beneficiaire/{beneficiaireId}/repondre »
    @PostMapping("/beneficiaire/{beneficiaireId}/repondre")
    // Méthode `repondreInvitation` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Beneficiaire ; intention : répond à (repondre invitation)
    public ResponseEntity<ApiResponse<Beneficiaire>> repondreInvitation(
            // Paramètre `beneficiaireId` de type identifiant UUID — valeur extraite du chemin de l'url
            @PathVariable UUID beneficiaireId,
            // Paramètre `action` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam String action) {
        // Déclare la variable `b` (Beneficiaire) initialisée avec `walletService.repondreInvitation(beneficiaireId, action)`
        Beneficiaire b = walletService.repondreInvitation(beneficiaireId, action);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Réponse enregistrée", b))
        return ResponseEntity.ok(ApiResponse.success("Réponse enregistrée", b));
    }

    // Route HTTP GET sur le chemin « /beneficiaire/mes-beneficiaires/{tuteurUserId} »
    @GetMapping("/beneficiaire/mes-beneficiaires/{tuteurUserId}")
    // Méthode `getMesBeneficiaires` (publique) — paramètres : `tuteurUserId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Beneficiaire ; intention : récupère (get mes beneficiaires)
    public ResponseEntity<ApiResponse<List<Beneficiaire>>> getMesBeneficiaires(@PathVariable UUID tuteurUserId) {
        // Déclare la variable `list` (liste de Beneficiaire) initialisée avec `walletService.getMesBeneficiaires(tuteurUserId)`
        List<Beneficiaire> list = walletService.getMesBeneficiaires(tuteurUserId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Mes bénéficiaires", list))
        return ResponseEntity.ok(ApiResponse.success("Mes bénéficiaires", list));
    }

    // Route HTTP POST sur le chemin « /payer-pour-proche »
    @PostMapping("/payer-pour-proche")
    // Méthode `payerPourProche` (publique) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Transaction ; intention : règle (payer pour proche)
    public ResponseEntity<ApiResponse<Transaction>> payerPourProche(
            // Paramètre `tuteurUserId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID tuteurUserId,
            // Paramètre `beneficiaireUserId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam UUID beneficiaireUserId,
            // Paramètre `montant` de type montant décimal précis — paramètre de requête http (?clé=valeur)
            @RequestParam BigDecimal montant,
            // Paramètre `serviceId` de type identifiant UUID — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) UUID serviceId,
            // Paramètre `description` de type chaîne de caractères — paramètre de requête http (?clé=valeur)
            @RequestParam(required = false) String description) {
        // Déclare la variable `tx` (Transaction) initialisée avec `walletService.payerPourProche(tuteurUserId, beneficiaireUserId, montant, servic…`
        Transaction tx = walletService.payerPourProche(tuteurUserId, beneficiaireUserId, montant, serviceId, description);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Prise en charge proche effectu…
        return ResponseEntity.ok(ApiResponse.success("Prise en charge proche effectuée avec succès", tx));
    }

    // Route HTTP GET sur le chemin « /user/{userId}/historique »
    @GetMapping("/user/{userId}/historique")
    // Méthode `getHistorique` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Transaction ; intention : récupère (get historique)
    public ResponseEntity<ApiResponse<List<Transaction>>> getHistorique(@PathVariable UUID userId) {
        // Déclare la variable `list` (liste de Transaction) initialisée avec `walletService.getHistorique(userId)`
        List<Transaction> list = walletService.getHistorique(userId);
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Historique des transactions", …
        return ResponseEntity.ok(ApiResponse.success("Historique des transactions", list));
    }

    // Route HTTP GET sur le chemin « /admin/all-wallets »
    @GetMapping("/admin/all-wallets")
    // Méthode `getAllWallets` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Portefeuille ; intention : récupère (get all wallets)
    public ResponseEntity<ApiResponse<List<Portefeuille>>> getAllWallets() {
        // Déclare la variable `list` (liste de Portefeuille) initialisée avec la valeur de l'attribut AllPortefeuilles de walletService
        List<Portefeuille> list = walletService.getAllPortefeuilles();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Tous les portefeuilles", list))
        return ResponseEntity.ok(ApiResponse.success("Tous les portefeuilles", list));
    }

    // Route HTTP GET sur le chemin « /admin/all-transactions »
    @GetMapping("/admin/all-transactions")
    // Méthode `getAllTransactions` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de liste de Transaction ; intention : récupère (get all transactions)
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        // Déclare la variable `list` (liste de Transaction) initialisée avec la valeur de l'attribut AllTransactions de walletService
        List<Transaction> list = walletService.getAllTransactions();
        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Toutes les transactions", list…
        return ResponseEntity.ok(ApiResponse.success("Toutes les transactions", list));
    }

    // Route HTTP GET sur le chemin « /admin/stats »
    @GetMapping("/admin/stats")
    // Méthode `getWalletStats` (publique) — sans paramètre ; retourne : réponse HTTP contenant enveloppe ApiResponse de java.util.Map<String, Object> ; intention : récupère (get wallet stats)
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getWalletStats() {
        // Déclare la variable `wallets` (liste de Portefeuille) initialisée avec la valeur de l'attribut AllPortefeuilles de walletService
        List<Portefeuille> wallets = walletService.getAllPortefeuilles();
        // Déclare la variable `txs` (liste de Transaction) initialisée avec la valeur de l'attribut AllTransactions de walletService
        List<Transaction> txs = walletService.getAllTransactions();

        // Déclare la variable `soldeTotal` (montant décimal précis) initialisée avec `wallets.stream()`
        BigDecimal soldeTotal = wallets.stream()
                // Enchaînement : appelle `map(p -> p.getSolde() != null ? p.getSolde() : BigDec…`
                .map(p -> p.getSolde() != null ? p.getSolde() : BigDecimal.ZERO)
                // Enchaînement : appelle `reduce(BigDecimal.ZERO, BigDecimal::add);`
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Déclare la variable `volumeTotal` (montant décimal précis) initialisée avec `txs.stream()`
        BigDecimal volumeTotal = txs.stream()
                // Enchaînement : appelle `filter(t -> t.getStatut() == Transaction.StatutTransacti…`
                .filter(t -> t.getStatut() == Transaction.StatutTransaction.VALIDE)
                // Enchaînement : appelle `map(t -> t.getMontant() != null ? t.getMontant() : Bi…`
                .map(t -> t.getMontant() != null ? t.getMontant() : BigDecimal.ZERO)
                // Enchaînement : appelle `reduce(BigDecimal.ZERO, BigDecimal::add);`
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Déclare la variable `nbTransactions` (entier long) initialisée avec `txs.size()`
        long nbTransactions = txs.size();
        // Déclare la variable `nbWallets` (entier long) initialisée avec `wallets.size()`
        long nbWallets = wallets.size();

        // Instruction : java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        // Appelle la méthode `put` sur `stats` : stats.put("totalWallets", nbWallets);
        stats.put("totalWallets", nbWallets);
        // Appelle la méthode `put` sur `stats` : stats.put("soldeTotalPlateforme", soldeTotal);
        stats.put("soldeTotalPlateforme", soldeTotal);
        // Appelle la méthode `put` sur `stats` : stats.put("volumeTotalTransactions", volumeTotal);
        stats.put("volumeTotalTransactions", volumeTotal);
        // Appelle la méthode `put` sur `stats` : stats.put("nombreTransactions", nbTransactions);
        stats.put("nombreTransactions", nbTransactions);

        // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Statistiques financières", sta…
        return ResponseEntity.ok(ApiResponse.success("Statistiques financières", stats));
    }

    // Route HTTP POST sur le chemin « /admin/ajuster »
    @PostMapping("/admin/ajuster")
    // Méthode `ajusterPortefeuille` (publique) — paramètres : `payload` (java.util.Map<String, Object>) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Transaction ; intention : ajuste (ajuster portefeuille)
    public ResponseEntity<ApiResponse<Transaction>> ajusterPortefeuille(@RequestBody java.util.Map<String, Object> payload) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Déclare la variable `userIdStr` (chaîne de caractères) initialisée avec `(String) payload.get("userId")`
            String userIdStr = (String) payload.get("userId");
            // Déclare la variable `userId` (identifiant UUID) initialisée avec `UUID.fromString(userIdStr)`
            UUID userId = UUID.fromString(userIdStr);
            // Déclare la variable `montant` (montant décimal précis) initialisée avec une nouvelle instance de BigDecimal
            BigDecimal montant = new BigDecimal(payload.get("montant").toString());
            // Déclare la variable `type` (chaîne de caractères) initialisée avec `(String) payload.getOrDefault("type", "CREDIT")`
            String type = (String) payload.getOrDefault("type", "CREDIT");
            // Déclare la variable `motif` (chaîne de caractères) initialisée avec `(String) payload.getOrDefault("justification", "Ajustement administratif")`
            String motif = (String) payload.getOrDefault("justification", "Ajustement administratif");

            // Déclare la variable `tx` (Transaction) initialisée avec `walletService.ajusterPortefeuille(userId, montant, type, motif)`
            Transaction tx = walletService.ajusterPortefeuille(userId, montant, type, motif);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Ajustement de compte effectué …
            return ResponseEntity.ok(ApiResponse.success("Ajustement de compte effectué avec succès", tx));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Erreur ajustement : " + e.g…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur ajustement : " + e.getMessage()));
        }
    }

    // Route HTTP DELETE sur le chemin « /beneficiaire/{beneficiaireId} »
    @DeleteMapping("/beneficiaire/{beneficiaireId}")
    // Méthode `supprimerBeneficiaire` (publique) — paramètres : `beneficiaireId` (identifiant UUID) ; retourne : réponse HTTP contenant enveloppe ApiResponse de Void ; intention : supprime (supprimer beneficiaire)
    public ResponseEntity<ApiResponse<Void>> supprimerBeneficiaire(@PathVariable UUID beneficiaireId) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Appelle la méthode `supprimerBeneficiaire` sur `walletService` : walletService.supprimerBeneficiaire(beneficiaireId);
            walletService.supprimerBeneficiaire(beneficiaireId);
            // Retourne une réponse HTTP 200 OK : ResponseEntity.ok(ApiResponse.success("Bénéficiaire supprimé avec suc…
            return ResponseEntity.ok(ApiResponse.success("Bénéficiaire supprimé avec succès", null));
        // Interception de l'exception Exception e
        } catch (Exception e) {
            // Retourne `ResponseEntity.badRequest().body(ApiResponse.error("Erreur suppression bénéfici…`
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur suppression bénéficiaire : " + e.getMessage()));
        }
    }
}

