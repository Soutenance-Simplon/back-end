package com.diamyaraam.medecin.service;

import com.diamyaraam.medecin.entity.Medecin;
import com.diamyaraam.medecin.entity.OnmsReference;
import com.diamyaraam.medecin.repository.MedecinRepository;
import com.diamyaraam.medecin.repository.OnmsReferenceRepository;
import com.diamyaraam.shared.dto.MedecinDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedecinService {

    private final MedecinRepository medecinRepository;
    private final OnmsReferenceRepository onmsReferenceRepository;

    public MedecinService(MedecinRepository medecinRepository, OnmsReferenceRepository onmsReferenceRepository) {
        this.medecinRepository = medecinRepository;
        this.onmsReferenceRepository = onmsReferenceRepository;
    }

    /**
     * Verification ONMS (RM034, RM036)
     */
    @Transactional
    public MedecinDto verifyAndRegisterMedecin(UUID userId, String numeroOrdre) {
        OnmsReference onms = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable dans la base de référence."));

        if (!onms.estAutoriseAExercer()) {
            throw new IllegalStateException("Le médecin rattaché à ce numéro n'est pas autorisé à exercer.");
        }

        Medecin medecin = medecinRepository.findByUserId(userId).orElseGet(Medecin::new);
        medecin.setUserId(userId);
        medecin.setOnmsReference(onms);
        medecin.setIsVerified(true);
        medecin.setVerifiedAt(LocalDateTime.now());
        medecin.setStatutMedecin(Medecin.StatutMedecin.ACTIF);

        Medecin saved = medecinRepository.save(medecin);
        return toDto(saved);
    }

    public OnmsReference lookupOnms(String numeroOrdre) {
        return onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable dans la base de référence."));
    }

    public List<MedecinDto> searchMedecins(String specialite, String region) {
        return searchMedecins(specialite, region, null);
    }

    private String normalize(String str) {
        if (str == null) return "";
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    public List<MedecinDto> searchMedecins(String specialite, String region, String search) {
        List<Medecin> list = medecinRepository.findByStatutMedecinAndIsVerifiedTrue(Medecin.StatutMedecin.ACTIF);
        boolean hasSpec = specialite != null && !specialite.trim().isEmpty();
        boolean hasReg = region != null && !region.trim().isEmpty();
        boolean hasSearch = search != null && !search.trim().isEmpty();

        if (hasSpec || hasReg || hasSearch) {
            String specNorm = hasSpec ? normalize(specialite) : "";
            String regNorm = hasReg ? normalize(region) : "";
            String searchNorm = hasSearch ? normalize(search) : "";

            list = list.stream()
                    .filter(m -> {
                        if (m.getOnmsReference() == null) return false;
                        OnmsReference onms = m.getOnmsReference();

                        String docSpecNorm = normalize(onms.getSpecialite());
                        boolean matchSpec = !hasSpec || docSpecNorm.contains(specNorm) || specNorm.contains(docSpecNorm);
                        
                        String docRegNorm = normalize(onms.getRegion());
                        boolean matchReg = !hasReg || docRegNorm.contains(regNorm);

                        boolean matchSearch = !hasSearch;
                        if (hasSearch) {
                            String fullName = normalize((onms.getPrenom() != null ? onms.getPrenom() : "") + " " +
                                              (onms.getNom() != null ? onms.getNom() : ""));
                            String numOrdre = normalize(onms.getNumeroOrdre());
                            String etablissement = normalize(onms.getEtablissement());
                            matchSearch = fullName.contains(searchNorm)
                                    || numOrdre.contains(searchNorm)
                                    || docSpecNorm.contains(searchNorm)
                                    || etablissement.contains(searchNorm);
                        }

                        return matchSpec && matchReg && matchSearch;
                    })
                    .collect(Collectors.toList());
        }

        return list.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<java.util.Map<String, Object>> getSpecialites() {
        // Obtenir d'abord les spécialités distinctes des médecins enregistrés
        List<String> distinctSpecs = medecinRepository.findByStatutMedecinAndIsVerifiedTrue(Medecin.StatutMedecin.ACTIF)
                .stream()
                .map(m -> m.getOnmsReference() != null ? m.getOnmsReference().getSpecialite() : null)
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        List<java.util.Map<String, Object>> standardSpecs = List.of(
                java.util.Map.of("id", 1, "nom", "Cardiologie", "nom_specialite", "Cardiologie", "description", "Maladies du cœur et des vaisseaux", "icone", "favorite"),
                java.util.Map.of("id", 2, "nom", "Pédiatrie", "nom_specialite", "Pédiatrie", "description", "Santé des enfants et des nourrissons", "icone", "child_care"),
                java.util.Map.of("id", 3, "nom", "Médecine Générale", "nom_specialite", "Médecine Générale", "description", "Consultations et soins primaires", "icone", "medical_services"),
                java.util.Map.of("id", 4, "nom", "Gynécologie Obstétrique", "nom_specialite", "Gynécologie Obstétrique", "description", "Santé de la femme et suivi de grossesse", "icone", "pregnant_woman"),
                java.util.Map.of("id", 5, "nom", "Dermatologie", "nom_specialite", "Dermatologie", "description", "Soins et pathologies de la peau", "icone", "healing"),
                java.util.Map.of("id", 6, "nom", "Ophtalmologie", "nom_specialite", "Ophtalmologie", "description", "Santé des yeux et de la vision", "icone", "remove_red_eye"),
                java.util.Map.of("id", 7, "nom", "Radiologie", "nom_specialite", "Radiologie", "description", "Imagerie médicale et diagnostics", "icone", "document_scanner"),
                java.util.Map.of("id", 8, "nom", "Neurologie", "nom_specialite", "Neurologie", "description", "Système nerveux et cerveau", "icone", "psychology"),
                java.util.Map.of("id", 9, "nom", "Chirurgie Générale", "nom_specialite", "Chirurgie Générale", "description", "Interventions et chirurgie", "icone", "healing"),
                java.util.Map.of("id", 10, "nom", "Pneumologie", "nom_specialite", "Pneumologie", "description", "Voies respiratoires et poumons", "icone", "air")
        );

        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>(standardSpecs);
        int currentId = 11;

        for (String spec : distinctSpecs) {
            boolean alreadyExists = result.stream().anyMatch(
                    s -> s.get("nom").toString().equalsIgnoreCase(spec)
            );
            if (!alreadyExists) {
                java.util.Map<String, Object> custom = new java.util.HashMap<>();
                custom.put("id", currentId++);
                custom.put("nom", spec);
                custom.put("nom_specialite", spec);
                custom.put("description", "Consultation en " + spec);
                custom.put("icone", "medical_services");
                result.add(custom);
            }
        }

        return result;
    }

    public MedecinDto getMedecinByUserId(UUID userId) {
        Medecin medecin = medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil médecin introuvable."));
        return toDto(medecin);
    }

    public MedecinDto toDto(Medecin medecin) {
        MedecinDto dto = new MedecinDto();
        dto.setId(medecin.getId());
        dto.setUserId(medecin.getUserId());
        dto.setVerified(Boolean.TRUE.equals(medecin.getIsVerified()));
        dto.setPhotoProfessionnelle(medecin.getPhotoProfessionnelle());
        dto.setBiographie(medecin.getBiographie());
        dto.setLanguesParlees(medecin.getLangues());
        dto.setTeleconsultationActive(Boolean.TRUE.equals(medecin.getTeleconsultationActive()));
        dto.setTarifConsultation(medecin.getTarifConsultation());
        dto.setDureeConsultationMinutes(medecin.getDureeConsultationMinutes() != null ? medecin.getDureeConsultationMinutes() : 30);
        dto.setStatutMedecin(medecin.getStatutMedecin() != null ? medecin.getStatutMedecin().name() : "");

        if (medecin.getOnmsReference() != null) {
            OnmsReference onms = medecin.getOnmsReference();
            dto.setNomComplet(onms.getPrenom() + " " + onms.getNom());
            dto.setSpecialite(onms.getSpecialite());
            dto.setEtablissement(onms.getEtablissement());
            dto.setRegion(onms.getRegion());
        }

        return dto;
    }

    // ==========================================
    // MÉTHODES D'ADMINISTRATION ONMS (CRUD)
    // ==========================================

    @Transactional
    public OnmsReference addOnmsReference(OnmsReference reference) {
        if (onmsReferenceRepository.findByNumeroOrdre(reference.getNumeroOrdre()).isPresent()) {
            throw new IllegalArgumentException("Un médecin avec ce numéro d'ordre existe déjà.");
        }
        reference.setDerniereSynchro(LocalDateTime.now());
        if (reference.getStatutProfessionnel() == null) {
            reference.setStatutProfessionnel(OnmsReference.StatutProfessionnel.ACTIF);
        }
        return onmsReferenceRepository.save(reference);
    }

    @Transactional
    public OnmsReference updateOnmsReference(String numeroOrdre, OnmsReference updateData) {
        OnmsReference existing = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable."));
        
        existing.setSection(updateData.getSection());
        existing.setNom(updateData.getNom());
        existing.setPrenom(updateData.getPrenom());
        existing.setSpecialite(updateData.getSpecialite());
        existing.setEtablissement(updateData.getEtablissement());
        existing.setRegion(updateData.getRegion());
        existing.setTelephone(updateData.getTelephone());
        existing.setStatutProfessionnel(updateData.getStatutProfessionnel());
        existing.setDerniereSynchro(LocalDateTime.now());
        
        return onmsReferenceRepository.save(existing);
    }

    @Transactional
    public void deleteOnmsReference(String numeroOrdre) {
        OnmsReference existing = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable."));
        // Au lieu de supprimer physiquement, on peut le radier
        existing.setStatutProfessionnel(OnmsReference.StatutProfessionnel.RADIE);
        existing.setDerniereSynchro(LocalDateTime.now());
        onmsReferenceRepository.save(existing);
    }

    public List<OnmsReference> getAllOnmsReferences() {
        return onmsReferenceRepository.findAll();
    }

    public List<MedecinDto> getAllPlatformMedecins() {
        return medecinRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public MedecinDto updateMedecinStatut(UUID medecinId, String statut, Boolean isVerified) {
        Medecin m = medecinRepository.findById(medecinId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinId));
        if (statut != null && !statut.trim().isEmpty()) {
            m.setStatutMedecin(Medecin.StatutMedecin.valueOf(statut.toUpperCase()));
        }
        if (isVerified != null) {
            m.setIsVerified(isVerified);
            if (isVerified && m.getVerifiedAt() == null) {
                m.setVerifiedAt(LocalDateTime.now());
            }
        }
        return toDto(medecinRepository.save(m));
    }
}
