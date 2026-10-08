// Déclaration du package Java : `com.diamyaraam.medecin.service`
package com.diamyaraam.medecin.service;

// Import de la classe `Medecin` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.Medecin;
// Import de la classe `OnmsReference` (paquet com.diamyaraam.medecin.entity)
import com.diamyaraam.medecin.entity.OnmsReference;
// Import de la classe `MedecinRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.MedecinRepository;
// Import de la classe `OnmsReferenceRepository` (paquet com.diamyaraam.medecin.repository)
import com.diamyaraam.medecin.repository.OnmsReferenceRepository;
// Import de la classe `MedecinDto` (paquet com.diamyaraam.shared.dto)
import com.diamyaraam.shared.dto.MedecinDto;
// Import de la classe `Service` (paquet org.springframework.stereotype)
import org.springframework.stereotype.Service;
// Import de la classe `Transactional` (paquet org.springframework.transaction.annotation)
import org.springframework.transaction.annotation.Transactional;


// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `List` (paquet java.util)
import java.util.List;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;
// Import de la classe `Collectors` (paquet java.util.stream)
import java.util.stream.Collectors;

// Composant de la couche métier (service Spring)
@Service
// Déclaration de la classe `MedecinService` (rôle : porte la logique métier)
public class MedecinService {

    // Attribut `medecinRepository` de type MedecinRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final MedecinRepository medecinRepository;
    // Attribut `onmsReferenceRepository` de type OnmsReferenceRepository — immuable, assigné une seule fois (souvent une dépendance injectée) [privée]
    private final OnmsReferenceRepository onmsReferenceRepository;

    // Constructeur de `MedecinService` — paramètres : `medecinRepository` (MedecinRepository), `onmsReferenceRepository` (OnmsReferenceRepository) (injection des dépendances par Spring)
    public MedecinService(MedecinRepository medecinRepository, OnmsReferenceRepository onmsReferenceRepository) {
        // Initialise l'attribut `medecinRepository` avec la valeur de medecinRepository
        this.medecinRepository = medecinRepository;
        // Initialise l'attribut `onmsReferenceRepository` avec la valeur de onmsReferenceRepository
        this.onmsReferenceRepository = onmsReferenceRepository;
    }

    /**
     * Verification ONMS (RM034, RM036)
     */
    @Transactional
    // Méthode `verifyAndRegisterMedecin` (publique) — paramètres : `userId` (identifiant UUID), `numeroOrdre` (chaîne de caractères) ; retourne : MedecinDto ; intention : vérifie (verify and register medecin)
    public MedecinDto verifyAndRegisterMedecin(UUID userId, String numeroOrdre) {
        // Déclare la variable `onms` (OnmsReference) initialisée avec le résultat de la requête findByNumeroOrdre exécutée via onmsReferenceRepository
        OnmsReference onms = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Numéro d'ordr…`
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable dans la base de référence."));

        // Condition : exécute le bloc suivant seulement si `!onms.estAutoriseAExercer()`
        if (!onms.estAutoriseAExercer()) {
            // Lève l'exception IllegalStateException avec le message « Le médecin rattaché à ce numéro n'est pas autorisé à exercer. »
            throw new IllegalStateException("Le médecin rattaché à ce numéro n'est pas autorisé à exercer.");
        }

        // Déclare la variable `medecin` (Medecin) initialisée avec le résultat de la requête findByUserId exécutée via medecinRepository
        Medecin medecin = medecinRepository.findByUserId(userId).orElseGet(Medecin::new);
        // Renseigne la propriété UserId de `medecin` avec la valeur de userId
        medecin.setUserId(userId);
        // Renseigne la propriété OnmsReference de `medecin` avec la valeur de onms
        medecin.setOnmsReference(onms);
        // Renseigne la propriété IsVerified de `medecin` avec le booléen vrai
        medecin.setIsVerified(true);
        // Renseigne la propriété VerifiedAt de `medecin` avec la date et l'heure courantes (LocalDateTime.now())
        medecin.setVerifiedAt(LocalDateTime.now());
        // Renseigne la propriété StatutMedecin de `medecin` avec la valeur de Medecin.StatutMedecin.ACTIF
        medecin.setStatutMedecin(Medecin.StatutMedecin.ACTIF);

        // Déclare la variable `saved` (Medecin) initialisée avec l'enregistrement en base de medecin via medecinRepository
        Medecin saved = medecinRepository.save(medecin);
        // Retourne `toDto(saved)`
        return toDto(saved);
    }

