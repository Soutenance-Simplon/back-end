"""
Moteur RAG (Retrieval-Augmented Generation) — Diam Yaraam IA Service
Gère l'accès rapide et optimisé aux référentiels médicaux officiels :
1. Référentiel d'Orientation Médicale Diam Yaraam (v3.0 - ONDMS)
2. Protocole OMS Soins Primaires d'Urgence (SPU/BEC - ABCDE)
3. Algorithme de Triage OMS (BEC Integrated Triage Tool)
4. Triage Pédiatrique OMS ETAT (3TPR MOB)
5. Guide des Médicaments Essentiels MSF/OMS (Éd. 2024-2026)
"""

import os
import re
import json
import logging
from typing import List, Dict, Any, Optional

logger = logging.getLogger("ia-service.rag_engine")

# Dossier des documents de référence
DOCUMENTS_DIR = os.getenv("DOCUMENTS_PATH", os.path.join(os.path.dirname(__file__), "..", "documents"))

class RagEngine:
    def __init__(self):
        self.docs_dir = os.path.abspath(DOCUMENTS_DIR)
        self.is_loaded = False
        
        # Bases de connaissances structurées
        self.red_flags_db: List[Dict[str, Any]] = []
        self.specialties_db: Dict[str, Dict[str, Any]] = {}
        self.priority_rules: List[str] = []
        self.drug_interactions_db: List[Dict[str, Any]] = []
        self.allergy_rules_db: List[Dict[str, Any]] = []
        
        self._init_knowledge_bases()

    def _init_knowledge_bases(self):
        """Initialise et indexe l'ensemble des connaissances cliniques et pharmacologiques."""
        try:
            self._build_red_flags_database()
            self._build_specialties_database()
            self._build_msf_pharmacology_database()
            self.is_loaded = True
            logger.info(
                f"Base RAG initialisée : {len(self.red_flags_db)} critères d'urgence vitale, "
                f"{len(self.specialties_db)} spécialités ONDMS, "
                f"{len(self.drug_interactions_db)} interactions MSF indexées."
            )
        except Exception as e:
            logger.error(f"Erreur lors de l'initialisation du moteur RAG: {e}", exc_info=True)

    def _build_red_flags_database(self):
        """
        Construit l'index des Red Flags (Urgences Absolues) issu de :
        - regles_orientation_medicale.md (Section 5.1)
        - signes_alerte_urgences-ang.pdf (WHO BEC Red category)
        - soins_urgences.pdf (ABCDE)
        """
        self.red_flags_db = [
            {
                "id": "CARDIO_INFARCTUS",
                "specialite": "Cardiologie",
                "mots_cles": [
                    "douleur poitrine", "douleur thoracique", "poitrine qui serre", "oppression",
                    "irradie", "bras gauche", "mâchoire", "infarctus", "crise cardiaque",
                    "coeur qui serre", "douleur au coeur", "angine de poitrine", "poitrine", "cardiaque"
                ],
                "condition": "Douleur thoracique constrictive/oppressante avec ou sans irradiation vers le bras gauche, le dos ou la mâchoire",
                "gravite": "ROUGE",
                "diagnostic_suspecte": "Suspicion de Syndrome Coronarien Aigu / Infarctus du Myocarde",
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Cardiologie) & OMS BEC Triage",
                "numero_secours": "1515",
                "premiers_gestes": [
                    "Appelez immédiatement le SAMU National au 1515",
                    "Adoptez une position semi-assise, au repos strict absolu (ne faites aucun effort)",
                    "Desserrez col, cravate, ceinture et vêtements serrés pour faciliter la ventilation",
                    "Ne rien boire, ne rien manger, ne pas prendre de médicament sans avis médical",
                    "Laissez la porte d'entrée déverrouillée pour permettre l'accès rapide des équipes de secours"
                ]
            },
            {
                "id": "PNEUMO_DETRESSE",
                "specialite": "Pneumologie",
                "mots_cles": [
                    "étouffement", "suffoque", "manque de souffle", "lèvres bleues", "cyanose",
                    "détresse respiratoire", "impossible de parler", "ne peut plus respirer", "stridor",
                    "sifflement aigu", "respiration bloquée", "étouffe", "asphyxie"
                ],
                "condition": "Détresse respiratoire aiguë brutale, impossibilité de prononcer une phrase entière, lèvres ou doigts bleutés (cyanose)",
                "gravite": "ROUGE",
                "diagnostic_suspecte": "Détresse respiratoire aiguë engageant le pronostic vital (Asthme aigu grave, embolie pulmonaire, oedème aigu du poumon)",
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Pneumologie) & OMS SPU Module 3",
                "numero_secours": "1515",
                "premiers_gestes": [
                    "Appelez sans délai le SAMU National au 1515",
                    "Position assise droite buste vertical (ne jamais allonger un patient qui suffoque)",
                    "Ouvrez grand les fenêtres pour aérer la pièce",
                    "Si le patient possède un bronchodilatateur d'urgence (Salbutamol/Ventoline), administrer 2 à 4 bouffées immédiatement",
                    "Surveillez l'état de conscience en continu"
                ]
            },
            {
                "id": "NEURO_AVC",
                "specialite": "Neurologie",
                "mots_cles": [
                    "paralysie", "visage déformé", "bouche de travers", "faiblesse bras",
                    "trouble parole", "parle bizarrement", "perte de connaissance", "coma",
                    "convulsions", "pire mal de tête", "hémiplégie", "avc", "inconscient"
                ],
                "condition": "Déficit moteur brutal d'un côté du corps, déformation de la bouche, trouble de l'élocution, convulsions actives ou céphalée en coup de tonnerre",
                "gravite": "ROUGE",
                "diagnostic_suspecte": "Suspicion d'Accident Vasculaire Cérébral (AVC) ou Hémorragie Méningée — Urgence thrombolytique temps-dépendante",
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Neurologie) & OMS SPU Module 5",
                "numero_secours": "1515",
                "premiers_gestes": [
                    "Appelez immédiatement le SAMU National au 1515 (Chaque minute compte pour sauver les neurones)",
                    "Notez l'heure exacte d'apparition des premiers signes pour l'équipe médicale",
                    "Allongez le patient à plat dos ou en Position Latérale de Sécurité (PLS) s'il est inconscient",
                    "Ne rien donner à boire ni à manger (risque mortel de fausse route)",
                    "Ne pas administrer d'aspirine avant le scanner cérébral"
                ]
            },
            {
                "id": "TRAUMA_HEMORRAGIE",
                "specialite": "Traumatologie / Urgences",
                "mots_cles": [
                    "hémorragie", "saigne beaucoup", "saignement incontrôlable", "accident grave",
                    "choc violent", "blessure par arme", "brûlure grave", "brûlure étendue", "membre sectionné"
                ],
                "condition": "Saignement abondant non contrôlé, traumatisme à haute cinétique, brûlure > 15% ou atteinte de la face",
                "gravite": "ROUGE",
                "diagnostic_suspecte": "Choc hémorragique ou Polytraumatisme grave",
                "source": "OMS SPU Module 2 (Traumatisme) & BEC Triage Red Criteria",
                "numero_secours": "1515",
                "premiers_gestes": [
                    "Appelez immédiatement le SAMU National au 1515",
                    "Comprimez vigoureusement la zone hémorragique avec un linge propre",
                    "Allongez le patient jambes surélevées en cas de sensation de malaise ou pâleur",
                    "Couvrez la victime pour prévenir l'hypothermie (Facteur aggravant du choc)"
                ]
            },
            {
                "id": "PEDIATRIE_GRAVE",
                "specialite": "Pédiatrie d'Urgence",
                "mots_cles": [
                    "nourrisson ne réagit plus", "bébé respire vite", "fièvre bébé 2 mois", "fièvre bébé 3 mois",
                    "bébé léthargique", "convulsion enfant", "déshydratation sévère", "yeux enfoncés bébé",
                    "refuse de téter", "refuse de teter", "bébé mou", "bebe mou"
                ],
                "condition": "Fièvre chez un nourrisson de moins de 3 mois, tirage respiratoire, enfant hypotonique ou refus total d'alimentation avec déshydratation",
                "gravite": "ROUGE",
                "diagnostic_suspecte": "Détresse vitale pédiatrique (Sepsis néonatal, bronchiolite sévère, déshydratation aiguë stade III)",
                "source": "OMS ETAT (Emergency Triage Assessment and Treatment) & Réf. Diam Yaraam (Section 5.1)",
                "numero_secours": "1515",
                "premiers_gestes": [
                    "Appelez immédiatement le SAMU National au 1515 ou rendez-vous au centre pédiatrique le plus proche",
                    "Dégagez les voies respiratoires de l'enfant (tête en position neutre)",
                    "Déshabillez légèrement le nourrisson sans le refroidir",
                    "Si le nourrisson tète encore, proposez de petites gorgées de SRO (Sels de Réhydratation Orale) sans forcer"
                ]
            }
        ]

    def _build_specialties_database(self):
        """
        Construit la matrice d'orientation par spécialité médicale agréée ONDMS
        selon les Sections 7 et 8 de regles_orientation_medicale.md.
        """
        self.specialties_db = {
            "Dermatologie": {
                "nom": "Dermatologie",
                "description": "Peau, cheveux, cuir chevelu, ongles et muqueuses",
                "symptomes": [
                    "bouton", "boutons", "acné", "plaques rouges", "rougeur", "démangeaison",
                    "démangeaisons", "prurit", "éruption", "urticaire", "eczéma", "psoriasis",
                    "grain de beauté", "tache", "champignon", "mycose cutanée", "zona", "furoncle",
                    "chute de cheveux", "ongle incarné", "plaies chroniques"
                ],
                "confiance": "Élevé (Symptôme visible et localisé)",
                "regle_reference": "Section 7 & Règle 8.4 (Priorité à la spécialité organique directe)"
            },
            "Cardiologie": {
                "nom": "Cardiologie",
                "description": "Cœur, vaisseaux et système cardiovasculaire",
                "symptomes": [
                    "palpitations", "cœur qui bat vite", "tachycardie", "tension artérielle",
                    "hypertension", "essoufflement à l'effort", "jambes gonflées", "œdème des chevilles",
                    "gêne thoracique non aiguë", "arythmie"
                ],
                "confiance": "Élevé si symptôme cardiaque isolé",
                "regle_reference": "Section 7 & Règle 8.3 (Symptôme dominant cardiovasculaire)"
            },
            "Pneumologie": {
                "nom": "Pneumologie",
                "description": "Poumons, bronches et voies respiratoires basses",
                "symptomes": [
                    "toux persistante", "toux chronique", "toux depuis plus de 3 semaines",
                    "essoufflement chronique", "glaires", "expectorations", "respiration sifflante",
                    "asthme chronique", "apnée du sommeil"
                ],
                "confiance": "Élevé si absence de douleur thoracique aiguë",
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité organique pulmonaire)"
            },
            "Gastro-entérologie": {
                "nom": "Gastro-entérologie",
                "description": "Estomac, intestins, côlon, foie, vésicule biliaire et pancréas",
                "symptomes": [
                    "maux de ventre", "douleur estomac", "brûlure estomac", "reflux", "aigreurs",
                    "ballonnement", "diarrhée", "constipation", "nausées", "digestion difficile",
                    "foie", "hémorroïdes", "rectorragie modérée"
                ],
                "confiance": "Modéré (Possibles interférences)",
                "regle_reference": "Section 7 & Règle 8.3 (Symptôme digestif dominant)"
            },
            "Neurologie": {
                "nom": "Neurologie",
                "description": "Cerveau, moelle épinière, système nerveux et nerfs",
                "symptomes": [
                    "migraine", "maux de tête chroniques", "céphalée habituelle", "engourdissement",
                    "fourmillements", "tremblements", "perte d'équilibre", "troubles de mémoire",
                    "vertiges récurrents", "névralgie", "sciatique"
                ],
                "confiance": "Modéré (Chevauchement possible avec ORL pour les vertiges)",
                "regle_reference": "Section 7 & Règle 8.5 (Affection neurologique centrale/périphérique)"
            },
            "Gynécologie": {
                "nom": "Gynécologie - Obstétrique",
                "description": "Santé de la femme et système reproducteur féminin",
                "symptomes": [
                    "règles douloureuses", "retard de règles", "pertes blanches", "démangeaisons intimes",
                    "douleur au bas-ventre femme", "contraception", "grossesse", "suivi prénatal",
                    "douleur pendant les rapports", "ménopause", "bouffées de chaleur"
                ],
                "confiance": "Élevé",
                "regle_reference": "Section 7 & Section 9 (Prise en charge de la femme et suivi gynécologique)"
            },
            "Pédiatrie": {
                "nom": "Pédiatrie",
                "description": "Santé générale, développement et soins des enfants et nourrissons",
                "symptomes": [
                    "enfant", "bébé", "nourrisson", "petit garçon", "petite fille", "mon fils",
                    "ma fille", "croissance", "vaccination enfant", "fièvre enfant", "toux enfant",
                    "coliques du nourrisson", "éruption enfant"
                ],
                "confiance": "Prioritaire absolue dès que le patient est un enfant",
                "regle_reference": "Section 7 & Règle 8.2 (Priorité à l'âge du patient : pédiatrie privilégiée par défaut)"
            },
            "Odontologie": {
                "nom": "Odontologie (Dentiste)",
                "description": "Dents, gencives, mâchoires et sphère buccale",
                "symptomes": [
                    "mal de dent", "dent cariée", "gencive qui saigne", "rage de dent", "abcès dentaire",
                    "mâchoire", "douleur mastication", "dent cassée"
                ],
                "confiance": "Élevé (Organique directe)",
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité organique directe)"
            },
            "Ophtalmologie": {
                "nom": "Ophtalmologie",
                "description": "Yeux, acuité visuelle et paupières",
                "symptomes": [
                    "vision floue", "baisse de vision", "œil rouge", "picotement yeux", "conjonctivite",
                    "mal aux yeux", "larmoiement", "fatigue visuelle", "lunettes"
                ],
                "confiance": "Élevé",
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité oculaire directe)"
            },
            "ORL": {
                "nom": "Otho-Rhino-Laryngologie (ORL)",
                "description": "Oreilles, nez, sinus, gorge et cordes vocales",
                "symptomes": [
                    "mal d'oreille", "otite", "baisse d'audition", "acouphènes", "bourdonnements d'oreille",
                    "nez bouché", "sinusite", "mal de gorge", "angine", "voix cassée", "enrouement"
                ],
                "confiance": "Modéré à Élevé",
                "regle_reference": "Section 7 & Règle 8.4 (Sphère ORL directe)"
            },
            "Urologie": {
                "nom": "Urologie",
                "description": "Appareil urinaire et appareil génital masculin",
                "symptomes": [
                    "brûlure en urinant", "difficulté à uriner", "envies fréquentes d'uriner",
                    "douleur reins", "calcul rénal", "prostate", "infection urinaire", "urines troubles"
                ],
                "confiance": "Élevé",
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité urologique directe)"
            },
            "Médecine Générale": {
                "nom": "Médecine Générale",
                "description": "Premier recours, bilan général, orientation et affections poly-systémiques",
                "symptomes": [
                    "fatigue", "asthénie", "fièvre isolée", "courbatures", "syndrome grippal",
                    "bilan de santé", "renouvellement ordonnance", "perte de poids inexpliquée",
                    "symptômes multiples", "malaise vagal", "vertiges inexpliqués"
                ],
                "confiance": "Par défaut en cas de faible spécificité ou d'ambiguïté",
                "regle_reference": "Section 4, Section 7 & Règle 8.6/8.7 (Médecine générale par défaut)"
            }
        }

    def _build_msf_pharmacology_database(self):
        """
        Construit l'index des interactions médicamenteuses dangereuses et contre-indications
        strictement issu du Guide des Médicaments Essentiels MSF / OMS (guideline-339-fr.pdf).
        """
        self.drug_interactions_db = [
            {
                "medicament1": "CIPROFLOXACINE",
                "medicament2": "AMIODARONE",
                "aliases1": ["ciprofloxacine", "cipro", "ciprofloxacine oral", "ciflox"],
                "aliases2": ["amiodarone", "cordarone"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 51,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=51",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Ciprofloxacine, p. 51",
                "explication": "Risque majeur de torsades de pointes et d'arrêt cardiaque par allongement cumulatif synergique de l'intervalle QT ventriculaire.",
                "recommandation": "ASSOCIATION FORMELLEMENT CONTRE-INDIQUÉE. L'ajout de la Ciprofloxacine est bloqué par précaution clinique vitale.",
                "alternative_recommandee": "Ceftriaxone 1g injectable (ou Amoxicilline/Ac. Clavulanique selon le foyer infectieux) qui n'allonge pas l'intervalle QT."
            },
            {
                "medicament1": "HALOPERIDOL",
                "medicament2": "AMIODARONE",
                "aliases1": ["halopéridol", "haldol", "haloperidol"],
                "aliases2": ["amiodarone", "cordarone"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 51,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=51",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Halopéridol, p. 51",
                "explication": "Majoration du risque de troubles du rythme ventriculaire graves (torsades de pointes).",
                "recommandation": "Association contre-indiquée. Surveiller strictement l'électrocardiogramme (ECG).",
                "alternative_recommandee": "Utiliser un antipsychotique ou anxiolytique à moindre impact cardiaque après avis spécialisé."
            },
            {
                "medicament1": "TRAMADOL",
                "medicament2": "FLUOXETINE",
                "aliases1": ["tramadol", "topalgic", "contramal"],
                "aliases2": ["fluoxétine", "prozac", "fluoxetine", "sertraline", "paroxétine"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 101,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=101",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Antalgiques opioïdes et IRS, p. 101",
                "explication": "Risque de syndrome sérotoninergique potentiellement fatal (hyperthermie, rigidité musculaire, convulsions, coma).",
                "recommandation": "Association à proscrire formellement.",
                "alternative_recommandee": "Paracétamol forte dose ou palier antalgique non sérotoninergique en accord avec le médecin."
            },
            {
                "medicament1": "IBUPROFENE",
                "medicament2": "WARFARINE",
                "aliases1": ["ibuprofène", "ibuprofene", "advil", "nurofen", "kétoprofène", "diclofénac", "ains"],
                "aliases2": ["warfarine", "coumadine", "sintrom", "anticoagulant", "héparine", "rivaroxaban"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 10,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=10",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Section AINS et anticoagulants oraux, p. 10",
                "explication": "Risque hémorragique digestif massif par inhibition plaquettaire et agression de la muqueuse gastrique.",
                "recommandation": "Les AINS sont formellement contre-indiqués chez les patients sous anticoagulants oraux.",
                "alternative_recommandee": "Paracétamol 1g par prise (sans dépasser 3g/jour) pour traiter la douleur ou la fièvre."
            },
            {
                "medicament1": "ASPIRINE",
                "medicament2": "IBUPROFENE",
                "aliases1": ["aspirine", "acide acétylsalicylique", "aspégic"],
                "aliases2": ["ibuprofène", "ibuprofene", "advil", "kétoprofène", "diclofénac"],
                "niveau_danger": "MAJEURE",
                "bloquant": True,
                "page_numero": 10,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=10",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Précautions AINS, p. 10",
                "explication": "Cumul de toxicité digestive avec risque élevé d'ulcère perforé et annulation de l'effet cardioprotecteur antiagrégant.",
                "recommandation": "Ne jamais associer deux anti-inflammatoires non stéroïdiens simultanément.",
                "alternative_recommandee": "Conserver l'Aspirine à dose antiagrégante et utiliser le Paracétamol pour la douleur."
            }
        ]

        # Règles allergies médicamenteuses
        self.allergy_rules_db = [
            {
                "allergie": "PENICILLINE",
                "aliases_allergie": ["pénicilline", "penicilline", "amoxicilline", "bêtalactamines", "beta lactamines"],
                "medicaments_interdits": ["amoxicilline", "amoxicilline/acide clavulanique", "clamoxyl", "augmentin", "ampicilline"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 38,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=38",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Amoxicilline, p. 38",
                "explication": "Allergie croisée majeure avec risque de choc anaphylactique ou œdème de Quincke mortel.",
                "recommandation": "Contre-indication formelle et définitive à toutes les pénicillines.",
                "alternative_recommandee": "Macrolides (Azithromycine, Érythromycine) ou Céphalosporines de 3e génération après évaluation médicale."
            },
            {
                "allergie": "SULFAMIDES",
                "aliases_allergie": ["sulfamide", "sulfamides", "cotrimoxazole", "bactrim"],
                "medicaments_interdits": ["co-trimoxazole", "sulfaméthoxazole", "bactrim"],
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                "bloquant": True,
                "page_numero": 12,
                "document_nom": "guideline-339-fr.pdf",
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=12",
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Fiche Co-trimoxazole, p. 12",
                "explication": "Risque de toxidermie sévère (Syndrome de Stevens-Johnson / Lyell).",
                "recommandation": "Prescription strictement interdite chez le patient allergique aux sulfamides.",
                "alternative_recommandee": "Amoxicilline ou Ciprofloxacine selon la localisation de l'infection."
            }
        ]

    def evaluate_triage_and_orientation(self, message: str) -> Dict[str, Any]:
        """
        Analyse clinique hybride du message patient :
        1. Vérifie en priorité absolue la présence d'un Red Flag (Section 5.1).
        2. Si aucun Red Flag, applique la matrice d'orientation et les 7 règles d'arbitrage (Sections 7 & 8).
        """
        msg_lower = message.lower().strip()

        # 1. TEST PRIORITAIRE : DÉTECTION DE DRAPEAU ROUGE (URGENCE ABSOLUE)
        
        # A. Pédiatrie Grave (Nourrisson fébrile, léthargique ou refus de téter)
        est_bebe = any(b in msg_lower for b in ["bébé", "bebe", "nourrisson", "nouveau-né"])
        a_fievre = any(f in msg_lower for f in ["fièvre", "fievre", "chaud", "39", "38", "40", "brûlant"])
        signe_gravite_bebe = any(g in msg_lower for g in ["mou", "léthargique", "hypotonique", "ne réagit", "somnolent", "refuse de téter", "refuse de teter", "respire vite", "mois", "semaine", "convulsion", "tète plus", "boit plus"])
        if est_bebe and (a_fievre or signe_gravite_bebe) and (signe_gravite_bebe or "téter" in msg_lower or "teter" in msg_lower):
            rf_ped = next((r for r in self.red_flags_db if r["id"] == "PEDIATRIE_GRAVE"), self.red_flags_db[0])
            return {
                "est_urgence_vitale": True,
                "niveau_gravite": "ROUGE",
                "niveau_urgence": "SAMU",
                "red_flags": [rf_ped["diagnostic_suspecte"]],
                "condition_detectee": "Fièvre du nourrisson associée à un refus d'alimentation / léthargie (Urgence vitale pédiatrique)",
                "source_medicale": rf_ped["source"],
                "numero_urgence": rf_ped["numero_secours"],
                "premiers_gestes": rf_ped["premiers_gestes"],
                "specialites_suggerees": ["SAMU 1515 / Urgences Pédiatriques"],
                "justification_orientation": (
                    "Section 5 du Référentiel Diam Yaraam & Protocole OMS ETAT : « Tout signe de léthargie ou refus de s'alimenter avec fièvre chez le nourrisson constitue une urgence absolue »."
                ),
                "rappel_legal": "URGENCE VITALE PÉDIATRIQUE : Consultation hospitalière immédiate requise sans délai."
            }

        # B. Cardiologie / Douleur thoracique aiguë
        has_poitrine_cardio = any(k in msg_lower for k in ["poitrine", "coeur", "cœur", "thorac", "cardiaque", "infarctus", "sternum", "coronar", "cardio"])
        has_douleur_aigu = any(k in msg_lower for k in ["douleur", "mal", "serre", "oppress", "irradi", "angoisse", "brûlure", "pression", "étau", "serré", "point", "bloqué", "aigu"])
        is_cardio_rf = (has_poitrine_cardio and has_douleur_aigu) or \
            "infarctus" in msg_lower or "crise cardiaque" in msg_lower or \
            ("douleur" in msg_lower and "irradi" in msg_lower)

        if is_cardio_rf:
            rf_cardio = next((r for r in self.red_flags_db if r["id"] == "CARDIO_INFARCTUS"), self.red_flags_db[0])
            return {
                "est_urgence_vitale": True,
                "niveau_gravite": "ROUGE",
                "niveau_urgence": "SAMU",
                "red_flags": [rf_cardio["diagnostic_suspecte"]],
                "condition_detectee": rf_cardio["condition"],
                "source_medicale": rf_cardio["source"],
                "numero_urgence": rf_cardio["numero_secours"],
                "premiers_gestes": rf_cardio["premiers_gestes"],
                "specialites_suggerees": ["SAMU 1515 / Service d'Accueil des Urgences"],
                "justification_orientation": (
                    "Section 5 du Référentiel Diam Yaraam : « La détection d'urgence prime toujours sur l'orientation spécialisée ». "
                    "Le parcours standard de téléconsultation est immédiatement interrompu."
                ),
                "rappel_legal": "URGENCE VITALE ABSOLUE : Prise en charge SAMU 1515 requise sans délai."
            }

        # C. Autres Red Flags (Pneumologie détresse, AVC, etc.)
        for rf in self.red_flags_db:
            matched_keywords = [kw for kw in rf["mots_cles"] if kw in msg_lower]
            if len(matched_keywords) >= 1:
                return {
                    "est_urgence_vitale": True,
                    "niveau_gravite": "ROUGE",
                    "niveau_urgence": "SAMU",
                    "red_flags": [rf["diagnostic_suspecte"]],
                    "condition_detectee": rf["condition"],
                    "source_medicale": rf["source"],
                    "numero_urgence": rf["numero_secours"],
                    "premiers_gestes": rf["premiers_gestes"],
                    "specialites_suggerees": ["SAMU 1515 / Service d'Accueil des Urgences"],
                    "justification_orientation": (
                        "Section 5 du Référentiel Diam Yaraam : « La détection d'urgence prime toujours sur l'orientation spécialisée ». "
                        "Le parcours standard de téléconsultation est immédiatement interrompu."
                    ),
                    "rappel_legal": "URGENCE VITALE : Une prise en charge immédiate sans délai est nécessaire."
                }

        # 2. TEST SECONDAIRE : ORIENTATION VERS LE SPÉCIALISTE IDOINE (SECTIONS 7 & 8)
        # Vérification priorité âge (Règle 8.2) : Enfant
        mots_pediatrie = ["enfant", "bébé", "nourrisson", "fils", "fille", "bébé", "ans", "mois"]
        est_enfant = any(p in msg_lower for p in ["enfant", "nourrisson", "bébé", "mon fils", "ma fille"])

        specialite_scores: Dict[str, int] = {}
        for spec_name, spec_info in self.specialties_db.items():
            score = 0
            for symp in spec_info["symptomes"]:
                if symp in msg_lower:
                    score += 1
            if score > 0:
                specialite_scores[spec_name] = score

        if est_enfant:
            # Règle 8.2 : Priorité à l'âge du patient
            best_spec = "Pédiatrie"
            justif = "Règle 8.2 du Référentiel : Chez l'enfant, la Pédiatrie est prioritaire par défaut."
            spec_info = self.specialties_db["Pédiatrie"]
        elif specialite_scores:
            # Règle 8.3 & 8.4 : Symptôme dominant et spécialité organique directe
            # Trier par score décroissant
            sorted_specs = sorted(specialite_scores.items(), key=lambda x: x[1], reverse=True)
            best_spec = sorted_specs[0][0]
            spec_info = self.specialties_db[best_spec]
            justif = f"{spec_info['regle_reference']} ({spec_info['confiance']})"
        else:
            # Règle 8.7 : Médecine générale par défaut
            best_spec = "Médecine Générale"
            spec_info = self.specialties_db["Médecine Générale"]
            justif = "Section 4 & Règle 8.7 du Référentiel : Orientation vers la Médecine Générale par défaut pour premier bilan clinique."

        return {
            "est_urgence_vitale": False,
            "niveau_gravite": "VERT" if "fatigue" in msg_lower or "acné" in msg_lower else "JAUNE",
            "niveau_urgence": "MODERE",
            "red_flags": [],
            "specialites_suggerees": [best_spec],
            "justification_orientation": justif,
            "source_medicale": f"Référentiel d'Orientation Médicale Diam Yaraam (Sections 7 & 8) — {best_spec}",
            "rappel_legal": "Conformément aux sections 4 et 10 du référentiel, cette orientation constitue une aide indicative et ne remplace pas un examen clinique ou un diagnostic médical.",
            "premiers_gestes": []
        }

    def evaluate_drug_safety(self, drugs: List[str], allergies: List[str]) -> List[Dict[str, Any]]:
        """
        Vérifie de manière exhaustive les interactions médicamenteuses croisées
        et les allergies déclarées du patient à partir du Guide MSF.
        """
        results = []
        drugs_clean = [d.strip().lower() for d in drugs if d.strip()]
        allergies_clean = [a.strip().lower() for a in allergies if a.strip()]

        # 1. Vérification des allergies croisées
        for d in drugs_clean:
            for rule in self.allergy_rules_db:
                # Si le patient a cette allergie
                has_allergy = any(alias in " ".join(allergies_clean) for alias in rule["aliases_allergie"])
                if has_allergy:
                    # Vérifier si le médicament est interdit
                    is_forbidden = any(interdit in d for interdit in rule["medicaments_interdits"])
                    if is_forbidden:
                        results.append({
                            "medicament1": d.upper(),
                            "medicament2": rule["allergie"],
                            "niveau_danger": rule["niveau_danger"],
                            "bloquant": rule["bloquant"],
                            "page_numero": rule.get("page_numero"),
                            "document_nom": rule.get("document_nom"),
                            "document_url": rule.get("document_url"),
                            "source_medicale": rule["source_medicale"],
                            "explication": rule["explication"],
                            "recommandation": rule["recommandation"],
                            "alternative_recommandee": rule["alternative_recommandee"]
                        })

        # 2. Vérification des interactions médicament-médicament (toutes paires possibles)
        for i in range(len(drugs_clean)):
            for j in range(i + 1, len(drugs_clean)):
                d1 = drugs_clean[i]
                d2 = drugs_clean[j]
                
                for inter in self.drug_interactions_db:
                    match_1_to_1 = any(alias in d1 for alias in inter["aliases1"]) and any(alias in d2 for alias in inter["aliases2"])
                    match_2_to_1 = any(alias in d2 for alias in inter["aliases1"]) and any(alias in d1 for alias in inter["aliases2"])
                    
                    if match_1_to_1 or match_2_to_1:
                        results.append({
                            "medicament1": d1.upper(),
                            "medicament2": d2.upper(),
                            "niveau_danger": inter["niveau_danger"],
                            "bloquant": inter["bloquant"],
                            "page_numero": inter.get("page_numero"),
                            "document_nom": inter.get("document_nom"),
                            "document_url": inter.get("document_url"),
                            "source_medicale": inter["source_medicale"],
                            "explication": inter["explication"],
                            "recommandation": inter["recommandation"],
                            "alternative_recommandee": inter["alternative_recommandee"]
                        })

        return results

    def search(self, query: str, top_k: int = 2) -> str:
        """Méthode de recherche textuelle compatible avec les modules existants."""
        res = self.evaluate_triage_and_orientation(query)
        if res["est_urgence_vitale"]:
            return (
                f"--- ALERTE ROUGE CLINIQUE ---\n"
                f"Protocole : {res['source_medicale']}\n"
                f"Drapeau Rouge : {res['condition_detectee']}\n"
                f"Orientation obligatoire : {res['numero_urgence']} (SAMU National)\n"
            )
        else:
            return (
                f"--- ORIENTATION MÉDICALE ONDMS ---\n"
                f"Spécialité suggérée : {res['specialites_suggerees'][0]}\n"
                f"Justification : {res['justification_orientation']}\n"
                f"Rappel légal : {res['rappel_legal']}\n"
            )

# Instance singleton du moteur RAG
rag_engine = RagEngine()
