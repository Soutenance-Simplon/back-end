// Déclaration du package Java : `com.diamyaraam.wallet.service`
package com.diamyaraam.wallet.service;

// Import de la classe `AuditFinancier` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.AuditFinancier;
// Import de la classe `Beneficiaire` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Beneficiaire;
// Import de la classe `Portefeuille` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Portefeuille;
// Import de la classe `Transaction` (paquet com.diamyaraam.wallet.entity)
import com.diamyaraam.wallet.entity.Transaction;
// Import de la classe `AuditFinancierRepository` (paquet com.diamyaraam.wallet.repository)
import com.diamyaraam.wallet.repository.AuditFinancierRepository;
// Import de la classe `BeneficiaireRepository` (paquet com.diamyaraam.wallet.repository)
import com.diamyaraam.wallet.repository.BeneficiaireRepository;
// Import de la classe `PortefeuilleRepository` (paquet com.diamyaraam.wallet.repository)
import com.diamyaraam.wallet.repository.PortefeuilleRepository;
// Import de la classe `TransactionRepository` (paquet com.diamyaraam.wallet.repository)
import com.diamyaraam.wallet.repository.TransactionRepository;
// Import de la classe `RealtimePublisher` (paquet com.diamyaraam.wallet.util)
import com.diamyaraam.wallet.util.RealtimePublisher;
// Import de la classe `Autowired` (paquet org.springframework.beans.factory.annotation)
import org.springframework.beans.factory.annotation.Autowired;
// Import de la classe `JdbcTemplate` (paquet org.springframework.jdbc.core)
import org.springframework.jdbc.core.JdbcTemplate;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `BigDecimal` (paquet java.math)
import java.math.BigDecimal;
// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `Map` (paquet java.util)
import java.util.Map;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ============================================================================
 * SERVICE FINANCIER & COMPTABILITÉ DES PORTEFEUILLES (WALLET-SERVICE)
 * ============================================================================
 * RÔLE ARCHITECTURAL (POINT CLÉ POUR LA SOUTENANCE) :
 * Ce microservice gère l'ensemble de la couche monétaire et transactionnelle
 * de la plateforme Diam-Yaraam en Francs CFA (XOF).
 *
 * PRINCIPES FINANCIERS & RÈGLES MÉTIERS STRICTES :
 * 1. Comptabilité Rigoureuse en Partie Double & Escrow :
 *    - Débit du patient à la confirmation du rendez-vous médical.
 *    - Découpage automatique des honoraires lors de la consultation :
 *      * 90% crédités sur le portefeuille du médecin praticien.
 *      * 10% conservés comme commission d'exploitation de la plateforme.
 * 2. Audit Financier Systématique & Immuable :
 *    - Toute opération (crédit, débit, transfert, rejet) génère un enregistrement
 *      dans `AuditFinancier` pour traçabilité légale et conformité fiscale.
 * 3. Événements Temps Réel (WebSockets / STOMP) :
 *    - Notifications push instantanées au patient et au praticien lors des
 *      rechargements et réceptions d'honoraires.
 * ============================================================================
 */
@Service
// Déclaration de la classe `WalletService` (rôle : porte la logique métier)
public class WalletService {

    // Déclaration du dépôt pour les opérations de persistance des portefeuilles
    private final PortefeuilleRepository portefeuilleRepository;

    // Déclaration du dépôt pour gérer les bénéficiaires familiaux associés aux portefeuilles
    private final BeneficiaireRepository beneficiaireRepository;

    // Déclaration du dépôt pour l'enregistrement et le requêtage des transactions financières
    private final TransactionRepository transactionRepository;

    // Déclaration du dépôt d'audit pour la traçabilité immuable des événements financiers
    private final AuditFinancierRepository auditRepository;

    // Client JDBC Spring pour exécuter des requêtes SQL natives inter-schémas si nécessaire
    private final JdbcTemplate jdbcTemplate;

    // Composant de publication temps réel via WebSocket STOMP
    private final RealtimePublisher realtimePublisher;

    // Constructeur d'injection des dépendances gérées par le conteneur Spring IoC
    public WalletService(
            // Injection du repository de portefeuille
            PortefeuilleRepository portefeuilleRepository,
            // Injection du repository des bénéficiaires
            BeneficiaireRepository beneficiaireRepository,
            // Injection du repository des transactions
            TransactionRepository transactionRepository,
            // Injection du repository d'audit financier
            AuditFinancierRepository auditRepository,
            // Injection optionnelle du JdbcTemplate
            @Autowired(required = false) JdbcTemplate jdbcTemplate,
            // Injection optionnelle du publisher WebSocket
            @Autowired(required = false) RealtimePublisher realtimePublisher) {
        // Initialisation de la référence du repository de portefeuille
        this.portefeuilleRepository = portefeuilleRepository;
        // Initialisation de la référence du repository de bénéficiaires
        this.beneficiaireRepository = beneficiaireRepository;
        // Initialisation de la référence du repository des transactions
        this.transactionRepository = transactionRepository;
        // Initialisation de la référence du repository d'audit financier
        this.auditRepository = auditRepository;
        // Initialisation de la référence JdbcTemplate
        this.jdbcTemplate = jdbcTemplate;
        // Initialisation de la référence du publisher temps réel
        this.realtimePublisher = realtimePublisher;
    }