    // Méthode `lookupOnms` (publique) — paramètres : `numeroOrdre` (chaîne de caractères) ; retourne : OnmsReference
    public OnmsReference lookupOnms(String numeroOrdre) {
        // Retourne le résultat de la requête findByNumeroOrdre exécutée via onmsReferenceRepository
        return onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Numéro d'ordr…`
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable dans la base de référence."));
    }

    // Méthode `searchMedecins` (publique) — paramètres : `specialite` (chaîne de caractères), `region` (chaîne de caractères) ; retourne : liste de MedecinDto
    public List<MedecinDto> searchMedecins(String specialite, String region) {
        // Retourne `searchMedecins(specialite, region, null)`
        return searchMedecins(specialite, region, null);
    }

    // Méthode `normalize` (privée) — paramètres : `str` (chaîne de caractères) ; retourne : chaîne de caractères
    private String normalize(String str) {
        // Condition : exécute le bloc suivant seulement si `str == null) return "";`
        if (str == null) return "";
        // Retourne `java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)`
        return java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
                // Enchaînement : appelle `replaceAll("p{M}", "")`
                .replaceAll("\\p{M}", "")
                // Enchaînement : appelle `toLowerCase()`
                .toLowerCase()
                // Enchaînement : appelle `trim();`
                .trim();
    }

