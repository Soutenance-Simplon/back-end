package com.diamyaraam.wallet.service;

import com.diamyaraam.wallet.entity.AuditFinancier;
import com.diamyaraam.wallet.entity.Beneficiaire;
import com.diamyaraam.wallet.entity.Portefeuille;
import com.diamyaraam.wallet.entity.Transaction;
import com.diamyaraam.wallet.repository.AuditFinancierRepository;
import com.diamyaraam.wallet.repository.BeneficiaireRepository;
import com.diamyaraam.wallet.repository.PortefeuilleRepository;
import com.diamyaraam.wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final PortefeuilleRepository portefeuilleRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final TransactionRepository transactionRepository;
    private final AuditFinancierRepository auditRepository;
    private final JdbcTemplate jdbcTemplate;

    public WalletService(
            PortefeuilleRepository portefeuilleRepository,
            BeneficiaireRepository beneficiaireRepository,
            TransactionRepository transactionRepository,
            AuditFinancierRepository auditRepository,
            @Autowired(required = false) JdbcTemplate jdbcTemplate) {
        this.portefeuilleRepository = portefeuilleRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.transactionRepository = transactionRepository;
        this.auditRepository = auditRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // RM131 — Création automatique du portefeuille
    @Transactional
    public Portefeuille getOrCreatePortefeuille(UUID userId) {
        return portefeuilleRepository.findByUserId(userId).orElseGet(() -> {
            Portefeuille p = new Portefeuille();
            p.setUserId(userId);
            p.setSolde(new BigDecimal("50000.00")); // Solde d'accueil / démo (RM131)
            p.setDevise("FCFA");
            p.setStatut(Portefeuille.StatutPortefeuille.ACTIF);
            return portefeuilleRepository.save(p);
        });
    }

    private UUID resolveMedecinUserId(UUID medecinRef) {
        if (medecinRef == null) return null;
        if (jdbcTemplate != null) {
            try {
                List<UUID> list = jdbcTemplate.query(
                        "SELECT user_id FROM medecin_schema.medecin WHERE id = ?",
                        (rs, rowNum) -> rs.getObject("user_id", UUID.class),
                        medecinRef
                );
                if (!list.isEmpty() && list.get(0) != null) {
                    return list.get(0);
                }
            } catch (Exception ignored) {
            }
        }
        return medecinRef;
    }

    // RM134, RM135 — Dépôt de fonds
    @Transactional
    public Transaction deposerFonds(UUID userId, BigDecimal montant, Transaction.MoyenPaiement moyen, String refExterne) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être supérieur à zéro.");
        }

        Portefeuille p = getOrCreatePortefeuille(userId);

        // Validation du dépôt (RM135)
        p.crediter(montant);
        portefeuilleRepository.save(p);

        Transaction tx = new Transaction();
        tx.setPortefeuille(p);
        tx.setTypeTransaction(Transaction.TypeTransaction.DEPOT);
        tx.setMontant(montant);
        tx.setMoyenPaiement(moyen);
        tx.setStatut(Transaction.StatutTransaction.VALIDE);
        tx.setReferenceExterne(refExterne != null ? refExterne : "DEP-" + UUID.randomUUID().toString().substring(0, 8));
        tx.setDescription("Alimentation portefeuille de " + montant + " FCFA via " + moyen.name());
        Transaction savedTx = transactionRepository.save(tx);

        audit(userId, savedTx.getId(), AuditFinancier.ActionFinanciere.DEPOT, montant, true, "Dépôt validé");
        return savedTx;
    }

    @Transactional
    public Transaction payerService(UUID userId, BigDecimal montant, Transaction.TypeTransaction type, UUID serviceId, String description) {
        return payerService(userId, montant, type, serviceId, description, null);
    }

    // RM139, RM140, RM141 — Paiement consultation : Débit patient + Crédit médecin
    @Transactional
    public Transaction payerService(UUID userId, BigDecimal montant, Transaction.TypeTransaction type, UUID serviceId, String description, UUID medecinUserId) {
        Portefeuille pPatient = getOrCreatePortefeuille(userId);

        // RM141 — Vérification du solde suffisant du patient
        if (!pPatient.aSoldeSuffisant(montant)) {
            audit(userId, null, AuditFinancier.ActionFinanciere.ECHEC_SOLDE_INSUFFISANT, montant, false, "Solde insuffisant pour " + type.name());
            throw new IllegalStateException("Solde insuffisant dans votre Portefeuille Santé (" + pPatient.getSolde() + " FCFA disponibles).");
        }

        // 1. Débit du patient
        pPatient.debiter(montant);
        portefeuilleRepository.save(pPatient);

        Transaction txPatient = new Transaction();
        txPatient.setPortefeuille(pPatient);
        txPatient.setTypeTransaction(type);
        txPatient.setMontant(montant);
        txPatient.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
        txPatient.setStatut(Transaction.StatutTransaction.VALIDE);
        txPatient.setServiceId(serviceId);
        txPatient.setDescription(description != null ? description : "Règlement " + type.name());
        Transaction savedTx = transactionRepository.save(txPatient);

        audit(userId, savedTx.getId(), AuditFinancier.ActionFinanciere.PAIEMENT, montant, true, "Paiement réussi pour service " + serviceId);

        // 2. Crédit automatique du médecin
        if (medecinUserId != null) {
            UUID resolvedMedecinUserId = resolveMedecinUserId(medecinUserId);
            if (resolvedMedecinUserId != null) {
                Portefeuille pMedecin = getOrCreatePortefeuille(resolvedMedecinUserId);
                pMedecin.crediter(montant);
                portefeuilleRepository.save(pMedecin);

                Transaction txMedecin = new Transaction();
                txMedecin.setPortefeuille(pMedecin);
                txMedecin.setTypeTransaction(Transaction.TypeTransaction.HONORAIRES_CONSULTATION);
                txMedecin.setMontant(montant);
                txMedecin.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
                txMedecin.setStatut(Transaction.StatutTransaction.VALIDE);
                txMedecin.setServiceId(serviceId);
                txMedecin.setDescription("Honoraires reçus" + (description != null ? " (" + description + ")" : ""));
                transactionRepository.save(txMedecin);

                audit(resolvedMedecinUserId, txMedecin.getId(), AuditFinancier.ActionFinanciere.DEPOT, montant, true, "Honoraires crédités suite à consultation " + serviceId);
            }
        }

        return savedTx;
    }

    // RM136, RM137 — Inviter un bénéficiaire familial
    @Transactional
    public Beneficiaire inviterBeneficiaire(UUID tuteurUserId, UUID beneficiaireUserId, Beneficiaire.LienParente lien, BigDecimal plafond) {
        Portefeuille p = getOrCreatePortefeuille(tuteurUserId);

        Beneficiaire b = beneficiaireRepository
                .findByPortefeuilleIdAndBeneficiaireUserId(p.getId(), beneficiaireUserId)
                .orElseGet(() -> {
                    Beneficiaire newB = new Beneficiaire();
                    newB.setPortefeuille(p);
                    newB.setBeneficiaireUserId(beneficiaireUserId);
                    return newB;
                });

        b.setLienParente(lien);
        b.setPlafondMensuel(plafond);
        b.setStatut(Beneficiaire.StatutBeneficiaire.EN_ATTENTE);

        return beneficiaireRepository.save(b);
    }

    @Transactional(readOnly = true)
    public List<Beneficiaire> getInvitationsEnAttente(UUID beneficiaireUserId) {
        List<Beneficiaire> list = beneficiaireRepository.findByBeneficiaireUserIdAndStatut(
                beneficiaireUserId, Beneficiaire.StatutBeneficiaire.EN_ATTENTE);
        list.forEach(b -> {
            if (b.getPortefeuille() != null) {
                b.getPortefeuille().getUserId();
            }
        });
        return list;
    }

    @Transactional
    public Beneficiaire repondreInvitation(UUID beneficiaireId, String action) {
        Beneficiaire b = beneficiaireRepository.findById(beneficiaireId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation introuvable"));
        if ("ACCEPTER".equalsIgnoreCase(action)) {
            b.setStatut(Beneficiaire.StatutBeneficiaire.ACTIF);
        } else {
            b.setStatut(Beneficiaire.StatutBeneficiaire.REJETE);
        }
        Beneficiaire saved = beneficiaireRepository.save(b);
        if (saved.getPortefeuille() != null) {
            saved.getPortefeuille().getUserId();
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Beneficiaire> getMesBeneficiaires(UUID tuteurUserId) {
        Portefeuille p = getOrCreatePortefeuille(tuteurUserId);
        List<Beneficiaire> list = beneficiaireRepository.findByPortefeuilleId(p.getId());
        list.forEach(b -> {
            if (b.getPortefeuille() != null) {
                b.getPortefeuille().getUserId();
            }
        });
        return list;
    }

    // RM138 — Prise en charge financière d'un proche par le tuteur
    @Transactional
    public Transaction payerPourProche(UUID tuteurUserId, UUID beneficiaireUserId, BigDecimal montant, UUID serviceId, String description) {
        Portefeuille tuteurPortefeuille = getOrCreatePortefeuille(tuteurUserId);

        Beneficiaire b = beneficiaireRepository
                .findByPortefeuilleIdAndBeneficiaireUserId(tuteurPortefeuille.getId(), beneficiaireUserId)
                .orElseThrow(() -> new IllegalArgumentException("Ce bénéficiaire n'est pas associé à votre portefeuille familial."));

        if (!b.isAutorise()) {
            throw new IllegalStateException("L'autorisation de prise en charge pour ce bénéficiaire est actuellement suspendue.");
        }

        // RM141 — Vérification du solde du tuteur
        if (!tuteurPortefeuille.aSoldeSuffisant(montant)) {
            audit(tuteurUserId, null, AuditFinancier.ActionFinanciere.ECHEC_SOLDE_INSUFFISANT, montant, false, "Solde tuteur insuffisant pour proche " + beneficiaireUserId);
            throw new IllegalStateException("Le portefeuille du tuteur dispose d'un solde insuffisant.");
        }

        tuteurPortefeuille.debiter(montant);
        portefeuilleRepository.save(tuteurPortefeuille);

        Transaction tx = new Transaction();
        tx.setPortefeuille(tuteurPortefeuille);
        tx.setTypeTransaction(Transaction.TypeTransaction.PAIEMENT_FAMILIAL);
        tx.setMontant(montant);
        tx.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
        tx.setStatut(Transaction.StatutTransaction.VALIDE);
        tx.setBeneficiaireUserId(beneficiaireUserId);
        tx.setServiceId(serviceId);
        tx.setDescription(description != null ? description : "Prise en charge soin de santé pour proche");
        Transaction savedTx = transactionRepository.save(tx);

        audit(tuteurUserId, savedTx.getId(), AuditFinancier.ActionFinanciere.PAIEMENT, montant, true, "Paiement pour proche " + beneficiaireUserId);
        return savedTx;
    }

    // RM142 — Historique des transactions
    public List<Transaction> getHistorique(UUID userId) {
        Portefeuille p = getOrCreatePortefeuille(userId);
        return transactionRepository.findByPortefeuilleIdOrderByDateTransactionDesc(p.getId());
    }

    // RM145 — Traçabilité et audit financier
    private void audit(UUID userId, UUID transactionId, AuditFinancier.ActionFinanciere action, BigDecimal montant, boolean success, String details) {
        AuditFinancier item = new AuditFinancier();
        item.setUserId(userId);
        item.setTransactionId(transactionId);
        item.setAction(action);
        item.setMontant(montant);
        item.setSuccess(success);
        item.setDetails(details);
        auditRepository.save(item);
    }
}
