package com.diamyaraam.dossier.service;

import com.diamyaraam.dossier.entity.CartePhysique;
import com.diamyaraam.dossier.repository.CartePhysiqueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartePhysiqueService {

    private final CartePhysiqueRepository cartePhysiqueRepository;

    public CartePhysiqueService(CartePhysiqueRepository cartePhysiqueRepository) {
        this.cartePhysiqueRepository = cartePhysiqueRepository;
    }

    /**
     * Lier une carte physique vierge à un patient.
     * Cette méthode est appelée quand l'agent scanne le QR code d'une nouvelle carte.
     */
    @Transactional
    public CartePhysique lierCarteAuPatient(String qrTokenSecurise, UUID patientId) {
        // 1. Trouver la carte via le QR Code scanné
        CartePhysique carte = cartePhysiqueRepository.findByQrTokenSecurise(qrTokenSecurise)
                .orElseThrow(() -> new RuntimeException("Carte introuvable ou QR Code invalide"));

        // 2. Vérifier si la carte est bien en stock et non utilisée
        if (carte.getStatut() != CartePhysique.StatutCarte.EN_STOCK) {
            throw new RuntimeException("Cette carte a déjà été utilisée ou est invalide (Statut: " + carte.getStatut() + ")");
        }

        // 3. (Optionnel) Désactiver l'ancienne carte du patient s'il en avait une
        Optional<CartePhysique> ancienneCarte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE);
        if (ancienneCarte.isPresent()) {
            CartePhysique oldCard = ancienneCarte.get();
            oldCard.setStatut(CartePhysique.StatutCarte.DESACTIVEE);
            cartePhysiqueRepository.save(oldCard);
        }

        // 4. Activer la nouvelle carte pour ce patient
        carte.setPatientId(patientId);
        carte.setStatut(CartePhysique.StatutCarte.ACTIVE);
        carte.setDateActivation(LocalDateTime.now());

        return cartePhysiqueRepository.save(carte);
    }

    /**
     * Générer un jeton dynamique valable 10 minutes pour la carte virtuelle (Flutter).
     */
    public String genererJetonDynamique(UUID patientId) {
        CartePhysique carte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucune carte active trouvée"));

        // Expiration = maintenant + 10 minutes (600 000 ms)
        long expiry = System.currentTimeMillis() + 600000;
        
        // Format du jeton: qrTokenSecurise|expiry
        String rawData = carte.getQrTokenSecurise() + "|" + expiry;
        
        // Encodage en Base64 (Dans un vrai système en production, utilisez un JWT signé cryptographiquement)
        return Base64.getEncoder().encodeToString(rawData.getBytes());
    }

    /**
     * Récupérer le dossier (patientId) à partir d'un scan de carte (physique ou virtuelle) en cas d'urgence.
     */
    public UUID getPatientIdByScannerCarte(String scannedToken) {
        String qrTokenSecurise = scannedToken;

        // Vérifier si c'est un jeton dynamique (Base64) de la carte virtuelle
        if (scannedToken.length() > 50 && !scannedToken.contains("-")) {
            try {
                String decoded = new String(Base64.getDecoder().decode(scannedToken));
                if (decoded.contains("|")) {
                    String[] parts = decoded.split("\\|");
                    qrTokenSecurise = parts[0];
                    long expiry = Long.parseLong(parts[1]);
                    
                    if (System.currentTimeMillis() > expiry) {
                        throw new RuntimeException("Le QR Code de la carte virtuelle a expiré (10 minutes). Veuillez demander au patient d'actualiser son application.");
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Ce n'était pas du Base64, on suppose que c'est le token de la carte physique directe.
            }
        }

        CartePhysique carte = cartePhysiqueRepository.findByQrTokenSecurise(qrTokenSecurise)
                .orElseThrow(() -> new RuntimeException("Carte introuvable ou QR Code invalide"));

        if (carte.getStatut() != CartePhysique.StatutCarte.ACTIVE) {
            throw new RuntimeException("Cette carte n'est pas active");
        }

        return carte.getPatientId();
    }

    /**
     * Déclarer une carte perdue
     */
    @Transactional
    public CartePhysique declarerCartePerdue(UUID patientId) {
        CartePhysique carte = cartePhysiqueRepository.findByPatientIdAndStatut(patientId, CartePhysique.StatutCarte.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aucune carte active trouvée pour ce patient"));

        carte.setStatut(CartePhysique.StatutCarte.PERDUE);
        return cartePhysiqueRepository.save(carte);
    }
}