    // Méthode `searchMedecins` (publique) — paramètres : `specialite` (chaîne de caractères), `region` (chaîne de caractères), `search` (chaîne de caractères) ; retourne : liste de MedecinDto
    public List<MedecinDto> searchMedecins(String specialite, String region, String search) {
        // Déclare la variable `list` (liste de Medecin) initialisée avec le résultat de la requête findByStatutMedecinAndIsVerifiedTrue exécutée via medecinRepository
        List<Medecin> list = medecinRepository.findByStatutMedecinAndIsVerifiedTrue(Medecin.StatutMedecin.ACTIF);
        // Déclare la variable `hasSpec` (booléen) initialisée avec `specialite != null && !specialite.trim().isEmpty()`
        boolean hasSpec = specialite != null && !specialite.trim().isEmpty();
        // Déclare la variable `hasReg` (booléen) initialisée avec `region != null && !region.trim().isEmpty()`
        boolean hasReg = region != null && !region.trim().isEmpty();
        // Déclare la variable `hasSearch` (booléen) initialisée avec `search != null && !search.trim().isEmpty()`
        boolean hasSearch = search != null && !search.trim().isEmpty();

        // Condition : exécute le bloc suivant seulement si `hasSpec || hasReg || hasSearch`
        if (hasSpec || hasReg || hasSearch) {
            // Déclare la variable `specNorm` (chaîne de caractères) initialisée avec `hasSpec ? normalize(specialite) : ""`
            String specNorm = hasSpec ? normalize(specialite) : "";
            // Déclare la variable `regNorm` (chaîne de caractères) initialisée avec `hasReg ? normalize(region) : ""`
            String regNorm = hasReg ? normalize(region) : "";
            // Déclare la variable `searchNorm` (chaîne de caractères) initialisée avec `hasSearch ? normalize(search) : ""`
            String searchNorm = hasSearch ? normalize(search) : "";

            // Affecte à `list` `list.stream()`
            list = list.stream()
                    // Enchaînement : appelle `filter(m -> {`
                    .filter(m -> {
                        // Condition : exécute le bloc suivant seulement si `m.getOnmsReference() == null) return false;`
                        if (m.getOnmsReference() == null) return false;
                        // Déclare la variable `onms` (OnmsReference) initialisée avec la valeur de l'attribut OnmsReference de m
                        OnmsReference onms = m.getOnmsReference();

                        // Déclare la variable `docSpecNorm` (chaîne de caractères) initialisée avec `normalize(onms.getSpecialite())`
                        String docSpecNorm = normalize(onms.getSpecialite());
                        // Déclare la variable `matchSpec` (booléen) initialisée avec `!hasSpec || docSpecNorm.contains(specNorm) || specNorm.contains(docSpecNorm)`
                        boolean matchSpec = !hasSpec || docSpecNorm.contains(specNorm) || specNorm.contains(docSpecNorm);
                        
                        // Déclare la variable `docRegNorm` (chaîne de caractères) initialisée avec `normalize(onms.getRegion())`
                        String docRegNorm = normalize(onms.getRegion());
                        // Déclare la variable `matchReg` (booléen) initialisée avec `!hasReg || docRegNorm.contains(regNorm)`
                        boolean matchReg = !hasReg || docRegNorm.contains(regNorm);

                        // Déclare la variable `matchSearch` (booléen) initialisée avec `!hasSearch`
                        boolean matchSearch = !hasSearch;
                        // Condition : exécute le bloc suivant seulement si `hasSearch`
                        if (hasSearch) {
                            // Déclare la variable `fullName` (chaîne de caractères) initialisée avec `normalize((onms.getPrenom() != null ? onms.getPrenom() : "") + " " +`
                            String fullName = normalize((onms.getPrenom() != null ? onms.getPrenom() : "") + " " +
                                              // Suite de l'instruction précédente : (onms.getNom() != null ? onms.getNom() : ""));
                                              (onms.getNom() != null ? onms.getNom() : ""));
                            // Déclare la variable `numOrdre` (chaîne de caractères) initialisée avec `normalize(onms.getNumeroOrdre())`
                            String numOrdre = normalize(onms.getNumeroOrdre());
                            // Déclare la variable `etablissement` (chaîne de caractères) initialisée avec `normalize(onms.getEtablissement())`
                            String etablissement = normalize(onms.getEtablissement());
                            // Affecte à `matchSearch` `fullName.contains(searchNorm)`
                            matchSearch = fullName.contains(searchNorm)
                                    // Suite de l'expression (opérateur) : || numOrdre.contains(searchNorm)
                                    || numOrdre.contains(searchNorm)
                                    // Suite de l'expression (opérateur) : || docSpecNorm.contains(searchNorm)
                                    || docSpecNorm.contains(searchNorm)
                                    // Suite de l'expression (opérateur) : || etablissement.contains(searchNorm);
                                    || etablissement.contains(searchNorm);
                        }

                        // Retourne `matchSpec && matchReg && matchSearch`
                        return matchSpec && matchReg && matchSearch;
                    })
                    // Enchaînement : appelle `collect(Collectors.toList());`
                    .collect(Collectors.toList());
        }

        // Retourne `list.stream()`
        return list.stream()
                // Enchaînement : appelle `map(this::toDto)`
                .map(this::toDto)
                // Enchaînement : appelle `collect(Collectors.toList());`
                .collect(Collectors.toList());
    }

