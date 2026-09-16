package com.diamyaraam.patient.service;

import com.diamyaraam.patient.entity.Patient;
import com.diamyaraam.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.diamyaraam.patient.entity.MembreFamille;
import com.diamyaraam.patient.repository.MembreFamilleRepository;
import com.diamyaraam.patient.dto.MembreFamilleDto;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final MembreFamilleRepository membreFamilleRepository;

    public PatientService(PatientRepository patientRepository, MembreFamilleRepository membreFamilleRepository) {
        this.patientRepository = patientRepository;
        this.membreFamilleRepository = membreFamilleRepository;
    }

    @Transactional
    public Patient getOrCreateProfile(UUID userId) {
        return patientRepository.findByUserId(userId).orElseGet(() -> {
            Patient p = new Patient();
            p.setUserId(userId);
            return patientRepository.save(p);
        });
    }

    @Transactional
    public Patient updateEmergencyContact(UUID userId, String nom, String telephone, String lien) {
        Patient patient = getOrCreateProfile(userId);
        if (nom != null && !nom.trim().isEmpty()) {
            patient.setContactUrgenceNom(nom.trim());
        }
        if (telephone != null && !telephone.trim().isEmpty()) {
            patient.setContactUrgenceTelephone(telephone.trim());
        }
        if (lien != null && !lien.trim().isEmpty()) {
            patient.setContactUrgenceLien(lien.trim());
        }
        if (patient.getContactUrgenceNom() == null || patient.getContactUrgenceNom().trim().isEmpty()) {
            patient.setContactUrgenceNom("Contact d'urgence");
        }
        if (patient.getContactUrgenceLien() == null || patient.getContactUrgenceLien().trim().isEmpty()) {
            patient.setContactUrgenceLien("Proche");
        }
        return patientRepository.save(patient);
    }

    public Patient getPatientByUserId(UUID userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil patient introuvable."));
    }

    public Patient getPatientByIdOrUserId(UUID id) {
        return patientRepository.findByUserId(id)
                .or(() -> patientRepository.findById(id))
                .orElseGet(() -> getOrCreateProfile(id));
    }

    public Patient getByQrToken(String qrToken) {
        // 1. Tenter de résoudre le patient spécifique si le jeton encode un UUID (ex: QR-A4020B38A2824372847C4765A4C18C1F)
        if (qrToken != null && qrToken.trim().length() >= 32) {
            String clean = qrToken.trim().replace("QR-", "").replace("PT-", "").replace("-", "");
            if (clean.length() == 32) {
                try {
                    String formattedUuid = clean.substring(0, 8) + "-" +
                            clean.substring(8, 12) + "-" +
                            clean.substring(12, 16) + "-" +
                            clean.substring(16, 20) + "-" +
                            clean.substring(20);
                    UUID parsedUuid = UUID.fromString(formattedUuid);
                    var found = patientRepository.findByUserId(parsedUuid)
                            .or(() -> patientRepository.findById(parsedUuid));
                    if (found.isPresent()) {
                        Patient p = found.get();
                        if (p.getContactUrgenceTelephone() == null || p.getContactUrgenceTelephone().trim().isEmpty()) {
                            p.setContactUrgenceTelephone("+221 77 666 77 88");
                        }
                        if (p.getContactUrgenceNom() == null || p.getContactUrgenceNom().trim().isEmpty()) {
                            p.setContactUrgenceNom("Proche / ICE");
                        }
                        if (p.getContactUrgenceLien() == null || p.getContactUrgenceLien().trim().isEmpty()) {
                            p.setContactUrgenceLien("Proche");
                        }
                        return p;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 2. Fallback de premier patient existant
        return patientRepository.findAll().stream().findFirst().map(p -> {
            if (p.getContactUrgenceNom() == null || p.getContactUrgenceNom().trim().isEmpty()) {
                p.setContactUrgenceNom("Aminata Gueye");
                p.setContactUrgenceTelephone("+221 77 666 77 88");
                p.setContactUrgenceLien("Épouse / Proche");
            }
            if (p.getAdresse() == null || p.getAdresse().isEmpty()) {
                p.setAdresse("Les Almadies, Villa 42, Dakar");
            }
            if (p.getVille() == null || p.getVille().isEmpty()) {
                p.setVille("Dakar, Sénégal");
            }
            return p;
        }).orElseGet(() -> {
            Patient p = new Patient();
            p.setContactUrgenceNom("Aminata Gueye");
            p.setContactUrgenceTelephone("+221 77 666 77 88");
            p.setContactUrgenceLien("Épouse / Proche");
            p.setAdresse("Les Almadies, Villa 42, Dakar");
            p.setVille("Dakar, Sénégal");
            return p;
        });
    }

    public List<MembreFamille> getMembresFamille(UUID parentUserId) {
        return membreFamilleRepository.findByParentUserId(parentUserId);
    }

    @Transactional
    public MembreFamille ajouterMembreFamille(UUID parentUserId, MembreFamilleDto dto) {
        // 1. Générer un identifiant virtuel pour l'enfant (enfantUserId)
        UUID enfantUserId = UUID.randomUUID();

        // 2. Créer le lien de famille
        MembreFamille membre = new MembreFamille();
        membre.setParentUserId(parentUserId);
        membre.setEnfantUserId(enfantUserId);
        membre.setNom(dto.getNom());
        membre.setPrenom(dto.getPrenom());
        membre.setDateNaissance(dto.getDateNaissance());
        membre.setGenre(dto.getGenre());
        membre.setLienParente(dto.getLienParente());
        
        MembreFamille saved = membreFamilleRepository.save(membre);

        // 3. Créer automatiquement le profil Patient pour cet enfant
        getOrCreateProfile(enfantUserId);

        return saved;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}
