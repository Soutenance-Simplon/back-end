// Déclaration du package Java : `com.diamyaraam.dossier.service`
package com.diamyaraam.dossier.service;

// Import de la classe `CartePhysique` (paquet com.diamyaraam.dossier.entity)
import com.diamyaraam.dossier.entity.CartePhysique;
// Import de la classe `CartePhysiqueRepository` (paquet com.diamyaraam.dossier.repository)
import com.diamyaraam.dossier.repository.CartePhysiqueRepository;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `Base64` (paquet java.util)
import java.util.Base64;
// Import de la classe `Optional` (paquet java.util)
import java.util.Optional;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

// Annotation marquant la classe comme composant de service Spring injecté
@Service
// Déclaration de la classe `CartePhysiqueService` (rôle : porte la logique métier)
public class CartePhysiqueService {

    // Déclaration du dépôt JPA pour la persistance des cartes physiques et badges de santé
    private final CartePhysiqueRepository cartePhysiqueRepository;

    // Constructeur pour l'injection de dépendances gérée par Spring
    public CartePhysiqueService(CartePhysiqueRepository cartePhysiqueRepository) {
        // Affectation du repository injecté
        this.cartePhysiqueRepository = cartePhysiqueRepository;
    }

    /**
     * Lier une carte physique vierge à un patient.
     * Cette méthode est appelée quand l'agent scanne le QR code d'une nouvelle carte.
     */
    // Transaction atomique garantissant l'inactivation de l'ancienne carte et l'activation de la nouvelle
    @Transactional
    // Méthode `lierCarteAuPatient` (publique) — paramètres : `qrTokenSecurise` (chaîne de caractères), `patientId` (identifiant UUID) ; retourne : CartePhysique ; intention : lie (lier carte au patient)
    public CartePhysique lierCarteAuPatient(String qrTokenSecurise, UUID patientId) {
        // 1. Recherche de la carte en base par le jeton cryptographique encodé dans le QR code
        CartePhysique carte = cartePhysiqueRepository.findByQrTokenSecurise(qrTokenSecurise)
                // Exception explicite si le jeton n'existe pas en base de données
                .orElseThrow(() -> new RuntimeException("Carte introuvable ou QR Code invalide"));

        // 2. Contrôle de sécurité : vérification que la carte physique est bien neuve en stock
        if (carte.getStatut() != CartePhysique.StatutCarte.EN_STOCK) {
            // Rejet immédiat si la carte a déjà été attribuée ou altérée
            throw new RuntimeException("Cette carte a déjà été utilisée ou est invalide (Statut: " + carte.getStatut() + ")");
        }

        // 3. Recherche de toute carte active préexistante pour ce patient afin de la révoquer
        Optional<CartePhysique> ancienneCarte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE);
        // Si une ancienne carte active existe déjà pour ce patient
        if (ancienneCarte.isPresent()) {
            // Extraction de l'ancienne carte
            CartePhysique oldCard = ancienneCarte.get();
            // Basculement de son statut vers DÉSACTIVÉE (révocation)
            oldCard.setStatut(CartePhysique.StatutCarte.DESACTIVEE);
            // Sauvegarde de la révocation en base de données
            cartePhysiqueRepository.save(oldCard);
        }

        // 4. Attribution de la nouvelle carte au patient concerné
        carte.setPatientId(patientId);
        // Changement du statut vers ACTIVE
        carte.setStatut(CartePhysique.StatutCarte.ACTIVE);
        // Enregistrement de l'horodatage précis de l'activation
        carte.setDateActivation(LocalDateTime.now());

