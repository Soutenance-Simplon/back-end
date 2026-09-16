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
}