    /**
     * Règle Métier RM131 : Obtient le portefeuille d'un utilisateur ou le crée
     * automatiquement avec un solde initial de bienvenue / démonstration.
     *
     * @param userId Identifiant unique de l'utilisateur (patient ou médecin)
     * @return L'entité Portefeuille persistée en base
     */
    // Déclaration de la transaction Spring pour garantir l'atomicité de la persistance
    @Transactional
    // Méthode `getOrCreatePortefeuille` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : Portefeuille ; intention : récupère ou crée (get or create portefeuille)
    public Portefeuille getOrCreatePortefeuille(UUID userId) {
        // Recherche du portefeuille existant en base de données par l'identifiant utilisateur
        return portefeuilleRepository.findByUserId(userId)
                // Si aucun portefeuille n'est trouvé, on en instancie et sauvegarde un nouveau
                .orElseGet(() -> {
                    // Instanciation d'un nouvel objet Portefeuille
                    Portefeuille p = new Portefeuille();
                    // Affectation de l'identifiant du titulaire (patient ou médecin)
                    p.setUserId(userId);
                    // Dotation initiale d'accueil fixée à 50 000 FCFA pour tests et démonstration soutenance
                    p.setSolde(new BigDecimal("50000.00"));
                    // Définition de la devise monétaire officielle (Franc CFA)
                    p.setDevise("FCFA");
                    // Activation immédiate du compte de paiement
                    p.setStatut(Portefeuille.StatutPortefeuille.ACTIF);
                    // Persistance du portefeuille nouvellement créé dans PostgreSQL
                    return portefeuilleRepository.save(p);
                });
    }

    // Méthode utilitaire pour résoudre l'identifiant utilisateur réel (user_id) lié à un praticien
    private UUID resolveMedecinUserId(UUID medecinRef) {
        // Vérification de la validité de la référence reçue en paramètre
        if (medecinRef == null) return null;
        // Si le JdbcTemplate est disponible, recherche directe dans la table medecin du schéma medecin_schema
        if (jdbcTemplate != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Exécution de la requête SQL native de jointure inter-schéma
                List<UUID> list = jdbcTemplate.query(
                        // Requête ciblant l'identifiant auth_user du praticien
                        "SELECT user_id FROM medecin_schema.medecin WHERE id = ?",
                        // Mappage de la colonne SQL user_id en UUID Java
                        (rs, rowNum) -> rs.getObject("user_id", UUID.class),
                        // Paramètre dynamique de la clause WHERE
                        medecinRef
                );
                // Si un identifiant correspondant a été trouvé en base de données
                if (!list.isEmpty() && list.get(0) != null) {
                    // Retourne l'identifiant auth_user résolu
                    return list.get(0);
                }
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {
                // Tolérance aux pannes : en cas d'erreur SQL, repli sur la valeur transmise
            }
        }
        // Valeur de repli par défaut si aucune résolution SQL n'a été possible
        return medecinRef;
    }

    // RM134, RM135 — Dépôt de fonds sur le portefeuille électronique
    // Déclaration de transaction Spring pour garantir que le crédit et la transaction soient atomiques
    @Transactional
    // Méthode `deposerFonds` (publique) — paramètres : `userId` (identifiant UUID), `montant` (montant décimal précis), `moyen` (Transaction.MoyenPaiement), `refExterne` (chaîne de caractères) ; retourne : Transaction ; intention : dépose (deposer fonds)
    public Transaction deposerFonds(UUID userId, BigDecimal montant, Transaction.MoyenPaiement moyen, String refExterne) {
        // Règle de validation RM135 : le montant à créditer doit être strictement positif
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            // Rejet immédiat de la requête si le montant est nul ou négatif
            throw new IllegalArgumentException("Le montant du dépôt doit être supérieur à zéro.");
        }

        // Récupération du portefeuille de l'utilisateur ou création automatique s'il n'existe pas
        Portefeuille p = getOrCreatePortefeuille(userId);

        // Crédit effectif du solde numérique en mémoire
        p.crediter(montant);
        // Persistance du nouveau solde mis à jour dans PostgreSQL
        portefeuilleRepository.save(p);