        // Persistance de la carte activée et renvoi de l'entité
        return cartePhysiqueRepository.save(carte);
    }

    /**
     * Générer un jeton dynamique valable 10 minutes pour la carte virtuelle (Flutter).
     */
    public String genererJetonDynamique(UUID patientId) {
        // Recherche de la carte active du patient
        CartePhysique carte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE)
                // Exception si le patient ne dispose d'aucune carte en vigueur
                .orElseThrow(() -> new RuntimeException("Aucune carte active trouvée"));

        // Calcul du timestamp d'expiration (temps actuel + 10 minutes / 600 000 millisecondes)
        long expiry = System.currentTimeMillis() + 600000;
        
        // Concaténation de la charge utile : jeton sécurisé et horodatage d'échéance
        String rawData = carte.getQrTokenSecurise() + "|" + expiry;
        
        // Encodage Base64 de la charge utile (dans une infra de production, un JWT signé est utilisé)
        return Base64.getEncoder().encodeToString(rawData.getBytes());
    }

    /**
     * Récupérer le dossier (patientId) à partir d'un scan de carte (physique ou virtuelle) en cas d'urgence.
     */
    public UUID getPatientIdByScannerCarte(String scannedToken) {
        // Initialisation de la variable de travail avec le jeton scanné
        String qrTokenSecurise = scannedToken;

        // Détection heuristique d'un jeton dynamique virtuel encodé en Base64
        if (scannedToken.length() > 50 && !scannedToken.contains("-")) {
            // Début d'un bloc `try` : les erreurs seront interceptées par le bloc `catch`
            try {
                // Décodage de la chaîne Base64
                String decoded = new String(Base64.getDecoder().decode(scannedToken));
                // Vérification de la présence du délimiteur temporel
                if (decoded.contains("|")) {
                    // Séparation du jeton et du timestamp d'expiration
                    String[] parts = decoded.split("\\|");
                    // Récupération de la clé de carte
                    qrTokenSecurise = parts[0];
                    // Parsing du temps d'expiration
                    long expiry = Long.parseLong(parts[1]);
                    
                    // Contrôle d'expiration temporelle anti-rejeu (Replay Attack)
                    if (System.currentTimeMillis() > expiry) {
                        // Rejet du jeton périmé après 10 minutes
                        throw new RuntimeException("Le QR Code de la carte virtuelle a expiré (10 minutes). Veuillez demander au patient d'actualiser son application.");
                    }
                }
            // Interception de l'exception IllegalArgumentException ignored
            } catch (IllegalArgumentException ignored) {
                // Si le décodage échoue, traitement comme un jeton de badge physique direct
            }
        }

        // Recherche de la carte correspondante dans la base de données
        CartePhysique carte = cartePhysiqueRepository.findByQrTokenSecurise(qrTokenSecurise)
                // Exception si la carte n'existe pas
                .orElseThrow(() -> new RuntimeException("Carte introuvable ou QR Code invalide"));

        // Contrôle que la carte scannée est bien dans un état actif
        if (carte.getStatut() != CartePhysique.StatutCarte.ACTIVE) {
            // Rejet si la carte est perdue, désactivée ou en stock
            throw new RuntimeException("Cette carte n'est pas active");
        }

        // Renvoi de l'identifiant patient résolu pour accès au dossier de soins
        return carte.getPatientId();
    }

    /**
     * Déclarer une carte perdue
     */
    // Transaction assurant le passage immédiat de la carte au statut PERDUE
    @Transactional
    // Méthode `declarerCartePerdue` (publique) — paramètres : `patientId` (identifiant UUID) ; retourne : CartePhysique ; intention : déclare (declarer carte perdue)
    public CartePhysique declarerCartePerdue(UUID patientId) {
        // Recherche de la carte actuellement active pour ce patient
        CartePhysique carte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE)
                // Exception si aucune carte active n'est trouvée
                .orElseThrow(() -> new RuntimeException("Aucune carte active trouvée pour ce patient"));

        // Invalidation de la carte pour empêcher toute utilisation frauduleuse
        carte.setStatut(CartePhysique.StatutCarte.PERDUE);
        // Persistance du nouveau statut
        return cartePhysiqueRepository.save(carte);
    }
}
