// Déclaration du package Java : `com.diamyaraam.shared.dto`
package com.diamyaraam.shared.dto;

// Import de la classe `BigDecimal` (paquet java.math)
import java.math.BigDecimal;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * ====================================================================================================
 * DTO PARTAGÉ : REPRÉSENTATION DU PRATICIEN DE SANTÉ (MEDECIN DTO)
 * ====================================================================================================
 * 
 * 🎓 PATRON DTO (DATA TRANSFER OBJECT) POUR LA SOUTENANCE :
 * Pourquoi utiliser un MedecinDto plutôt que d'exposer directement l'entité JPA `Medecin` ?
 * 
 * 1. Sécurité & Confidentialité Médicale :
 *    Une entité de base de données peut contenir des colonnes sensibles ou des relations circulaires
 *    Hibernate complexes (Lazy loading). Le DTO filtre strictement les données exposées aux clients.
 * 
 * 2. Découplage Inter-Services (Contrat d'API Indépendant) :
 *    Ce DTO est partagé via `shared-lib` entre plusieurs microservices :
 *    - `medecin-service` : qui produit et alimente ce DTO à partir de sa base PostgreSQL.
 *    - `rdv-service` : qui consomme ce DTO pour afficher les détails du médecin lors de la prise de RDV.
 *    - `dossier-service` : pour identifier le praticien signataire des ordonnances et consultations.
 *    Ainsi, une modification de schéma SQL interne n'impacte pas les autres services.
 * 
 * 3. Indépendance Technologique :
 *    Champs sérialisables en JSON standardisés avec types stricts (UUID, BigDecimal pour la monnaie).
 * ====================================================================================================
 */
public class MedecinDto {

    /** Identifiant unique du profil médecin dans la table medecins */
    private UUID id;

    /** Identifiant du compte utilisateur associé dans auth-service (table users) */
    private UUID userId;

    /** Nom complet du praticien (ex: "Dr. Mamadou Diallo") */
    private String nomComplet;

    /** Spécialité médicale (ex: Cardiologie, Pédiatrie, Médecine Générale, Gynécologie) */
    private String specialite;

    /** Hôpital, clinique ou centre de santé d'attache (ex: "Hôpital Principal de Dakar") */
    private String etablissement;

    /** Région administrative d'exercice au Sénégal (ex: "Dakar", "Thiès", "Saint-Louis") */
    private String region;

    /** Indicateur de certification officielle par l'Ordre National des Médecins du Sénégal (ONDMS) */
    private boolean isVerified;

    /** URL ou chemin d'accès vers la photo officielle de profil professionnel */
    private String photoProfessionnelle;

    /** Résumé biographique et parcours professionnel du médecin */
    private String biographie;

    /** Langues de consultation (ex: "Français, Wolof, Anglais") */
    private String languesParlees;

    /** Indique si le médecin accepte les téléconsultations vidéo à distance */
    private boolean teleconsultationActive;

    /** 
     * Tarif de la consultation en Francs CFA (XOF).
     * Utilisation de BigDecimal pour éviter les erreurs d'arrondi sur les montants financiers.
     */
    private BigDecimal tarifConsultation;

    /** Durée moyenne d'un créneau de consultation en minutes (ex: 30 min) */
    private int dureeConsultationMinutes;

    /** Statut d'activité du praticien (ex: "ACTIF", "EN_CONGE", "SUSPENDU") */
    private String statutMedecin;

    /** Constructeur par défaut (désérialisation Jackson) */
    public MedecinDto() {}

    // ================================================================================================
    // ACCESSEURS ET MUTATEURS
    // ================================================================================================

    // Accesseur (getter) : renvoie la valeur de l'attribut `id`
    public UUID getId() { return id; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `id`
    public void setId(UUID id) { this.id = id; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `userId`
    public UUID getUserId() { return userId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `userId`
    public void setUserId(UUID userId) { this.userId = userId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nomComplet`
    public String getNomComplet() { return nomComplet; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nomComplet`
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `specialite`
    public String getSpecialite() { return specialite; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `specialite`
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `etablissement`
    public String getEtablissement() { return etablissement; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `etablissement`
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `region`
    public String getRegion() { return region; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `region`
    public void setRegion(String region) { this.region = region; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `isVerified`
    public boolean isVerified() { return isVerified; }
    // Méthode `setVerified` (publique) — paramètres : `verified` (booléen) ; retourne : aucune valeur ; intention : modifie (set verified)
    public void setVerified(boolean verified) { isVerified = verified; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `photoProfessionnelle`
    public String getPhotoProfessionnelle() { return photoProfessionnelle; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `photoProfessionnelle`
    public void setPhotoProfessionnelle(String photoProfessionnelle) { this.photoProfessionnelle = photoProfessionnelle; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `biographie`
    public String getBiographie() { return biographie; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `biographie`
    public void setBiographie(String biographie) { this.biographie = biographie; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `languesParlees`
    public String getLanguesParlees() { return languesParlees; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `languesParlees`
    public void setLanguesParlees(String languesParlees) { this.languesParlees = languesParlees; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `teleconsultationActive`
    public boolean isTeleconsultationActive() { return teleconsultationActive; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `teleconsultationActive`
    public void setTeleconsultationActive(boolean teleconsultationActive) { this.teleconsultationActive = teleconsultationActive; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `tarifConsultation`
    public BigDecimal getTarifConsultation() { return tarifConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `tarifConsultation`
    public void setTarifConsultation(BigDecimal tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dureeConsultationMinutes`
    public int getDureeConsultationMinutes() { return dureeConsultationMinutes; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dureeConsultationMinutes`
    public void setDureeConsultationMinutes(int dureeConsultationMinutes) { this.dureeConsultationMinutes = dureeConsultationMinutes; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `statutMedecin`
    public String getStatutMedecin() { return statutMedecin; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `statutMedecin`
    public void setStatutMedecin(String statutMedecin) { this.statutMedecin = statutMedecin; }
}