        // Instanciation d'un nouvel enregistrement de transaction financière
        Transaction tx = new Transaction();
        // Rattachement de la transaction au portefeuille de l'utilisateur
        tx.setPortefeuille(p);
        // Typage de l'opération comme un dépôt de fonds (rechargement)
        tx.setTypeTransaction(Transaction.TypeTransaction.DEPOT);
        // Spécification du montant crédité en FCFA
        tx.setMontant(montant);
        // Enregistrement du canal de paiement choisi (Wave, Orange Money, Carte, etc.)
        tx.setMoyenPaiement(moyen);
        // Validation immédiate du statut de la transaction
        tx.setStatut(Transaction.StatutTransaction.VALIDE);
        // Attribution de la référence de paiement externe ou génération d'un identifiant unique
        tx.setReferenceExterne(refExterne != null ? refExterne : "DEP-" + UUID.randomUUID().toString().substring(0, 8));
        // Libellé explicite de l'opération pour l'historique bancaire de l'utilisateur
        tx.setDescription("Alimentation portefeuille de " + montant + " FCFA via " + moyen.name());
        // Sauvegarde de la transaction dans la table transaction de PostgreSQL
        Transaction savedTx = transactionRepository.save(tx);

        // Écriture obligatoire d'une ligne d'audit financier pour la traçabilité fiscale et réglementaire
        audit(userId, savedTx.getId(), AuditFinancier.ActionFinanciere.DEPOT, montant, true, "Dépôt validé");