    // Méthode `getSpecialites` (publique) — sans paramètre ; retourne : liste de java.util.Map<String, Object> ; intention : récupère (get specialites)
    public List<java.util.Map<String, Object>> getSpecialites() {
        // Obtenir d'abord les spécialités distinctes des médecins enregistrés
        List<String> distinctSpecs = medecinRepository.findByStatutMedecinAndIsVerifiedTrue(Medecin.StatutMedecin.ACTIF)
                // Enchaînement : appelle `stream()`
                .stream()
                // Enchaînement : appelle `map(m -> m.getOnmsReference() != null ? m.getOnmsRefe…`
                .map(m -> m.getOnmsReference() != null ? m.getOnmsReference().getSpecialite() : null)
                // Enchaînement : appelle `filter(s -> s != null && !s.trim().isEmpty())`
                .filter(s -> s != null && !s.trim().isEmpty())
                // Enchaînement : appelle `map(String::trim)`
                .map(String::trim)
                // Enchaînement : appelle `distinct()`
                .distinct()
                // Enchaînement : appelle `collect(Collectors.toList());`
                .collect(Collectors.toList());

        // Déclare la variable `standardSpecs` (liste de java.util.Map<String, Object>) initialisée avec `List.of(`
        List<java.util.Map<String, Object>> standardSpecs = List.of(
                // Argument/valeur : `java.util.Map.of("id", 1, "nom", "Cardiologie", "nom_specialite", "Cardiologie"…`
                java.util.Map.of("id", 1, "nom", "Cardiologie", "nom_specialite", "Cardiologie", "description", "Maladies du cœur et des vaisseaux", "icone", "favorite"),
                // Argument/valeur : `java.util.Map.of("id", 2, "nom", "Pédiatrie", "nom_specialite", "Pédiatrie", "d…`
                java.util.Map.of("id", 2, "nom", "Pédiatrie", "nom_specialite", "Pédiatrie", "description", "Santé des enfants et des nourrissons", "icone", "child_care"),
                // Argument/valeur : `java.util.Map.of("id", 3, "nom", "Médecine Générale", "nom_specialite", "Médeci…`
                java.util.Map.of("id", 3, "nom", "Médecine Générale", "nom_specialite", "Médecine Générale", "description", "Consultations et soins primaires", "icone", "medical_services"),
                // Argument/valeur : `java.util.Map.of("id", 4, "nom", "Gynécologie Obstétrique", "nom_specialite", "…`
                java.util.Map.of("id", 4, "nom", "Gynécologie Obstétrique", "nom_specialite", "Gynécologie Obstétrique", "description", "Santé de la femme et suivi de grossesse", "icone", "pregnant_woman"),
                // Argument/valeur : `java.util.Map.of("id", 5, "nom", "Dermatologie", "nom_specialite", "Dermatologi…`
                java.util.Map.of("id", 5, "nom", "Dermatologie", "nom_specialite", "Dermatologie", "description", "Soins et pathologies de la peau", "icone", "healing"),
                // Argument/valeur : `java.util.Map.of("id", 6, "nom", "Ophtalmologie", "nom_specialite", "Ophtalmolo…`
                java.util.Map.of("id", 6, "nom", "Ophtalmologie", "nom_specialite", "Ophtalmologie", "description", "Santé des yeux et de la vision", "icone", "remove_red_eye"),
                // Argument/valeur : `java.util.Map.of("id", 7, "nom", "Radiologie", "nom_specialite", "Radiologie", …`
                java.util.Map.of("id", 7, "nom", "Radiologie", "nom_specialite", "Radiologie", "description", "Imagerie médicale et diagnostics", "icone", "document_scanner"),
                // Argument/valeur : `java.util.Map.of("id", 8, "nom", "Neurologie", "nom_specialite", "Neurologie", …`
                java.util.Map.of("id", 8, "nom", "Neurologie", "nom_specialite", "Neurologie", "description", "Système nerveux et cerveau", "icone", "psychology"),
                // Argument/valeur : `java.util.Map.of("id", 9, "nom", "Chirurgie Générale", "nom_specialite", "Chiru…`
                java.util.Map.of("id", 9, "nom", "Chirurgie Générale", "nom_specialite", "Chirurgie Générale", "description", "Interventions et chirurgie", "icone", "healing"),
                // Argument/valeur : `java.util.Map.of("id", 10, "nom", "Pneumologie", "nom_specialite", "Pneumologie…`
                java.util.Map.of("id", 10, "nom", "Pneumologie", "nom_specialite", "Pneumologie", "description", "Voies respiratoires et poumons", "icone", "air")
        );

        // Déclare la variable `result` (liste de java.util.Map<String, Object>) initialisée avec une nouvelle instance de java.util.ArrayList<>
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>(standardSpecs);
        // Déclare la variable `currentId` (entier) initialisée avec la valeur numérique 11
        int currentId = 11;

        // Boucle `for` : String spec : distinctSpecs)
        for (String spec : distinctSpecs) {
            // Déclare la variable `alreadyExists` (booléen) initialisée avec `result.stream().anyMatch(`
            boolean alreadyExists = result.stream().anyMatch(
                    // Fonction lambda : s -> s.get("nom").toString().equalsIgnoreCase(spec)
                    s -> s.get("nom").toString().equalsIgnoreCase(spec)
            );
            // Condition : exécute le bloc suivant seulement si `!alreadyExists`
            if (!alreadyExists) {
                // Instruction : java.util.Map<String, Object> custom = new java.util.HashMap<>();
                java.util.Map<String, Object> custom = new java.util.HashMap<>();
                // Appelle la méthode `put` sur `custom` : custom.put("id", currentId++);
                custom.put("id", currentId++);
                // Appelle la méthode `put` sur `custom` : custom.put("nom", spec);
                custom.put("nom", spec);
                // Appelle la méthode `put` sur `custom` : custom.put("nom_specialite", spec);
                custom.put("nom_specialite", spec);
                // Appelle la méthode `put` sur `custom` : custom.put("description", "Consultation en " + spec);
                custom.put("description", "Consultation en " + spec);
                // Appelle la méthode `put` sur `custom` : custom.put("icone", "medical_services");
                custom.put("icone", "medical_services");
                // Appelle la méthode `add` sur `result` : result.add(custom);
                result.add(custom);
            }
        }

        // Retourne la valeur de result
        return result;
    }

