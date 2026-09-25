package com.diamyaraam.wallet.controller;

import com.diamyaraam.shared.dto.ApiResponse;
import com.diamyaraam.wallet.entity.Beneficiaire;
import com.diamyaraam.wallet.entity.Portefeuille;
import com.diamyaraam.wallet.entity.Transaction;
import com.diamyaraam.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/wallet", "/portefeuilles"})
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Portefeuille>> getPortefeuille(@PathVariable UUID userId) {
        Portefeuille p = walletService.getOrCreatePortefeuille(userId);
        return ResponseEntity.ok(ApiResponse.success("Portefeuille Santé", p));
    }

    @PostMapping("/deposer")
    public ResponseEntity<ApiResponse<Transaction>> deposer(
            @RequestParam UUID userId,
            @RequestParam BigDecimal montant,
            @RequestParam(defaultValue = "MOBILE_MONEY_ORANGE") String moyen,
            @RequestParam(required = false) String reference) {
        Transaction.MoyenPaiement m = Transaction.MoyenPaiement.valueOf(moyen);
        Transaction tx = walletService.deposerFonds(userId, montant, m, reference);
        return ResponseEntity.ok(ApiResponse.success("Dépôt effectué avec succès", tx));
    }

    @PostMapping("/payer-service")
    public ResponseEntity<ApiResponse<Transaction>> payerService(
            @RequestParam UUID userId,
            @RequestParam BigDecimal montant,
            @RequestParam(defaultValue = "PAIEMENT_TELECONSULTATION") String type,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String medecinUserId,
            @RequestParam(required = false) String medecinId) {
        try {
            UUID targetMedecin = null;
            String rawMed = (medecinUserId != null && !medecinUserId.trim().isEmpty()) ? medecinUserId.trim() : (medecinId != null ? medecinId.trim() : null);
            if (rawMed != null && !rawMed.isEmpty()) {
                try {
                    targetMedecin = UUID.fromString(rawMed);
                } catch (IllegalArgumentException e) {
                    targetMedecin = UUID.nameUUIDFromBytes(rawMed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }

            Transaction.TypeTransaction t = Transaction.TypeTransaction.valueOf(type);
            Transaction tx = walletService.payerService(userId, montant, t, serviceId, description, targetMedecin);
            return ResponseEntity.ok(ApiResponse.success("Paiement effectué avec succès", tx));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Erreur paiement : " + e.getMessage()));
        }
    }

    @PostMapping("/beneficiaire/inviter")
    public ResponseEntity<ApiResponse<Beneficiaire>> inviterBeneficiaire(
            @RequestParam UUID tuteurUserId,
            @RequestParam UUID beneficiaireUserId,
            @RequestParam(defaultValue = "ENFANT") String lien,
            @RequestParam(required = false) BigDecimal plafond) {
        Beneficiaire.LienParente l;
        try {
            String clean = lien.toUpperCase().trim();
            if (clean.contains("CONJOINT")) {
                l = Beneficiaire.LienParente.CONJOINT;
            } else if (clean.contains("PARENT")) {
                l = Beneficiaire.LienParente.PARENT;
            } else if (clean.contains("ENFANT")) {
                l = Beneficiaire.LienParente.ENFANT;
            } else {
                l = Beneficiaire.LienParente.AUTRE;
            }
        } catch (Exception e) {
            l = Beneficiaire.LienParente.AUTRE;
        }
        Beneficiaire b = walletService.inviterBeneficiaire(tuteurUserId, beneficiaireUserId, l, plafond);
        return ResponseEntity.ok(ApiResponse.success("Invitation envoyée au bénéficiaire", b));
    }

    @GetMapping("/beneficiaire/invitations/{userId}")
    public ResponseEntity<ApiResponse<List<Beneficiaire>>> getInvitations(@PathVariable UUID userId) {
        List<Beneficiaire> list = walletService.getInvitationsEnAttente(userId);
        return ResponseEntity.ok(ApiResponse.success("Invitations en attente", list));
    }

    @PostMapping("/beneficiaire/{beneficiaireId}/repondre")
    public ResponseEntity<ApiResponse<Beneficiaire>> repondreInvitation(
            @PathVariable UUID beneficiaireId,
            @RequestParam String action) {
        Beneficiaire b = walletService.repondreInvitation(beneficiaireId, action);
        return ResponseEntity.ok(ApiResponse.success("Réponse enregistrée", b));
    }

    @GetMapping("/beneficiaire/mes-beneficiaires/{tuteurUserId}")
    public ResponseEntity<ApiResponse<List<Beneficiaire>>> getMesBeneficiaires(@PathVariable UUID tuteurUserId) {
        List<Beneficiaire> list = walletService.getMesBeneficiaires(tuteurUserId);
        return ResponseEntity.ok(ApiResponse.success("Mes bénéficiaires", list));
    }

    @PostMapping("/payer-pour-proche")
    public ResponseEntity<ApiResponse<Transaction>> payerPourProche(
            @RequestParam UUID tuteurUserId,
            @RequestParam UUID beneficiaireUserId,
            @RequestParam BigDecimal montant,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) String description) {
        Transaction tx = walletService.payerPourProche(tuteurUserId, beneficiaireUserId, montant, serviceId, description);
        return ResponseEntity.ok(ApiResponse.success("Prise en charge proche effectuée avec succès", tx));
    }

    @GetMapping("/user/{userId}/historique")
    public ResponseEntity<ApiResponse<List<Transaction>>> getHistorique(@PathVariable UUID userId) {
        List<Transaction> list = walletService.getHistorique(userId);
        return ResponseEntity.ok(ApiResponse.success("Historique des transactions", list));
    }

    @GetMapping("/admin/all-wallets")
    public ResponseEntity<ApiResponse<List<Portefeuille>>> getAllWallets() {
        List<Portefeuille> list = walletService.getAllPortefeuilles();
        return ResponseEntity.ok(ApiResponse.success("Tous les portefeuilles", list));
    }

    @GetMapping("/admin/all-transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        List<Transaction> list = walletService.getAllTransactions();
        return ResponseEntity.ok(ApiResponse.success("Toutes les transactions", list));
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getWalletStats() {
        List<Portefeuille> wallets = walletService.getAllPortefeuilles();
        List<Transaction> txs = walletService.getAllTransactions();

        BigDecimal soldeTotal = wallets.stream()
                .map(p -> p.getSolde() != null ? p.getSolde() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal volumeTotal = txs.stream()
                .filter(t -> t.getStatut() == Transaction.StatutTransaction.VALIDE)
                .map(t -> t.getMontant() != null ? t.getMontant() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long nbTransactions = txs.size();
        long nbWallets = wallets.size();

        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalWallets", nbWallets);
        stats.put("soldeTotalPlateforme", soldeTotal);
        stats.put("volumeTotalTransactions", volumeTotal);
        stats.put("nombreTransactions", nbTransactions);

        return ResponseEntity.ok(ApiResponse.success("Statistiques financières", stats));
    }

    @PostMapping("/admin/ajuster")
    public ResponseEntity<ApiResponse<Transaction>> ajusterPortefeuille(@RequestBody java.util.Map<String, Object> payload) {
        try {
            String userIdStr = (String) payload.get("userId");
            UUID userId = UUID.fromString(userIdStr);
            BigDecimal montant = new BigDecimal(payload.get("montant").toString());
            String type = (String) payload.getOrDefault("type", "CREDIT");
            String motif = (String) payload.getOrDefault("justification", "Ajustement administratif");

            Transaction tx = walletService.ajusterPortefeuille(userId, montant, type, motif);
            return ResponseEntity.ok(ApiResponse.success("Ajustement de compte effectué avec succès", tx));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur ajustement : " + e.getMessage()));
        }
    }

    @DeleteMapping("/beneficiaire/{beneficiaireId}")
    public ResponseEntity<ApiResponse<Void>> supprimerBeneficiaire(@PathVariable UUID beneficiaireId) {
        try {
            walletService.supprimerBeneficiaire(beneficiaireId);
            return ResponseEntity.ok(ApiResponse.success("Bénéficiaire supprimé avec succès", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Erreur suppression bénéficiaire : " + e.getMessage()));
        }
    }
}

