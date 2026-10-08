// Déclaration du package Java : `com.diamyaraam.rdv.dto`
package com.diamyaraam.rdv.dto;

// Import de la classe `RendezVous` (paquet com.diamyaraam.rdv.entity)
import com.diamyaraam.rdv.entity.RendezVous;

// Import de la classe `LocalDateTime` (paquet java.time)
import java.time.LocalDateTime;
// Import de la classe `UUID` (paquet java.util)
import java.util.UUID;

/**
 * Message WebSocket envoyé en push à tous les clients abonnés à /topic/rdv-updates.
 * Permet au patient et au médecin d'être notifiés en temps réel des créations et changements de statut.
 */
public class RdvUpdateEvent {

    // Attribut `rdvId` de type identifiant UUID [privée]
    private UUID rdvId;
    // Attribut `patientId` (identifiant du patient) de type identifiant UUID [privée]
    private UUID patientId;
    // Attribut `medecinId` (identifiant du médecin) de type identifiant UUID [privée]
    private UUID medecinId;
    // Attribut `nouveauStatut` de type chaîne de caractères [privée]
    private String nouveauStatut;
    // Attribut `timestamp` de type chaîne de caractères [privée]
    private String timestamp;
    // Attribut `type` (type) de type chaîne de caractères [privée]
    private String type; // "STATUT_CHANGE" | "CREATE" | "CANCELLED"
    // Attribut `motif` (motif) de type chaîne de caractères [privée]
    private String motif;
    // Attribut `dateHeure` (date et heure) de type chaîne de caractères [privée]
    private String dateHeure;
    // Attribut `typeConsultation` de type chaîne de caractères [privée]
    private String typeConsultation;
    // Attribut `montant` (montant en FCFA) de type nombre décimal [privée]
    private Double montant;
    // Attribut `paiementValide` de type booléen [privée]
    private Boolean paiementValide;

    // Constructeur de `RdvUpdateEvent` sans paramètre
    public RdvUpdateEvent() {}

    // Constructeur de `RdvUpdateEvent` — paramètres : `rdv` (RendezVous), `type` (chaîne de caractères) (injection des dépendances par Spring)
    public RdvUpdateEvent(RendezVous rdv, String type) {
        // Initialise l'attribut `rdvId` avec la valeur de l'attribut Id de rdv
        this.rdvId = rdv.getId();
        // Initialise l'attribut `patientId` avec la valeur de l'attribut PatientId de rdv
        this.patientId = rdv.getPatientId();
        // Initialise l'attribut `medecinId` avec la valeur de l'attribut MedecinId de rdv
        this.medecinId = rdv.getMedecinId();
        // Initialise l'attribut `nouveauStatut` avec `rdv.getStatut() != null ? rdv.getStatut().name() : null`
        this.nouveauStatut = rdv.getStatut() != null ? rdv.getStatut().name() : null;
        // Initialise l'attribut `timestamp` avec la date et l'heure courantes (LocalDateTime.now().toString())
        this.timestamp = LocalDateTime.now().toString();
        // Initialise l'attribut `type` avec la valeur de type
        this.type = type;
        // Initialise l'attribut `motif` avec la valeur de l'attribut Motif de rdv
        this.motif = rdv.getMotif();
        // Déclare la variable `dh` (date-heure) initialisée avec `rdv.getDateHeureConfirmee() != null ? rdv.getDateHeureConfirmee() : rdv.getDate…`
        LocalDateTime dh = rdv.getDateHeureConfirmee() != null ? rdv.getDateHeureConfirmee() : rdv.getDateHeureSouhaitee();
        // Initialise l'attribut `dateHeure` avec la date et l'heure courantes (dh != null ? dh.toString() : LocalDateTime.now().toString())
        this.dateHeure = dh != null ? dh.toString() : LocalDateTime.now().toString();
        // Initialise l'attribut `typeConsultation` avec `rdv.getTypeConsultation() != null ? rdv.getTypeConsultation().name() : "PRESENT…`
        this.typeConsultation = rdv.getTypeConsultation() != null ? rdv.getTypeConsultation().name() : "PRESENTIELLE";
        // Initialise l'attribut `montant` avec la valeur numérique 15000.0
        this.montant = 15000.0;
        // Initialise l'attribut `paiementValide` avec la valeur de l'attribut PaiementValide de rdv
        this.paiementValide = rdv.getPaiementValide();
    }

    // Accesseur (getter) : renvoie la valeur de l'attribut `rdvId`
    public UUID getRdvId() { return rdvId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `rdvId`
    public void setRdvId(UUID rdvId) { this.rdvId = rdvId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `patientId`
    public UUID getPatientId() { return patientId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `patientId`
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `medecinId`
    public UUID getMedecinId() { return medecinId; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `medecinId`
    public void setMedecinId(UUID medecinId) { this.medecinId = medecinId; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `nouveauStatut`
    public String getNouveauStatut() { return nouveauStatut; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `nouveauStatut`
    public void setNouveauStatut(String nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `timestamp`
    public String getTimestamp() { return timestamp; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `timestamp`
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `type`
    public String getType() { return type; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `type`
    public void setType(String type) { this.type = type; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `motif`
    public String getMotif() { return motif; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `motif`
    public void setMotif(String motif) { this.motif = motif; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `dateHeure`
    public String getDateHeure() { return dateHeure; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `dateHeure`
    public void setDateHeure(String dateHeure) { this.dateHeure = dateHeure; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `typeConsultation`
    public String getTypeConsultation() { return typeConsultation; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `typeConsultation`
    public void setTypeConsultation(String typeConsultation) { this.typeConsultation = typeConsultation; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `montant`
    public Double getMontant() { return montant; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `montant`
    public void setMontant(Double montant) { this.montant = montant; }

    // Accesseur (getter) : renvoie la valeur de l'attribut `paiementValide`
    public Boolean getPaiementValide() { return paiementValide; }
    // Mutateur (setter) : affecte la valeur reçue à l'attribut `paiementValide`
    public void setPaiementValide(Boolean paiementValide) { this.paiementValide = paiementValide; }
}