    // Méthode `getMedecinByUserId` (publique) — paramètres : `userId` (identifiant UUID) ; retourne : MedecinDto ; intention : récupère (get medecin by user id)
    public MedecinDto getMedecinByUserId(UUID userId) {
        // Déclare la variable `medecin` (Medecin) initialisée avec le résultat de la requête findByUserId exécutée via medecinRepository
        Medecin medecin = medecinRepository.findByUserId(userId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Profil médeci…`
                .orElseThrow(() -> new IllegalArgumentException("Profil médecin introuvable."));
        // Retourne `toDto(medecin)`
        return toDto(medecin);
    }

    // Méthode `toDto` (publique) — paramètres : `medecin` (Medecin) ; retourne : MedecinDto
    public MedecinDto toDto(Medecin medecin) {
        // Déclare la variable `dto` (MedecinDto) initialisée avec une nouvelle instance de MedecinDto
        MedecinDto dto = new MedecinDto();
        // Renseigne la propriété Id de `dto` avec la valeur de l'attribut Id de medecin
        dto.setId(medecin.getId());
        // Renseigne la propriété UserId de `dto` avec la valeur de l'attribut UserId de medecin
        dto.setUserId(medecin.getUserId());
        // Renseigne la propriété Verified de `dto` avec `Boolean.TRUE.equals(medecin.getIsVerified())`
        dto.setVerified(Boolean.TRUE.equals(medecin.getIsVerified()));
        // Renseigne la propriété PhotoProfessionnelle de `dto` avec la valeur de l'attribut PhotoProfessionnelle de medecin
        dto.setPhotoProfessionnelle(medecin.getPhotoProfessionnelle());
        // Renseigne la propriété Biographie de `dto` avec la valeur de l'attribut Biographie de medecin
        dto.setBiographie(medecin.getBiographie());
        // Renseigne la propriété LanguesParlees de `dto` avec la valeur de l'attribut Langues de medecin
        dto.setLanguesParlees(medecin.getLangues());
        // Renseigne la propriété TeleconsultationActive de `dto` avec `Boolean.TRUE.equals(medecin.getTeleconsultationActive())`
        dto.setTeleconsultationActive(Boolean.TRUE.equals(medecin.getTeleconsultationActive()));
        // Renseigne la propriété TarifConsultation de `dto` avec la valeur de l'attribut TarifConsultation de medecin
        dto.setTarifConsultation(medecin.getTarifConsultation());
        // Renseigne la propriété DureeConsultationMinutes de `dto` avec `medecin.getDureeConsultationMinutes() != null ? medecin.getDureeConsultationMin…`
        dto.setDureeConsultationMinutes(medecin.getDureeConsultationMinutes() != null ? medecin.getDureeConsultationMinutes() : 30);
        // Renseigne la propriété StatutMedecin de `dto` avec `medecin.getStatutMedecin() != null ? medecin.getStatutMedecin().name() : ""`
        dto.setStatutMedecin(medecin.getStatutMedecin() != null ? medecin.getStatutMedecin().name() : "");

        // Condition : exécute le bloc suivant seulement si `medecin.getOnmsReference() != null`
        if (medecin.getOnmsReference() != null) {
            // Déclare la variable `onms` (OnmsReference) initialisée avec la valeur de l'attribut OnmsReference de medecin
            OnmsReference onms = medecin.getOnmsReference();
            // Renseigne la propriété NomComplet de `dto` avec `onms.getPrenom() + " " + onms.getNom()`
            dto.setNomComplet(onms.getPrenom() + " " + onms.getNom());
            // Renseigne la propriété Specialite de `dto` avec la valeur de l'attribut Specialite de onms
            dto.setSpecialite(onms.getSpecialite());
            // Renseigne la propriété Etablissement de `dto` avec la valeur de l'attribut Etablissement de onms
            dto.setEtablissement(onms.getEtablissement());
            // Renseigne la propriété Region de `dto` avec la valeur de l'attribut Region de onms
            dto.setRegion(onms.getRegion());
        }

        // Retourne la valeur de dto
        return dto;
    }

    // ==========================================
    // MÉTHODES D'ADMINISTRATION ONMS (CRUD)
    // ==========================================

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `addOnmsReference` (publique) — paramètres : `reference` (OnmsReference) ; retourne : OnmsReference ; intention : ajoute (add onms reference)
    public OnmsReference addOnmsReference(OnmsReference reference) {
        // Condition : exécute le bloc suivant seulement si `onmsReferenceRepository.findByNumeroOrdre(reference.getNumeroOrdre()).isPresent()`
        if (onmsReferenceRepository.findByNumeroOrdre(reference.getNumeroOrdre()).isPresent()) {
            // Lève l'exception IllegalArgumentException avec le message « Un médecin avec ce numéro d'ordre existe déjà. »
            throw new IllegalArgumentException("Un médecin avec ce numéro d'ordre existe déjà.");
        }
        // Renseigne la propriété DerniereSynchro de `reference` avec la date et l'heure courantes (LocalDateTime.now())
        reference.setDerniereSynchro(LocalDateTime.now());
        // Condition : exécute le bloc suivant seulement si `reference.getStatutProfessionnel() == null`
        if (reference.getStatutProfessionnel() == null) {
            // Renseigne la propriété StatutProfessionnel de `reference` avec la valeur de OnmsReference.StatutProfessionnel.ACTIF
            reference.setStatutProfessionnel(OnmsReference.StatutProfessionnel.ACTIF);
        }
        // Retourne l'enregistrement en base de reference via onmsReferenceRepository
        return onmsReferenceRepository.save(reference);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `updateOnmsReference` (publique) — paramètres : `numeroOrdre` (chaîne de caractères), `updateData` (OnmsReference) ; retourne : OnmsReference ; intention : met à jour (update onms reference)
    public OnmsReference updateOnmsReference(String numeroOrdre, OnmsReference updateData) {
        // Déclare la variable `existing` (OnmsReference) initialisée avec le résultat de la requête findByNumeroOrdre exécutée via onmsReferenceRepository
        OnmsReference existing = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Numéro d'ordr…`
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable."));
        
        // Renseigne la propriété Section de `existing` avec la valeur de l'attribut Section de updateData
        existing.setSection(updateData.getSection());
        // Renseigne la propriété Nom de `existing` avec la valeur de l'attribut Nom de updateData
        existing.setNom(updateData.getNom());
        // Renseigne la propriété Prenom de `existing` avec la valeur de l'attribut Prenom de updateData
        existing.setPrenom(updateData.getPrenom());
        // Renseigne la propriété Specialite de `existing` avec la valeur de l'attribut Specialite de updateData
        existing.setSpecialite(updateData.getSpecialite());
        // Renseigne la propriété Etablissement de `existing` avec la valeur de l'attribut Etablissement de updateData
        existing.setEtablissement(updateData.getEtablissement());
        // Renseigne la propriété Region de `existing` avec la valeur de l'attribut Region de updateData
        existing.setRegion(updateData.getRegion());
        // Renseigne la propriété Telephone de `existing` avec la valeur de l'attribut Telephone de updateData
        existing.setTelephone(updateData.getTelephone());
        // Renseigne la propriété StatutProfessionnel de `existing` avec la valeur de l'attribut StatutProfessionnel de updateData
        existing.setStatutProfessionnel(updateData.getStatutProfessionnel());
        // Renseigne la propriété DerniereSynchro de `existing` avec la date et l'heure courantes (LocalDateTime.now())
        existing.setDerniereSynchro(LocalDateTime.now());
        
        // Retourne l'enregistrement en base de existing via onmsReferenceRepository
        return onmsReferenceRepository.save(existing);
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `deleteOnmsReference` (publique) — paramètres : `numeroOrdre` (chaîne de caractères) ; retourne : aucune valeur ; intention : supprime (delete onms reference)
    public void deleteOnmsReference(String numeroOrdre) {
        // Déclare la variable `existing` (OnmsReference) initialisée avec le résultat de la requête findByNumeroOrdre exécutée via onmsReferenceRepository
        OnmsReference existing = onmsReferenceRepository.findByNumeroOrdre(numeroOrdre)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Numéro d'ordr…`
                .orElseThrow(() -> new IllegalArgumentException("Numéro d'ordre ONMS introuvable."));
        // Au lieu de supprimer physiquement, on peut le radier
        existing.setStatutProfessionnel(OnmsReference.StatutProfessionnel.RADIE);
        // Renseigne la propriété DerniereSynchro de `existing` avec la date et l'heure courantes (LocalDateTime.now())
        existing.setDerniereSynchro(LocalDateTime.now());
        // Enregistre existing en base de données via onmsReferenceRepository
        onmsReferenceRepository.save(existing);
    }

    // Méthode `getAllOnmsReferences` (publique) — sans paramètre ; retourne : liste de OnmsReference ; intention : récupère (get all onms references)
    public List<OnmsReference> getAllOnmsReferences() {
        // Retourne le résultat de la requête findAll exécutée via onmsReferenceRepository
        return onmsReferenceRepository.findAll();
    }

    // Méthode `getAllPlatformMedecins` (publique) — sans paramètre ; retourne : liste de MedecinDto ; intention : récupère (get all platform medecins)
    public List<MedecinDto> getAllPlatformMedecins() {
        // Retourne le résultat de la requête findAll exécutée via medecinRepository
        return medecinRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // Exécute dans une transaction base de données (commit ou rollback automatique)
    @Transactional
    // Méthode `updateMedecinStatut` (publique) — paramètres : `medecinId` (identifiant UUID), `statut` (chaîne de caractères), `isVerified` (booléen) ; retourne : MedecinDto ; intention : met à jour (update medecin statut)
    public MedecinDto updateMedecinStatut(UUID medecinId, String statut, Boolean isVerified) {
        // Déclare la variable `m` (Medecin) initialisée avec le résultat de la requête findById exécutée via medecinRepository
        Medecin m = medecinRepository.findById(medecinId)
                // Enchaînement : appelle `orElseThrow(() -> new IllegalArgumentException("Médecin intro…`
                .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable : " + medecinId));
        // Condition : exécute le bloc suivant seulement si `statut != null && !statut.trim().isEmpty()`
        if (statut != null && !statut.trim().isEmpty()) {
            // Renseigne la propriété StatutMedecin de `m` avec `Medecin.StatutMedecin.valueOf(statut.toUpperCase())`
            m.setStatutMedecin(Medecin.StatutMedecin.valueOf(statut.toUpperCase()));
        }
        // Condition : exécute le bloc suivant seulement si `isVerified != null`
        if (isVerified != null) {
            // Renseigne la propriété IsVerified de `m` avec la valeur de isVerified
            m.setIsVerified(isVerified);
            // Condition : exécute le bloc suivant seulement si `isVerified && m.getVerifiedAt() == null`
            if (isVerified && m.getVerifiedAt() == null) {
                // Renseigne la propriété VerifiedAt de `m` avec la date et l'heure courantes (LocalDateTime.now())
                m.setVerifiedAt(LocalDateTime.now());
            }
        }
        // Retourne `toDto(medecinRepository.save(m))`
        return toDto(medecinRepository.save(m));
    }
}