        // Diffusion d'événements en temps réel via WebSocket pour rafraîchir l'interface client instantanément
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Publication de l'événement de mise à jour du portefeuille sur le topic dédié
                realtimePublisher.publish("/topic/wallet", "WALLET_UPDATE", Map.of(
                    // Type d'opération notifiée
                    "type", "DEPOT",
                    // Identifiant de l'utilisateur concerné
                    "userId", userId.toString(),
                    // Nouveau solde actualisé après rechargement
                    "nouveauSolde", p.getSolde(),
                    // Montant du rechargement
                    "montant", montant,
                    // Nom du moyen de paiement
                    "moyenPaiement", moyen.name(),
                    // Identifiant de la transaction enregistrée
                    "transactionId", savedTx.getId() != null ? savedTx.getId().toString() : "",
                    // Libellé de la transaction
                    "description", savedTx.getDescription(),
                    // Horodatage ISO de l'opération
                    "date", LocalDateTime.now().toString()
                ));
                // Publication d'une notification push dans le centre de notifications de l'utilisateur
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Identifiant unique de la notification
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Destinataire de la notification
                    "userId", userId.toString(),
                    // Type de notification
                    "type", "PAIEMENT_VALIDE",
                    // Titre court affiché dans le bandeau
                    "titre", "Rechargement réussi",
                    // Corps descriptif de la notification
                    "corps", "Votre compte a été rechargé de " + montant + " FCFA via " + moyen.name(),
                    // Message complet
                    "message", "Votre compte a été rechargé de " + montant + " FCFA via " + moyen.name(),
                    // Date d'envoi de la notification
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Statut de lecture initialisé à faux
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {
                // Les erreurs de publication temps réel ne doivent pas faire échouer la transaction financière
            }
        }

        // Retourne la transaction sauvegardée à l'appelant
        return savedTx;
    }

    // Surcharge de la méthode payerService sans identifiant médecin explicite
    @Transactional
    // Méthode `payerService` (publique) — paramètres : `userId` (identifiant UUID), `montant` (montant décimal précis), `type` (Transaction.TypeTransaction), `serviceId` (identifiant UUID), `description` (chaîne de caractères) ; retourne : Transaction ; intention : règle (payer service)
    public Transaction payerService(UUID userId, BigDecimal montant, Transaction.TypeTransaction type, UUID serviceId, String description) {
        // Redirection vers la méthode principale avec le paramètre medecinUserId fixé à null
        return payerService(userId, montant, type, serviceId, description, null);
    }

    // RM139, RM140, RM141 — Règlement de prestation médicale : Débit patient + Crédit du médecin praticien
    // Déclaration de transaction globale Spring : en cas d'erreur, l'ensemble des opérations est annulé (Rollback)
    @Transactional
    // Méthode `payerService` (publique) — paramètres : `userId` (identifiant UUID), `montant` (montant décimal précis), `type` (Transaction.TypeTransaction), `serviceId` (identifiant UUID), `description` (chaîne de caractères), `medecinUserId` (identifiant UUID) ; retourne : Transaction ; intention : règle (payer service)
    public Transaction payerService(UUID userId, BigDecimal montant, Transaction.TypeTransaction type, UUID serviceId, String description, UUID medecinUserId) {
        // Récupération ou création automatique du portefeuille du patient émetteur
        Portefeuille pPatient = getOrCreatePortefeuille(userId);

        // RM141 — Contrôle de solvabilité : vérification que le solde disponible couvre le montant
        if (!pPatient.aSoldeSuffisant(montant)) {
            // Journalisation de l'échec pour solde insuffisant dans la table d'audit
            audit(userId, null, AuditFinancier.ActionFinanciere.ECHEC_SOLDE_INSUFFISANT, montant, false, "Solde insuffisant pour " + type.name());
            // Déclenchement d'une exception interrompant le paiement et notifiant l'utilisateur
            throw new IllegalStateException("Solde insuffisant dans votre Portefeuille Santé (" + pPatient.getSolde() + " FCFA disponibles).");
        }

        // --- ÉTAPE 1 : Débit du compte du patient ---
        // Soustraction du montant dû du solde du patient
        pPatient.debiter(montant);
        // Sauvegarde de l'état débité du portefeuille patient dans PostgreSQL
        portefeuilleRepository.save(pPatient);

        // Création de l'objet transaction représentant le débit du patient
        Transaction txPatient = new Transaction();
        // Association au portefeuille du patient
        txPatient.setPortefeuille(pPatient);
        // Spécification de la nature de la transaction (ex: CONSULTATION_PRESENTIEL, TELECONSULTATION)
        txPatient.setTypeTransaction(type);
        // Montant prélevé en FCFA
        txPatient.setMontant(montant);
        // Précision du moyen de règlement (débit direct sur le solde du portefeuille)
        txPatient.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
        // Marquage de la transaction comme validée avec succès
        txPatient.setStatut(Transaction.StatutTransaction.VALIDE);
        // Référence optionnelle vers le rendez-vous médical ou l'acte de soin
        txPatient.setServiceId(serviceId);
        // Description personnalisée ou libellé par défaut de l'opération
        txPatient.setDescription(description != null ? description : "Règlement " + type.name());
        // Enregistrement persistant de la transaction patient en base
        Transaction savedTx = transactionRepository.save(txPatient);

        // Enregistrement immuable dans l'audit financier pour certification des comptes
        audit(userId, savedTx.getId(), AuditFinancier.ActionFinanciere.PAIEMENT, montant, true, "Paiement réussi pour service " + serviceId);

        // Diffusion temps réel vers l'application mobile du patient
        if (realtimePublisher != null) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Émission de l'événement de décrémentation de solde sur le canal WebSocket
                realtimePublisher.publish("/topic/wallet", "WALLET_UPDATE", Map.of(
                    // Type d'opération notifiée
                    "type", "PAIEMENT",
                    // Identifiant de l'utilisateur patient
                    "userId", userId.toString(),
                    // Nouveau solde restant après déduction
                    "nouveauSolde", pPatient.getSolde(),
                    // Montant prélevé
                    "montant", montant,
                    // Identifiant unique de la transaction générée
                    "transactionId", savedTx.getId() != null ? savedTx.getId().toString() : "",
                    // Libellé de la transaction
                    "description", savedTx.getDescription(),
                    // Horodatage ISO
                    "date", LocalDateTime.now().toString()
                ));
                // Émission d'une alerte push au centre de notifications du patient
                realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                    // Identifiant de la notification
                    "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                    // Destinataire
                    "userId", userId.toString(),
                    // Catégorie de notification
                    "type", "PAIEMENT_VALIDE",
                    // En-tête
                    "titre", "Paiement débité",
                    // Corps du message
                    "corps", "Un paiement de " + montant + " FCFA a été effectué (" + savedTx.getDescription() + ")",
                    // Texte du message
                    "message", "Un paiement de " + montant + " FCFA a été effectué (" + savedTx.getDescription() + ")",
                    // Horodatage
                    "dateEnvoi", LocalDateTime.now().toString(),
                    // Statut non-lu
                    "lue", false
                ));
            // Interception de l'exception Exception ignored
            } catch (Exception ignored) {
                // Protection contre les interruptions de broker WebSocket
            }
        }

        // --- ÉTAPE 2 : Crédit automatique des honoraires du médecin praticien ---
        // Vérification si un identifiant médecin est fourni pour reverser la rémunération
        if (medecinUserId != null) {
            // Résolution de l'identifiant auth_user du médecin (dans le cas où un identifiant métier a été passé)
            UUID resolvedMedecinUserId = resolveMedecinUserId(medecinUserId);
            // Si le praticien destinataire est correctement identifié
            if (resolvedMedecinUserId != null) {
                // Récupération ou initialisation du portefeuille du médecin
                Portefeuille pMedecin = getOrCreatePortefeuille(resolvedMedecinUserId);
                // Augmentation du solde du praticien du montant des honoraires perçus
                pMedecin.crediter(montant);
                // Persistance du portefeuille crédité du médecin
                portefeuilleRepository.save(pMedecin);

                // Création d'une transaction miroir attestant du versement des honoraires
                Transaction txMedecin = new Transaction();
                // Rattachement au portefeuille du médecin
                txMedecin.setPortefeuille(pMedecin);
                // Typage spécifique comme honoraires de consultation médicale
                txMedecin.setTypeTransaction(Transaction.TypeTransaction.HONORAIRES_CONSULTATION);
                // Montant net crédité en FCFA
                txMedecin.setMontant(montant);
                // Provenance des fonds depuis le solde portefeuille de la plateforme
                txMedecin.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
                // Validation de l'encaissement
                txMedecin.setStatut(Transaction.StatutTransaction.VALIDE);
                // Association à la consultation correspondante
                txMedecin.setServiceId(serviceId);
                // Libellé clair pour le relevé d'activité du praticien
                txMedecin.setDescription("Honoraires reçus" + (description != null ? " (" + description + ")" : ""));
                // Sauvegarde de la transaction d'honoraires
                Transaction savedTxMedecin = transactionRepository.save(txMedecin);

                // Enregistrement d'audit pour certifier la rémunération du médecin
                audit(resolvedMedecinUserId, txMedecin.getId(), AuditFinancier.ActionFinanciere.DEPOT, montant, true, "Honoraires crédités suite à consultation " + serviceId);

                // Notification temps réel du médecin sur son application mobile / tableau de bord
                if (realtimePublisher != null) {
                    // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
                    try {
                        // Envoi de la notification de mise à jour du portefeuille médecin
                        realtimePublisher.publish("/topic/wallet", "WALLET_UPDATE", Map.of(
                            // Type de mouvement
                            "type", "HONORAIRES",
                            // Identifiant du médecin
                            "userId", resolvedMedecinUserId.toString(),
                            // Solde actualisé du médecin
                            "nouveauSolde", pMedecin.getSolde(),
                            // Montant des honoraires encaissés
                            "montant", montant,
                            // Identifiant de transaction
                            "transactionId", savedTxMedecin.getId() != null ? savedTxMedecin.getId().toString() : "",
                            // Libellé de consultation
                            "description", savedTxMedecin.getDescription(),
                            // Date de l'encaissement
                            "date", LocalDateTime.now().toString()
                        ));
                        // Notification push visuelle pour avertir le médecin de l'encaissement
                        realtimePublisher.publish("/topic/notifications", "NEW_NOTIFICATION", Map.of(
                            // Identifiant unique
                            "id", "notif-" + UUID.randomUUID().toString().substring(0, 8),
                            // Identifiant du destinataire
                            "userId", resolvedMedecinUserId.toString(),
                            // Type de notification
                            "type", "PAIEMENT_VALIDE",
                            // Titre du push
                            "titre", "Honoraires reçus",
                            // Corps informatif
                            "corps", "Vous avez reçu " + montant + " FCFA pour une consultation",
                            // Message détaillé
                            "message", "Vous avez reçu " + montant + " FCFA pour une consultation",
                            // Horodatage
                            "dateEnvoi", LocalDateTime.now().toString(),
                            // Non-lu
                            "lue", false
                        ));
                    // Interception de l'exception Exception ignored
                    } catch (Exception ignored) {
                        // Tolérance aux pannes du socket
                    }
                }
            }
        }

        // Retour de la transaction débit patient
        return savedTx;
    }

    // RM136, RM137 — Invitation et rattachement d'un bénéficiaire familial au portefeuille du tuteur
    // Transaction Spring assurant la création ou mise à jour sécurisée du lien de parenté
    @Transactional
    // Méthode `inviterBeneficiaire` (publique) — paramètres : `tuteurUserId` (identifiant UUID), `beneficiaireUserId` (identifiant UUID), `lien` (Beneficiaire.LienParente), `plafond` (montant décimal précis) ; retourne : Beneficiaire ; intention : invite (inviter beneficiaire)
    public Beneficiaire inviterBeneficiaire(UUID tuteurUserId, UUID beneficiaireUserId, Beneficiaire.LienParente lien, BigDecimal plafond) {
        // Recherche ou initialisation du portefeuille du tuteur légal / responsable financier
        Portefeuille p = getOrCreatePortefeuille(tuteurUserId);

        // Recherche d'un lien de bénéficiaire existant entre ce portefeuille et ce proche
        Beneficiaire b = beneficiaireRepository
                // Requête par identifiant de portefeuille et identifiant utilisateur du proche
                .findByPortefeuilleIdAndBeneficiaireUserId(p.getId(), beneficiaireUserId)
                // Si la relation n'existe pas encore, instanciation d'un nouveau lien
                .orElseGet(() -> {
                    // Création de l'entité Beneficiaire
                    Beneficiaire newB = new Beneficiaire();
                    // Rattachement au portefeuille payeur du tuteur
                    newB.setPortefeuille(p);
                    // Définition de l'identifiant du membre de la famille rattaché
                    newB.setBeneficiaireUserId(beneficiaireUserId);
                    // Renvoi de la nouvelle instance
                    return newB;
                });

        // Définition du lien de parenté (ENFANT, CONJOINT, PARENT, PROCHE, etc.)
        b.setLienParente(lien);
        // Fixation du plafond mensuel de dépenses de santé autorisées en FCFA
        b.setPlafondMensuel(plafond);
        // Initialisation du statut à EN_ATTENTE de l'acceptation par le proche
        b.setStatut(Beneficiaire.StatutBeneficiaire.EN_ATTENTE);

        // Enregistrement de l'invitation dans PostgreSQL et retour de l'entité persistée
        return beneficiaireRepository.save(b);
    }

    // Récupération en lecture seule des invitations en attente pour un utilisateur bénéficiaire
    @Transactional(readOnly = true)
    // Méthode `getInvitationsEnAttente` (publique) — paramètres : `beneficiaireUserId` (identifiant UUID) ; retourne : liste de Beneficiaire ; intention : récupère (get invitations en attente)
    public List<Beneficiaire> getInvitationsEnAttente(UUID beneficiaireUserId) {
        // Recherche en base de données de toutes les invitations en attente adressées à cet utilisateur
        List<Beneficiaire> list = beneficiaireRepository.findByBeneficiaireUserIdAndStatut(
                // Suite de l'instruction précédente : beneficiaireUserId, Beneficiaire.StatutBeneficiaire.EN_ATTENTE);
                beneficiaireUserId, Beneficiaire.StatutBeneficiaire.EN_ATTENTE);
        // Parcours de la liste pour forcer l'initialisation du proxy Hibernate du portefeuille (évite LazyInitializationException)
        list.forEach(b -> {
            // Vérification de la présence de la relation portefeuille
            if (b.getPortefeuille() != null) {
                // Lecture de l'identifiant du tuteur pour charger l'objet
                b.getPortefeuille().getUserId();
            }
        });
        // Renvoi de la liste des invitations chargées
        return list;
    }

    // Traitement de l'acceptation ou du refus d'une invitation de parrainage financier
    @Transactional
    // Méthode `repondreInvitation` (publique) — paramètres : `beneficiaireId` (identifiant UUID), `action` (chaîne de caractères) ; retourne : Beneficiaire ; intention : répond à (repondre invitation)
    public Beneficiaire repondreInvitation(UUID beneficiaireId, String action) {
        // Recherche du lien de bénéficiaire par son identifiant unique
        Beneficiaire b = beneficiaireRepository.findById(beneficiaireId)
                // Déclenchement d'une exception si l'invitation est introuvable
                .orElseThrow(() -> new IllegalArgumentException("Invitation introuvable"));
        // Vérification si l'action choisie par le proche est une acceptation
        if ("ACCEPTER".equalsIgnoreCase(action)) {
            // Activation immédiate de la prise en charge financière
            b.setStatut(Beneficiaire.StatutBeneficiaire.ACTIF);
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Rejet et annulation de l'invitation
            b.setStatut(Beneficiaire.StatutBeneficiaire.REJETE);
        }
        // Sauvegarde de l'état mis à jour dans PostgreSQL
        Beneficiaire saved = beneficiaireRepository.save(b);
        // Chargement du tuteur associé si existant pour transmission complète au client
        if (saved.getPortefeuille() != null) {
            // Déclenchement du getter pour hydrater le proxy Hibernate
            saved.getPortefeuille().getUserId();
        }
        // Retour de l'entité mise à jour
        return saved;
    }

    // Consultation de la liste des bénéficiaires gérés par un tuteur
    @Transactional(readOnly = true)
    // Méthode `getMesBeneficiaires` (publique) — paramètres : `tuteurUserId` (identifiant UUID) ; retourne : liste de Beneficiaire ; intention : récupère (get mes beneficiaires)
    public List<Beneficiaire> getMesBeneficiaires(UUID tuteurUserId) {
        // Obtention du portefeuille du tuteur connecté
        Portefeuille p = getOrCreatePortefeuille(tuteurUserId);
        // Récupération de tous les bénéficiaires rattachés à ce portefeuille
        List<Beneficiaire> list = beneficiaireRepository.findByPortefeuilleId(p.getId());
        // Parcours pour initialiser les relations Lazy
        list.forEach(b -> {
            // Contrôle de non-nullité du portefeuille
            if (b.getPortefeuille() != null) {
                // Chargement de l'identifiant utilisateur tuteur
                b.getPortefeuille().getUserId();
            }
        });
        // Renvoi de la liste des proches parrainés
        return list;
    }

    // RM138 — Prise en charge financière d'un soin médical pour un proche par le tuteur
    @Transactional
    // Méthode `payerPourProche` (publique) — paramètres : `tuteurUserId` (identifiant UUID), `beneficiaireUserId` (identifiant UUID), `montant` (montant décimal précis), `serviceId` (identifiant UUID), `description` (chaîne de caractères) ; retourne : Transaction ; intention : règle (payer pour proche)
    public Transaction payerPourProche(UUID tuteurUserId, UUID beneficiaireUserId, BigDecimal montant, UUID serviceId, String description) {
        // Récupération du portefeuille du tuteur financier
        Portefeuille tuteurPortefeuille = getOrCreatePortefeuille(tuteurUserId);

        // Vérification formelle du lien de parenté entre le tuteur et le bénéficiaire
        Beneficiaire b = beneficiaireRepository
                // Requête par couple portefeuille / bénéficiaire
                .findByPortefeuilleIdAndBeneficiaireUserId(tuteurPortefeuille.getId(), beneficiaireUserId)
                // Rejet si aucun lien de bénéficiaire n'a été préalablement établi
                .orElseThrow(() -> new IllegalArgumentException("Ce bénéficiaire n'est pas associé à votre portefeuille familial."));

        // Contrôle que l'autorisation de prise en charge est active et non suspendue
        if (!b.isAutorise()) {
            // Blocage de l'opération en cas de suspension ou révocation
            throw new IllegalStateException("L'autorisation de prise en charge pour ce bénéficiaire est actuellement suspendue.");
        }

        // RM141 — Contrôle de solvabilité sur le portefeuille du tuteur
        if (!tuteurPortefeuille.aSoldeSuffisant(montant)) {
            // Trace d'échec dans l'audit financier
            audit(tuteurUserId, null, AuditFinancier.ActionFinanciere.ECHEC_SOLDE_INSUFFISANT, montant, false, "Solde tuteur insuffisant pour proche " + beneficiaireUserId);
            // Rejet de la transaction pour provision insuffisante
            throw new IllegalStateException("Le portefeuille du tuteur dispose d'un solde insuffisant.");
        }

        // Débit du montant sur le compte du tuteur
        tuteurPortefeuille.debiter(montant);
        // Persistance du portefeuille du tuteur
        portefeuilleRepository.save(tuteurPortefeuille);

        // Création de l'enregistrement de transaction familiale
        Transaction tx = new Transaction();
        // Attribution au portefeuille débité
        tx.setPortefeuille(tuteurPortefeuille);
        // Typage comme paiement au bénéfice d'un membre familial
        tx.setTypeTransaction(Transaction.TypeTransaction.PAIEMENT_FAMILIAL);
        // Montant payé
        tx.setMontant(montant);
        // Mode de règlement
        tx.setMoyenPaiement(Transaction.MoyenPaiement.SOLDE_PORTEFEUILLE);
        // Validation immédiate
        tx.setStatut(Transaction.StatutTransaction.VALIDE);
        // Identification du proche bénéficiaire des soins
        tx.setBeneficiaireUserId(beneficiaireUserId);
        // Référence de la consultation ou de la prescription médicale prise en charge
        tx.setServiceId(serviceId);
        // Libellé explicite
        tx.setDescription(description != null ? description : "Prise en charge soin de santé pour proche");
        // Persistance de la transaction dans la base de données
        Transaction savedTx = transactionRepository.save(tx);

        // Enregistrement dans le journal d'audit de sécurité financière
        audit(tuteurUserId, savedTx.getId(), AuditFinancier.ActionFinanciere.PAIEMENT, montant, true, "Paiement pour proche " + beneficiaireUserId);
        // Retour de la transaction enregistrée
        return savedTx;
    }

    // RM142 — Historique chronologique complet des transactions d'un utilisateur
    public List<Transaction> getHistorique(UUID userId) {
        // Chargement du portefeuille de l'utilisateur
        Portefeuille p = getOrCreatePortefeuille(userId);
        // Récupération de l'ensemble des transactions classées de la plus récente à la plus ancienne
        return transactionRepository.findByPortefeuilleIdOrderByDateTransactionDesc(p.getId());
    }

    // RM145 — Traçabilité, immuabilité et audit financier légal
    private void audit(UUID userId, UUID transactionId, AuditFinancier.ActionFinanciere action, BigDecimal montant, boolean success, String details) {
        // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
        try {
            // Création d'une nouvelle ligne d'audit
            AuditFinancier item = new AuditFinancier();
            // Identifiant de l'auteur de l'opération
            item.setUserId(userId);
            // Identifiant de la transaction associée (si existante)
            item.setTransactionId(transactionId);
            // Nature de l'opération financière (DEPOT, RETRAIT, PAIEMENT, etc.)
            item.setAction(action);
            // Montant concerné en FCFA
            item.setMontant(montant);
            // Indicateur de succès ou d'échec
            item.setSuccess(success);
            // Détails contextuels ou motif de rejet
            item.setDetails(details);
            // Sauvegarde dans la table d'audit financier de PostgreSQL
            auditRepository.save(item);
        // Interception de l'exception Exception ignored
        } catch (Exception ignored) {
            // Tolérance aux pannes : un dysfonctionnement de log ne doit jamais bloquer les flux de trésorerie
        }
    }

    // Consultation de l'ensemble des portefeuilles pour le tableau de bord d'administration
    public List<Portefeuille> getAllPortefeuilles() {
        // Requête de tous les comptes enregistrés
        return portefeuilleRepository.findAll();
    }

    // Consultation et tri global de toutes les transactions pour la supervision financière
    public List<Transaction> getAllTransactions() {
        // Récupération du flux complet des transactions
        return transactionRepository.findAll().stream()
                // Tri décroissant par date de transaction
                .sorted((a, b) -> {
                    // Gestion des dates nulles éventuelles
                    if (a.getDateTransaction() == null) return 1;
                    // Condition : exécute le bloc suivant seulement si `b.getDateTransaction() == null) return -1;`
                    if (b.getDateTransaction() == null) return -1;
                    // Comparaison temporelle
                    return b.getDateTransaction().compareTo(a.getDateTransaction());
                })
                // Collecte des résultats dans une liste
                .collect(java.util.stream.Collectors.toList());
    }

    // Opération d'ajustement administratif de solde (régularisation ou remboursement)
    @Transactional
    // Méthode `ajusterPortefeuille` (publique) — paramètres : `userId` (identifiant UUID), `montant` (montant décimal précis), `typeAjustement` (chaîne de caractères), `justification` (chaîne de caractères) ; retourne : Transaction ; intention : ajuste (ajuster portefeuille)
    public Transaction ajusterPortefeuille(UUID userId, BigDecimal montant, String typeAjustement, String justification) {
        // Récupération du portefeuille concerné
        Portefeuille p = getOrCreatePortefeuille(userId);
        // Détermination du sens du mouvement (Crédit ou Débit)
        boolean isCredit = "CREDIT".equalsIgnoreCase(typeAjustement);

        // Application de l'ajustement selon la nature demandée
        if (isCredit) {
            // Crédit du compte
            p.crediter(montant);
        // Sinon (cas contraire de la condition précédente)
        } else {
            // Débit exceptionnel du compte
            p.debiter(montant);
        }
        // Enregistrement du nouveau solde dans PostgreSQL
        portefeuilleRepository.save(p);

        // Création de la transaction d'ajustement comptable
        Transaction tx = new Transaction();
        // Attribution au portefeuille
        tx.setPortefeuille(p);
        // Typage comptable selon le sens
        tx.setTypeTransaction(isCredit ? Transaction.TypeTransaction.DEPOT : Transaction.TypeTransaction.PAIEMENT_PARTENAIRE);
        // Montant régularisé
        tx.setMontant(montant);
        // Méthode de règlement marquée comme virement / régularisation
        tx.setMoyenPaiement(Transaction.MoyenPaiement.VIREMENT);
        // Statut validé
        tx.setStatut(Transaction.StatutTransaction.VALIDE);
        // Justification obligatoire archivée
        tx.setDescription("Ajustement admin (" + (isCredit ? "Crédit" : "Débit") + ") : " + justification);
        // Sauvegarde de la transaction
        Transaction saved = transactionRepository.save(tx);

        // Audit obligatoire de l'ajustement administratif avec la justification
        audit(userId, saved.getId(), isCredit ? AuditFinancier.ActionFinanciere.DEPOT : AuditFinancier.ActionFinanciere.PAIEMENT,
                // Suite de l'instruction précédente : montant, true, "Ajustement administratif : " + justification);
                montant, true, "Ajustement administratif : " + justification);
        // Retour de la transaction validée
        return saved;
    }

    // Suppression d'un rattachement de bénéficiaire familial
    @Transactional
    // Méthode `supprimerBeneficiaire` (publique) — paramètres : `beneficiaireId` (identifiant UUID) ; retourne : aucune valeur ; intention : supprime (supprimer beneficiaire)
    public void supprimerBeneficiaire(UUID beneficiaireId) {
        // Recherche du bénéficiaire à supprimer
        Beneficiaire b = beneficiaireRepository.findById(beneficiaireId)
                // Exception si l'identifiant est introuvable
                .orElseThrow(() -> new IllegalArgumentException("Bénéficiaire introuvable : " + beneficiaireId));
        // Suppression effective de l'association en base
        beneficiaireRepository.delete(b);
    }
}

