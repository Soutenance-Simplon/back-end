package com.diamyaraam.wallet.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ENTITÉ : Transaction — Historique immuable des opérations financières (RM142)
 *
 * RM142 — Historique complet des transactions (ne peut pas être supprimé).
 */
@Entity
@Table(name = "transaction_financiere", schema = "wallet_schema")
public class Transaction {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portefeuille_id", nullable = false)
    private Portefeuille portefeuille;

    public enum TypeTransaction {
        DEPOT,                  // Alimentation par Mobile Money, CB, etc. (RM134)
        PAIEMENT_TELECONSULTATION, // Règlement consultation (RM139)
        PAIEMENT_PARTENAIRE,    // Règlement établissement partenaire (RM140)
        PAIEMENT_FAMILIAL,      // Prise en charge d'un proche (RM138)
        REMBOURSEMENT,          // Remboursement en cas d'annulation
        HONORAIRES_CONSULTATION // Crédit honoraires reçus par le médecin (RM139)
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type_transaction", length = 35, nullable = false)
    private TypeTransaction typeTransaction;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    public enum MoyenPaiement {
        MOBILE_MONEY_ORANGE,
        MOBILE_MONEY_WAVE,
        CARTE_BANCAIRE,
        VIREMENT,
        SOLDE_PORTEFEUILLE
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "moyen_paiement", length = 30)
    private MoyenPaiement moyenPaiement;

    public enum StatutTransaction {
        EN_ATTENTE,   // En attente de confirmation du prestataire (RM135)
        VALIDE,       // Confirmé et comptabilisé
        ECHOUE,       // Échec ou solde insuffisant (RM141)
        ANNULE        // Annulé par l'utilisateur ou le prestataire
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 15, nullable = false)
    private StatutTransaction statut = StatutTransaction.EN_ATTENTE;

    @Column(name = "reference_externe", length = 100)
    private String referenceExterne;   // Référence Orange Money / Wave / CB

    @Column(name = "beneficiaire_user_id")
    private UUID beneficiaireUserId;   // Proche bénéficiaire du soin

    @Column(name = "service_id")
    private UUID serviceId;            // ID du RDV ou de la consultation

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "date_transaction", updatable = false)
    private LocalDateTime dateTransaction;

    public Transaction() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Portefeuille getPortefeuille() { return portefeuille; }
    public void setPortefeuille(Portefeuille portefeuille) { this.portefeuille = portefeuille; }

    public TypeTransaction getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(TypeTransaction typeTransaction) { this.typeTransaction = typeTransaction; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public MoyenPaiement getMoyenPaiement() { return moyenPaiement; }
    public void setMoyenPaiement(MoyenPaiement moyenPaiement) { this.moyenPaiement = moyenPaiement; }

    public StatutTransaction getStatut() { return statut; }
    public void setStatut(StatutTransaction statut) { this.statut = statut; }

    public String getReferenceExterne() { return referenceExterne; }
    public void setReferenceExterne(String referenceExterne) { this.referenceExterne = referenceExterne; }

    public UUID getBeneficiaireUserId() { return beneficiaireUserId; }
    public void setBeneficiaireUserId(UUID beneficiaireUserId) { this.beneficiaireUserId = beneficiaireUserId; }

    public UUID getServiceId() { return serviceId; }
    public void setServiceId(UUID serviceId) { this.serviceId = serviceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }
}
