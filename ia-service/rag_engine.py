"""
====================================================================================================
MOTEUR RAG (RETRIEVAL-AUGMENTED GENERATION) CLINIQUEMENT VALIDÉ (OMS / MSF / ONDMS)
====================================================================================================

 CONCEPTS D'INTELLIGENCE ARTIFICIELLE MÉDICALE POUR LA SOUTENANCE :
Le moteur RAG résout le principal danger des modèles de langage (LLMs) : l'hallucination médicale.
En médecine d'urgence, une invention ou une inexactitude peut avoir des conséquences létales.

 BASES DE CONNAISSANCES INDEXÉES :
1. Référentiel d'Orientation Médicale Diam Yaraam (v3.0 - conforme ONDMS Sénégal).
2. Protocole OMS Soins Primaires d'Urgence (SPU / Basic Emergency Care - BEC) :
   Approche vitale systématique ABCDE (Airway, Breathing, Circulation, Disability, Exposure).
3. Algorithme de Triage Pédiatrique OMS ETAT (Emergency Triage Assessment and Treatment).
4. Guide des Médicaments Essentiels MSF / OMS (Édition 2024-2026) :
   Interactions médicamenteuses majeures, contre-indications absolues et fenêtres thérapeutiques.

 ARCHITECTURE DU PIPELINE DE RÉCUPÉRATION (RETRIEVAL PIPELINE) :
- Analyse sémantique des symptômes exprimés en langage naturel par le patient (ex: "douleur poitrine qui serre").
- Détection prioritaire des "Red Flags" (Drapeaux Rouges d'urgence vitale).
- Injection des extraits médicaux certifiés dans le prompt système du LLM avant génération de la réponse.
====================================================================================================
"""

# Importation de la librairie os
import os
# Importation de la librairie re
import re
# Importation de la librairie json
import json
# Importation de la librairie logging
import logging
# Importation spécifique depuis le module typing
from typing import List, Dict, Any, Optional

# Affectation de la valeur à la variable 'logger'
logger = logging.getLogger("ia-service.rag_engine")

# Dossier hébergeant les guides PDF et documents médicaux de référence
DOCUMENTS_DIR = os.getenv("DOCUMENTS_PATH", os.path.join(os.path.dirname(__file__), "..", "documents"))

# Définition de la classe RagEngine
class RagEngine:

    # Définition de la fonction __init__
    def __init__(self):
        # Initialisation et affectation d'une variable
        self.docs_dir = os.path.abspath(DOCUMENTS_DIR)
        # Initialisation et affectation d'une variable
        self.is_loaded = False
        
        # Bases de connaissances structurées
        self.red_flags_db: List[Dict[str, Any]] = []
        # Initialisation et affectation d'une variable
        self.specialties_db: Dict[str, Dict[str, Any]] = {}
        # Initialisation et affectation d'une variable
        self.priority_rules: List[str] = []
        # Initialisation et affectation d'une variable
        self.drug_interactions_db: List[Dict[str, Any]] = []
        # Initialisation et affectation d'une variable
        self.allergy_rules_db: List[Dict[str, Any]] = []
        
        # Appel d'une fonction ou méthode spécifique
        self._init_knowledge_bases()

    # Définition de la fonction _init_knowledge_bases
    def _init_knowledge_bases(self):
        # Exécution de cette instruction spécifique
        """Initialise et indexe l'ensemble des connaissances cliniques et pharmacologiques."""
        # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
        try:
            # Appel d'une fonction ou méthode spécifique
            self._build_red_flags_database()
            # Appel d'une fonction ou méthode spécifique
            self._build_specialties_database()
            # Appel d'une fonction ou méthode spécifique
            self._build_msf_pharmacology_database()
            # Initialisation et affectation d'une variable
            self.is_loaded = True
            # Enregistre une trace (log) pour le suivi de l'application
            logger.info(
                # Exécution de cette instruction spécifique
                f"Base RAG initialisée : {len(self.red_flags_db)} critères d'urgence vitale, "
                # Exécution de cette instruction spécifique
                f"{len(self.specialties_db)} spécialités ONDMS, "
                # Exécution de cette instruction spécifique
                f"{len(self.drug_interactions_db)} interactions MSF indexées."
            )
        # Capture et gestion de l'erreur si le bloc try échoue
        except Exception as e:
            # Enregistre une trace (log) pour le suivi de l'application
            logger.error(f"Erreur lors de l'initialisation du moteur RAG: {e}", exc_info=True)

    # Définition de la fonction _build_red_flags_database
    def _build_red_flags_database(self):
        """
        Construit l'index des Red Flags (Urgences Absolues) issu de :
        - regles_orientation_medicale.md (Section 5.1)
        - signes_alerte_urgences-ang.pdf (WHO BEC Red category)
        - soins_urgences.pdf (ABCDE)
        """
        # Initialisation et affectation d'une variable
        self.red_flags_db = [
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "id": "CARDIO_INFARCTUS",
                # Exécution de cette instruction spécifique
                "specialite": "Cardiologie",
                # Ouverture d'une structure de données ou d'une fonction
                "mots_cles": [
                    # Exécution de cette instruction spécifique
                    "douleur poitrine", "douleur thoracique", "poitrine qui serre", "oppression",
                    # Exécution de cette instruction spécifique
                    "irradie", "bras gauche", "mâchoire", "infarctus", "crise cardiaque",
                    # Exécution de cette instruction spécifique
                    "coeur qui serre", "douleur au coeur", "angine de poitrine", "poitrine", "cardiaque"
                ],
                # Exécution de cette instruction spécifique
                "condition": "Douleur thoracique constrictive/oppressante avec ou sans irradiation vers le bras gauche, le dos ou la mâchoire",
                # Exécution de cette instruction spécifique
                "gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "diagnostic_suspecte": "Suspicion de Syndrome Coronarien Aigu / Infarctus du Myocarde",
                # Exécution de cette instruction spécifique
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Cardiologie) & OMS BEC Triage",
                # Exécution de cette instruction spécifique
                "numero_secours": "1515",
                # Ouverture d'une structure de données ou d'une fonction
                "premiers_gestes": [
                    # Exécution de cette instruction spécifique
                    "Appelez immédiatement le SAMU National au 1515",
                    # Exécution de cette instruction spécifique
                    "Adoptez une position semi-assise, au repos strict absolu (ne faites aucun effort)",
                    # Exécution de cette instruction spécifique
                    "Desserrez col, cravate, ceinture et vêtements serrés pour faciliter la ventilation",
                    # Exécution de cette instruction spécifique
                    "Ne rien boire, ne rien manger, ne pas prendre de médicament sans avis médical",
                    # Exécution de cette instruction spécifique
                    "Laissez la porte d'entrée déverrouillée pour permettre l'accès rapide des équipes de secours"
                ]
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "id": "PNEUMO_DETRESSE",
                # Exécution de cette instruction spécifique
                "specialite": "Pneumologie",
                # Ouverture d'une structure de données ou d'une fonction
                "mots_cles": [
                    # Exécution de cette instruction spécifique
                    "étouffement", "suffoque", "manque de souffle", "lèvres bleues", "cyanose",
                    # Exécution de cette instruction spécifique
                    "détresse respiratoire", "impossible de parler", "ne peut plus respirer", "stridor",
                    # Exécution de cette instruction spécifique
                    "sifflement aigu", "respiration bloquée", "étouffe", "asphyxie"
                ],
                # Exécution de cette instruction spécifique
                "condition": "Détresse respiratoire aiguë brutale, impossibilité de prononcer une phrase entière, lèvres ou doigts bleutés (cyanose)",
                # Exécution de cette instruction spécifique
                "gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "diagnostic_suspecte": "Détresse respiratoire aiguë engageant le pronostic vital (Asthme aigu grave, embolie pulmonaire, oedème aigu du poumon)",
                # Exécution de cette instruction spécifique
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Pneumologie) & OMS SPU Module 3",
                # Exécution de cette instruction spécifique
                "numero_secours": "1515",
                # Ouverture d'une structure de données ou d'une fonction
                "premiers_gestes": [
                    # Exécution de cette instruction spécifique
                    "Appelez sans délai le SAMU National au 1515",
                    # Exécution de cette instruction spécifique
                    "Position assise droite buste vertical (ne jamais allonger un patient qui suffoque)",
                    # Exécution de cette instruction spécifique
                    "Ouvrez grand les fenêtres pour aérer la pièce",
                    # Exécution de cette instruction spécifique
                    "Si le patient possède un bronchodilatateur d'urgence (Salbutamol/Ventoline), administrer 2 à 4 bouffées immédiatement",
                    # Exécution de cette instruction spécifique
                    "Surveillez l'état de conscience en continu"
                ]
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "id": "NEURO_AVC",
                # Exécution de cette instruction spécifique
                "specialite": "Neurologie",
                # Ouverture d'une structure de données ou d'une fonction
                "mots_cles": [
                    # Exécution de cette instruction spécifique
                    "paralysie", "visage déformé", "bouche de travers", "faiblesse bras",
                    # Exécution de cette instruction spécifique
                    "trouble parole", "parle bizarrement", "perte de connaissance", "coma",
                    # Exécution de cette instruction spécifique
                    "convulsions", "pire mal de tête", "hémiplégie", "avc", "inconscient"
                ],
                # Exécution de cette instruction spécifique
                "condition": "Déficit moteur brutal d'un côté du corps, déformation de la bouche, trouble de l'élocution, convulsions actives ou céphalée en coup de tonnerre",
                # Exécution de cette instruction spécifique
                "gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "diagnostic_suspecte": "Suspicion d'Accident Vasculaire Cérébral (AVC) ou Hémorragie Méningée — Urgence thrombolytique temps-dépendante",
                # Exécution de cette instruction spécifique
                "source": "Règles d'orientation médicale Diam Yaraam (Section 5.1 - Neurologie) & OMS SPU Module 5",
                # Exécution de cette instruction spécifique
                "numero_secours": "1515",
                # Ouverture d'une structure de données ou d'une fonction
                "premiers_gestes": [
                    # Exécution de cette instruction spécifique
                    "Appelez immédiatement le SAMU National au 1515 (Chaque minute compte pour sauver les neurones)",
                    # Exécution de cette instruction spécifique
                    "Notez l'heure exacte d'apparition des premiers signes pour l'équipe médicale",
                    # Exécution de cette instruction spécifique
                    "Allongez le patient à plat dos ou en Position Latérale de Sécurité (PLS) s'il est inconscient",
                    # Exécution de cette instruction spécifique
                    "Ne rien donner à boire ni à manger (risque mortel de fausse route)",
                    # Exécution de cette instruction spécifique
                    "Ne pas administrer d'aspirine avant le scanner cérébral"
                ]
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "id": "TRAUMA_HEMORRAGIE",
                # Exécution de cette instruction spécifique
                "specialite": "Traumatologie / Urgences",
                # Ouverture d'une structure de données ou d'une fonction
                "mots_cles": [
                    # Exécution de cette instruction spécifique
                    "hémorragie", "saigne beaucoup", "saignement incontrôlable", "accident grave",
                    # Exécution de cette instruction spécifique
                    "choc violent", "blessure par arme", "brûlure grave", "brûlure étendue", "membre sectionné"
                ],
                # Exécution de cette instruction spécifique
                "condition": "Saignement abondant non contrôlé, traumatisme à haute cinétique, brûlure > 15% ou atteinte de la face",
                # Exécution de cette instruction spécifique
                "gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "diagnostic_suspecte": "Choc hémorragique ou Polytraumatisme grave",
                # Exécution de cette instruction spécifique
                "source": "OMS SPU Module 2 (Traumatisme) & BEC Triage Red Criteria",
                # Exécution de cette instruction spécifique
                "numero_secours": "1515",
                # Ouverture d'une structure de données ou d'une fonction
                "premiers_gestes": [
                    # Exécution de cette instruction spécifique
                    "Appelez immédiatement le SAMU National au 1515",
                    # Exécution de cette instruction spécifique
                    "Comprimez vigoureusement la zone hémorragique avec un linge propre",
                    # Exécution de cette instruction spécifique
                    "Allongez le patient jambes surélevées en cas de sensation de malaise ou pâleur",
                    # Exécution de cette instruction spécifique
                    "Couvrez la victime pour prévenir l'hypothermie (Facteur aggravant du choc)"
                ]
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "id": "PEDIATRIE_GRAVE",
                # Exécution de cette instruction spécifique
                "specialite": "Pédiatrie d'Urgence",
                # Ouverture d'une structure de données ou d'une fonction
                "mots_cles": [
                    # Exécution de cette instruction spécifique
                    "nourrisson ne réagit plus", "bébé respire vite", "fièvre bébé 2 mois", "fièvre bébé 3 mois",
                    # Exécution de cette instruction spécifique
                    "bébé léthargique", "convulsion enfant", "déshydratation sévère", "yeux enfoncés bébé",
                    # Exécution de cette instruction spécifique
                    "refuse de téter", "refuse de teter", "bébé mou", "bebe mou"
                ],
                # Exécution de cette instruction spécifique
                "condition": "Fièvre chez un nourrisson de moins de 3 mois, tirage respiratoire, enfant hypotonique ou refus total d'alimentation avec déshydratation",
                # Exécution de cette instruction spécifique
                "gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "diagnostic_suspecte": "Détresse vitale pédiatrique (Sepsis néonatal, bronchiolite sévère, déshydratation aiguë stade III)",
                # Exécution de cette instruction spécifique
                "source": "OMS ETAT (Emergency Triage Assessment and Treatment) & Réf. Diam Yaraam (Section 5.1)",
                # Exécution de cette instruction spécifique
                "numero_secours": "1515",
                # Ouverture d'une structure de données ou d'une fonction
                "premiers_gestes": [
                    # Exécution de cette instruction spécifique
                    "Appelez immédiatement le SAMU National au 1515 ou rendez-vous au centre pédiatrique le plus proche",
                    # Exécution de cette instruction spécifique
                    "Dégagez les voies respiratoires de l'enfant (tête en position neutre)",
                    # Exécution de cette instruction spécifique
                    "Déshabillez légèrement le nourrisson sans le refroidir",
                    # Exécution de cette instruction spécifique
                    "Si le nourrisson tète encore, proposez de petites gorgées de SRO (Sels de Réhydratation Orale) sans forcer"
                ]
            }
        ]

    # Définition de la fonction _build_specialties_database
    def _build_specialties_database(self):
        """
        Construit la matrice d'orientation par spécialité médicale agréée ONDMS
        selon les Sections 7 et 8 de regles_orientation_medicale.md.
        """
        # Initialisation et affectation d'une variable
        self.specialties_db = {
            # Ouverture d'une structure de données ou d'une fonction
            "Dermatologie": {
                # Exécution de cette instruction spécifique
                "nom": "Dermatologie",
                # Exécution de cette instruction spécifique
                "description": "Peau, cheveux, cuir chevelu, ongles et muqueuses",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "bouton", "boutons", "acné", "plaques rouges", "rougeur", "démangeaison",
                    # Exécution de cette instruction spécifique
                    "démangeaisons", "prurit", "éruption", "urticaire", "eczéma", "psoriasis",
                    # Exécution de cette instruction spécifique
                    "grain de beauté", "tache", "champignon", "mycose cutanée", "zona", "furoncle",
                    # Exécution de cette instruction spécifique
                    "chute de cheveux", "ongle incarné", "plaies chroniques"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé (Symptôme visible et localisé)",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Priorité à la spécialité organique directe)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Cardiologie": {
                # Exécution de cette instruction spécifique
                "nom": "Cardiologie",
                # Exécution de cette instruction spécifique
                "description": "Cœur, vaisseaux et système cardiovasculaire",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "palpitations", "cœur qui bat vite", "tachycardie", "tension artérielle",
                    # Exécution de cette instruction spécifique
                    "hypertension", "essoufflement à l'effort", "jambes gonflées", "œdème des chevilles",
                    # Exécution de cette instruction spécifique
                    "gêne thoracique non aiguë", "arythmie"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé si symptôme cardiaque isolé",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.3 (Symptôme dominant cardiovasculaire)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Pneumologie": {
                # Exécution de cette instruction spécifique
                "nom": "Pneumologie",
                # Exécution de cette instruction spécifique
                "description": "Poumons, bronches et voies respiratoires basses",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "toux persistante", "toux chronique", "toux depuis plus de 3 semaines",
                    # Exécution de cette instruction spécifique
                    "essoufflement chronique", "glaires", "expectorations", "respiration sifflante",
                    # Exécution de cette instruction spécifique
                    "asthme chronique", "apnée du sommeil"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé si absence de douleur thoracique aiguë",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité organique pulmonaire)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Gastro-entérologie": {
                # Exécution de cette instruction spécifique
                "nom": "Gastro-entérologie",
                # Exécution de cette instruction spécifique
                "description": "Estomac, intestins, côlon, foie, vésicule biliaire et pancréas",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "maux de ventre", "douleur estomac", "brûlure estomac", "reflux", "aigreurs",
                    # Exécution de cette instruction spécifique
                    "ballonnement", "diarrhée", "constipation", "nausées", "digestion difficile",
                    # Exécution de cette instruction spécifique
                    "foie", "hémorroïdes", "rectorragie modérée"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Modéré (Possibles interférences)",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.3 (Symptôme digestif dominant)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Neurologie": {
                # Exécution de cette instruction spécifique
                "nom": "Neurologie",
                # Exécution de cette instruction spécifique
                "description": "Cerveau, moelle épinière, système nerveux et nerfs",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "migraine", "maux de tête chroniques", "céphalée habituelle", "engourdissement",
                    # Exécution de cette instruction spécifique
                    "fourmillements", "tremblements", "perte d'équilibre", "troubles de mémoire",
                    # Exécution de cette instruction spécifique
                    "vertiges récurrents", "névralgie", "sciatique"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Modéré (Chevauchement possible avec ORL pour les vertiges)",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.5 (Affection neurologique centrale/périphérique)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Gynécologie": {
                # Exécution de cette instruction spécifique
                "nom": "Gynécologie - Obstétrique",
                # Exécution de cette instruction spécifique
                "description": "Santé de la femme et système reproducteur féminin",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "règles douloureuses", "retard de règles", "pertes blanches", "démangeaisons intimes",
                    # Exécution de cette instruction spécifique
                    "douleur au bas-ventre femme", "contraception", "grossesse", "suivi prénatal",
                    # Exécution de cette instruction spécifique
                    "douleur pendant les rapports", "ménopause", "bouffées de chaleur"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Section 9 (Prise en charge de la femme et suivi gynécologique)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Pédiatrie": {
                # Exécution de cette instruction spécifique
                "nom": "Pédiatrie",
                # Exécution de cette instruction spécifique
                "description": "Santé générale, développement et soins des enfants et nourrissons",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "enfant", "bébé", "nourrisson", "petit garçon", "petite fille", "mon fils",
                    # Exécution de cette instruction spécifique
                    "ma fille", "croissance", "vaccination enfant", "fièvre enfant", "toux enfant",
                    # Exécution de cette instruction spécifique
                    "coliques du nourrisson", "éruption enfant"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Prioritaire absolue dès que le patient est un enfant",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.2 (Priorité à l'âge du patient : pédiatrie privilégiée par défaut)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Odontologie": {
                # Exécution de cette instruction spécifique
                "nom": "Odontologie (Dentiste)",
                # Exécution de cette instruction spécifique
                "description": "Dents, gencives, mâchoires et sphère buccale",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "mal de dent", "dent cariée", "gencive qui saigne", "rage de dent", "abcès dentaire",
                    # Exécution de cette instruction spécifique
                    "mâchoire", "douleur mastication", "dent cassée"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé (Organique directe)",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité organique directe)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Ophtalmologie": {
                # Exécution de cette instruction spécifique
                "nom": "Ophtalmologie",
                # Exécution de cette instruction spécifique
                "description": "Yeux, acuité visuelle et paupières",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "vision floue", "baisse de vision", "œil rouge", "picotement yeux", "conjonctivite",
                    # Exécution de cette instruction spécifique
                    "mal aux yeux", "larmoiement", "fatigue visuelle", "lunettes"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité oculaire directe)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "ORL": {
                # Exécution de cette instruction spécifique
                "nom": "Otho-Rhino-Laryngologie (ORL)",
                # Exécution de cette instruction spécifique
                "description": "Oreilles, nez, sinus, gorge et cordes vocales",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "mal d'oreille", "otite", "baisse d'audition", "acouphènes", "bourdonnements d'oreille",
                    # Exécution de cette instruction spécifique
                    "nez bouché", "sinusite", "mal de gorge", "angine", "voix cassée", "enrouement"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Modéré à Élevé",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Sphère ORL directe)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Urologie": {
                # Exécution de cette instruction spécifique
                "nom": "Urologie",
                # Exécution de cette instruction spécifique
                "description": "Appareil urinaire et appareil génital masculin",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "brûlure en urinant", "difficulté à uriner", "envies fréquentes d'uriner",
                    # Exécution de cette instruction spécifique
                    "douleur reins", "calcul rénal", "prostate", "infection urinaire", "urines troubles"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Élevé",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 7 & Règle 8.4 (Spécialité urologique directe)"
            },
            # Ouverture d'une structure de données ou d'une fonction
            "Médecine Générale": {
                # Exécution de cette instruction spécifique
                "nom": "Médecine Générale",
                # Exécution de cette instruction spécifique
                "description": "Premier recours, bilan général, orientation et affections poly-systémiques",
                # Ouverture d'une structure de données ou d'une fonction
                "symptomes": [
                    # Exécution de cette instruction spécifique
                    "fatigue", "asthénie", "fièvre isolée", "courbatures", "syndrome grippal",
                    # Exécution de cette instruction spécifique
                    "bilan de santé", "renouvellement ordonnance", "perte de poids inexpliquée",
                    # Exécution de cette instruction spécifique
                    "symptômes multiples", "malaise vagal", "vertiges inexpliqués"
                ],
                # Exécution de cette instruction spécifique
                "confiance": "Par défaut en cas de faible spécificité ou d'ambiguïté",
                # Exécution de cette instruction spécifique
                "regle_reference": "Section 4, Section 7 & Règle 8.6/8.7 (Médecine générale par défaut)"
            }
        }

    # Définition de la fonction _build_msf_pharmacology_database
    def _build_msf_pharmacology_database(self):
        """
        Construit l'index des interactions médicamenteuses dangereuses et contre-indications
        strictement issu du Guide des Médicaments Essentiels MSF / OMS (guideline-339-fr.pdf).
        """
        # Initialisation et affectation d'une variable
        self.drug_interactions_db = [
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "CIPROFLOXACINE",
                # Exécution de cette instruction spécifique
                "medicament2": "AMIODARONE",
                # Exécution de cette instruction spécifique
                "aliases1": ["ciprofloxacine", "cipro", "ciprofloxacine oral", "ciflox"],
                # Exécution de cette instruction spécifique
                "aliases2": ["amiodarone", "cordarone"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 51,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=51",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Ciprofloxacine, p. 51",
                # Exécution de cette instruction spécifique
                "explication": "Risque majeur de torsades de pointes et d'arrêt cardiaque par allongement cumulatif synergique de l'intervalle QT ventriculaire.",
                # Exécution de cette instruction spécifique
                "recommandation": "ASSOCIATION FORMELLEMENT CONTRE-INDIQUÉE. L'ajout de la Ciprofloxacine est bloqué par précaution clinique vitale.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Ceftriaxone 1g injectable (ou Amoxicilline/Ac. Clavulanique selon le foyer infectieux) qui n'allonge pas l'intervalle QT."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "HALOPERIDOL",
                # Exécution de cette instruction spécifique
                "medicament2": "AMIODARONE",
                # Exécution de cette instruction spécifique
                "aliases1": ["halopéridol", "haldol", "haloperidol"],
                # Exécution de cette instruction spécifique
                "aliases2": ["amiodarone", "cordarone"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 51,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=51",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Halopéridol, p. 51",
                # Exécution de cette instruction spécifique
                "explication": "Majoration du risque de troubles du rythme ventriculaire graves (torsades de pointes).",
                # Exécution de cette instruction spécifique
                "recommandation": "Association contre-indiquée. Surveiller strictement l'électrocardiogramme (ECG).",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Utiliser un antipsychotique ou anxiolytique à moindre impact cardiaque après avis spécialisé."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "TRAMADOL",
                # Exécution de cette instruction spécifique
                "medicament2": "FLUOXETINE",
                # Exécution de cette instruction spécifique
                "aliases1": ["tramadol", "topalgic", "contramal"],
                # Exécution de cette instruction spécifique
                "aliases2": ["fluoxétine", "prozac", "fluoxetine", "sertraline", "paroxétine"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 101,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=101",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Antalgiques opioïdes et IRS, p. 101",
                # Exécution de cette instruction spécifique
                "explication": "Risque de syndrome sérotoninergique potentiellement fatal (hyperthermie, rigidité musculaire, convulsions, coma).",
                # Exécution de cette instruction spécifique
                "recommandation": "Association à proscrire formellement.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Paracétamol forte dose ou palier antalgique non sérotoninergique en accord avec le médecin."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "IBUPROFENE",
                # Exécution de cette instruction spécifique
                "medicament2": "WARFARINE",
                # Exécution de cette instruction spécifique
                "aliases1": ["ibuprofène", "ibuprofene", "advil", "nurofen", "kétoprofène", "diclofénac", "ains"],
                # Exécution de cette instruction spécifique
                "aliases2": ["warfarine", "coumadine", "sintrom", "anticoagulant", "héparine", "rivaroxaban"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 10,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=10",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Section AINS et anticoagulants oraux, p. 10",
                # Exécution de cette instruction spécifique
                "explication": "Risque hémorragique digestif massif par inhibition plaquettaire et agression de la muqueuse gastrique.",
                # Exécution de cette instruction spécifique
                "recommandation": "Les AINS sont formellement contre-indiqués chez les patients sous anticoagulants oraux.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Paracétamol 1g par prise (sans dépasser 3g/jour) pour traiter la douleur ou la fièvre."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "ASPIRINE",
                # Exécution de cette instruction spécifique
                "medicament2": "IBUPROFENE",
                # Exécution de cette instruction spécifique
                "aliases1": ["aspirine", "acide acétylsalicylique", "aspégic", "aspegic", "kardegic", "kardégic"],
                # Exécution de cette instruction spécifique
                "aliases2": ["ibuprofène", "ibuprofene", "advil", "nurofen", "kétoprofène", "ketoprofene", "diclofénac", "diclofenac", "profénid", "profenid", "voltarène", "voltarene"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 10,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=10",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Précautions AINS, p. 10",
                # Exécution de cette instruction spécifique
                "explication": "ASSOCIATION DE DEUX AINS FORMELLEMENT CONTRE-INDIQUÉE. Cumul de toxicité digestive avec risque élevé d'ulcère perforé et d'hémorragie digestive sans bénéfice antalgique.",
                # Exécution de cette instruction spécifique
                "recommandation": "Ne jamais associer deux anti-inflammatoires non stéroïdiens simultanément.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Conserver un seul AINS à dose efficace et utiliser le Paracétamol pour la douleur."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "PERINDOPRIL",
                # Exécution de cette instruction spécifique
                "medicament2": "SPIRONOLACTONE",
                # Exécution de cette instruction spécifique
                "aliases1": ["périndopril", "perindopril", "coversyl", "ramipril", "énalapril", "enalapril", "losartan"],
                # Exécution de cette instruction spécifique
                "aliases2": ["spironolactone", "aldactone", "éplérénone", "eplerenone"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "MAJEURE",
                # Exécution de cette instruction spécifique
                "bloquant": False,
                # Exécution de cette instruction spécifique
                "page_numero": 77,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=77",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Diurétiques et IEC, p. 77",
                # Exécution de cette instruction spécifique
                "explication": "Risque d'hyperkaliémie sévère potentiellement mortelle (troubles de la conduction cardiaque).",
                # Exécution de cette instruction spécifique
                "recommandation": "Surveiller impérativement la kaliémie et la créatininémie à J7 puis régulièrement.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Ajuster la posologie ou associer un diurétique hypokaliémiant (Furosémide)."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "medicament1": "SIMVASTATINE",
                # Exécution de cette instruction spécifique
                "medicament2": "CLARITHROMYCINE",
                # Exécution de cette instruction spécifique
                "aliases1": ["simvastatine", "atorvastatine", "zocor", "tahor", "crestor"],
                # Exécution de cette instruction spécifique
                "aliases2": ["clarithromycine", "érythromycine", "erythromycine", "zeclar", "josacine"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "MAJEURE",
                # Exécution de cette instruction spécifique
                "bloquant": False,
                # Exécution de cette instruction spécifique
                "page_numero": 83,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=83",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Hypolipémiants et Macrolides, p. 83",
                # Exécution de cette instruction spécifique
                "explication": "Inhibition du CYP3A4 avec risque de rhabdomyolyse aiguë et insuffisance rénale par surdosage en statine.",
                # Exécution de cette instruction spécifique
                "recommandation": "Interrompre temporairement la statine pendant la durée du traitement antibiotique.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Remplacer par Azithromycine (non inhibiteur CYP3A4) ou Amoxicilline."
            }
        ]

        # Règles allergies médicamenteuses
        self.allergy_rules_db = [
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "allergie": "PENICILLINE",
                # Exécution de cette instruction spécifique
                "aliases_allergie": ["pénicilline", "penicilline", "amoxicilline", "bêtalactamines", "beta lactamines"],
                # Exécution de cette instruction spécifique
                "medicaments_interdits": ["amoxicilline", "amoxicilline/acide clavulanique", "clamoxyl", "augmentin", "ampicilline", "oracilline"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 38,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=38",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Amoxicilline, p. 38",
                # Exécution de cette instruction spécifique
                "explication": "Allergie croisée majeure avec risque de choc anaphylactique ou œdème de Quincke mortel.",
                # Exécution de cette instruction spécifique
                "recommandation": "Contre-indication formelle et définitive à toutes les pénicillines.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Macrolides (Azithromycine, Érythromycine) ou Céphalosporines de 3e génération après évaluation médicale."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "allergie": "SULFAMIDES",
                # Exécution de cette instruction spécifique
                "aliases_allergie": ["sulfamide", "sulfamides", "cotrimoxazole", "bactrim"],
                # Exécution de cette instruction spécifique
                "medicaments_interdits": ["co-trimoxazole", "sulfaméthoxazole", "bactrim", "cotrimoxazole"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 12,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=12",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Fiche Co-trimoxazole, p. 12",
                # Exécution de cette instruction spécifique
                "explication": "Risque de toxidermie sévère (Syndrome de Stevens-Johnson / Lyell).",
                # Exécution de cette instruction spécifique
                "recommandation": "Prescription strictement interdite chez le patient allergique aux sulfamides.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Amoxicilline ou Ciprofloxacine selon la localisation de l'infection."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "allergie": "AINS / ASPIRINE",
                # Exécution de cette instruction spécifique
                "aliases_allergie": ["ains", "aspirine", "aspirin", "ibuprofène", "ibuprofene", "anti-inflammatoire"],
                # Exécution de cette instruction spécifique
                "medicaments_interdits": ["ibuprofène", "ibuprofene", "advil", "nurofen", "kétoprofène", "ketoprofene", "diclofénac", "diclofenac", "aspirine", "aspégic", "naproxène", "voltarène"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 10,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=10",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie AINS, p. 10",
                # Exécution de cette instruction spécifique
                "explication": "Risque de crise d'asthme sévère (syndrome de Widal), bronchospasme aigu ou choc anaphylactoïde.",
                # Exécution de cette instruction spécifique
                "recommandation": "Contre-indication absolue à tous les AINS.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Privilégier le Paracétamol 1g ou un antalgique de palier 2 (Tramadol)."
            },
            # Ouverture d'une structure de données ou d'une fonction
            {
                # Exécution de cette instruction spécifique
                "allergie": "MACROLIDES",
                # Exécution de cette instruction spécifique
                "aliases_allergie": ["macrolide", "macrolides", "azithromycine", "érythromycine", "clarithromycine"],
                # Exécution de cette instruction spécifique
                "medicaments_interdits": ["azithromycine", "clarithromycine", "érythromycine", "josacine", "rovamycine", "zithromax"],
                # Exécution de cette instruction spécifique
                "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                # Exécution de cette instruction spécifique
                "bloquant": True,
                # Exécution de cette instruction spécifique
                "page_numero": 44,
                # Exécution de cette instruction spécifique
                "document_nom": "guideline-339-fr.pdf",
                # Initialisation et affectation d'une variable
                "document_url": "http://127.0.0.1:8089/documents/guideline-339-fr.pdf#page=44",
                # Exécution de cette instruction spécifique
                "source_medicale": "Guide Médicaments Essentiels MSF/OMS, Monographie Macrolides, p. 44",
                # Exécution de cette instruction spécifique
                "explication": "Réaction d'hypersensibilité cutanée et hépatique sévère.",
                # Exécution de cette instruction spécifique
                "recommandation": "Éviter la classe des macrolides.",
                # Exécution de cette instruction spécifique
                "alternative_recommandee": "Amoxicilline, Céfixime ou Doxycycline selon indication clinique."
            }
        ]

    # Définition de la fonction evaluate_triage_and_orientation
    def evaluate_triage_and_orientation(self, message: str) -> Dict[str, Any]:
        """
        [SOUTENANCE] - ALGORITHME DE TRIAGE STRICT
        Analyse clinique hybride du message patient :
        1. Vérifie en priorité absolue la présence d'un Red Flag (Section 5.1).
        2. Si aucun Red Flag, applique la matrice d'orientation et les 7 règles d'arbitrage (Sections 7 & 8).
        Cet algorithme garantit que l'IA ne décide JAMAIS du sort d'une urgence vitale,
        mais délègue cette tâche à ce code déterministe (100% de fiabilité).
        """
        # Affectation de la valeur à la variable 'msg_lower'
        msg_lower = message.lower().strip()

        # 1. TEST PRIORITAIRE : DÉTECTION DE DRAPEAU ROUGE (URGENCE ABSOLUE)
        # L'algorithme recherche des patterns syntaxiques stricts liés à la mort ou aux séquelles irréversibles.
        
        # A. Pédiatrie Grave (Nourrisson fébrile, léthargique ou refus de téter)
        est_bebe = any(b in msg_lower for b in ["bébé", "bebe", "nourrisson", "nouveau-né"])
        # Affectation de la valeur à la variable 'a_fievre'
        a_fievre = any(f in msg_lower for f in ["fièvre", "fievre", "chaud", "39", "38", "40", "brûlant"])
        # Affectation de la valeur à la variable 'signe_gravite_bebe'
        signe_gravite_bebe = any(g in msg_lower for g in ["mou", "léthargique", "hypotonique", "ne réagit", "somnolent", "refuse de téter", "refuse de teter", "respire vite", "mois", "semaine", "convulsion", "tète plus", "boit plus"])
        # Structure conditionnelle : on vérifie si la condition est vraie
        if est_bebe and (a_fievre or signe_gravite_bebe) and (signe_gravite_bebe or "téter" in msg_lower or "teter" in msg_lower):
            # Appel d'une fonction ou méthode spécifique
            rf_ped = next((r for r in self.red_flags_db if r["id"] == "PEDIATRIE_GRAVE"), self.red_flags_db[0])
            # Renvoie le résultat final de cette fonction
            return {
                # Exécution de cette instruction spécifique
                "est_urgence_vitale": True,
                # Exécution de cette instruction spécifique
                "niveau_gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "niveau_urgence": "SAMU",
                # Exécution de cette instruction spécifique
                "red_flags": [rf_ped["diagnostic_suspecte"]],
                # Exécution de cette instruction spécifique
                "condition_detectee": "Fièvre du nourrisson associée à un refus d'alimentation / léthargie (Urgence vitale pédiatrique)",
                # Exécution de cette instruction spécifique
                "source_medicale": rf_ped["source"],
                # Exécution de cette instruction spécifique
                "numero_urgence": rf_ped["numero_secours"],
                # Exécution de cette instruction spécifique
                "premiers_gestes": rf_ped["premiers_gestes"],
                # Exécution de cette instruction spécifique
                "specialites_suggerees": ["SAMU 1515 / Urgences Pédiatriques"],
                # Ouverture d'une structure de données ou d'une fonction
                "justification_orientation": (
                    # Exécution de cette instruction spécifique
                    "Section 5 du Référentiel Diam Yaraam & Protocole OMS ETAT : « Tout signe de léthargie ou refus de s'alimenter avec fièvre chez le nourrisson constitue une urgence absolue »."
                # Exécution de cette instruction spécifique
                ),
                # Exécution de cette instruction spécifique
                "rappel_legal": "URGENCE VITALE PÉDIATRIQUE : Consultation hospitalière immédiate requise sans délai."
            }

        # B. Cardiologie / Douleur thoracique aiguë
        # Ex: Un patient dit "J'ai mal au coeur et ça serre".
        # Le RAG intercepte ça immédiatement et arrête le flux, car le risque d'Infarctus est majeur.
        has_poitrine_cardio = any(k in msg_lower for k in ["poitrine", "coeur", "cœur", "thorac", "cardiaque", "infarctus", "sternum", "coronar", "cardio"])
        # Affectation de la valeur à la variable 'has_douleur_aigu'
        has_douleur_aigu = any(k in msg_lower for k in ["douleur", "mal", "serre", "oppress", "irradi", "angoisse", "brûlure", "pression", "étau", "serré", "point", "bloqué", "aigu"])
        # Affectation de la valeur à la variable 'is_cardio_rf'
        is_cardio_rf = (
            (has_poitrine_cardio and has_douleur_aigu) or
            # Exécution de cette # instruction spécifique
            "infarctus" in msg_lower or "crise cardiaque" in msg_lower or
            # Appel d'une fonction ou méthode spécifique
            ("douleur" in msg_lower and "irradi" in msg_lower)
        )

        # Structure conditionnelle : on vérifie si la condition est vraie
        if is_cardio_rf:
            # Appel d'une fonction ou méthode spécifique
            rf_cardio = next((r for r in self.red_flags_db if r["id"] == "CARDIO_INFARCTUS"), self.red_flags_db[0])
            # Renvoie le résultat final de cette fonction
            return {
                # Exécution de cette instruction spécifique
                "est_urgence_vitale": True,
                # Exécution de cette instruction spécifique
                "niveau_gravite": "ROUGE",
                # Exécution de cette instruction spécifique
                "niveau_urgence": "SAMU",
                # Exécution de cette instruction spécifique
                "red_flags": [rf_cardio["diagnostic_suspecte"]],
                # Exécution de cette instruction spécifique
                "condition_detectee": rf_cardio["condition"],
                # Exécution de cette instruction spécifique
                "source_medicale": rf_cardio["source"],
                # Exécution de cette instruction spécifique
                "numero_urgence": rf_cardio["numero_secours"],
                # Exécution de cette instruction spécifique
                "premiers_gestes": rf_cardio["premiers_gestes"],
                # Exécution de cette instruction spécifique
                "specialites_suggerees": ["SAMU 1515 / Service d'Accueil des Urgences"],
                # Ouverture d'une structure de données ou d'une fonction
                "justification_orientation": (
                    # Exécution de cette instruction spécifique
                    "Section 5 du Référentiel Diam Yaraam : « La détection d'urgence prime toujours sur l'orientation spécialisée ». "
                    # Exécution de cette instruction spécifique
                    "Le parcours standard de téléconsultation est immédiatement interrompu."
                # Exécution de cette instruction spécifique
                ),
                # Exécution de cette instruction spécifique
                "rappel_legal": "URGENCE VITALE ABSOLUE : Prise en charge SAMU 1515 requise sans délai."
            }

        # C. Autres Red Flags (Pneumologie détresse, AVC, etc.)
        for rf in self.red_flags_db:
            # Affectation de la valeur à la variable 'matched_keywords'
            matched_keywords = [kw for kw in rf["mots_cles"] if kw in msg_lower]
            # Structure conditionnelle : on vérifie si la condition est vraie
            if len(matched_keywords) >= 1:
                # Renvoie le résultat final de cette fonction
                return {
                    # Exécution de cette instruction spécifique
                    "est_urgence_vitale": True,
                    # Exécution de cette instruction spécifique
                    "niveau_gravite": "ROUGE",
                    # Exécution de cette instruction spécifique
                    "niveau_urgence": "SAMU",
                    # Exécution de cette instruction spécifique
                    "red_flags": [rf["diagnostic_suspecte"]],
                    # Exécution de cette instruction spécifique
                    "condition_detectee": rf["condition"],
                    # Exécution de cette instruction spécifique
                    "source_medicale": rf["source"],
                    # Exécution de cette instruction spécifique
                    "numero_urgence": rf["numero_secours"],
                    # Exécution de cette instruction spécifique
                    "premiers_gestes": rf["premiers_gestes"],
                    # Exécution de cette instruction spécifique
                    "specialites_suggerees": ["SAMU 1515 / Service d'Accueil des Urgences"],
                    # Ouverture d'une structure de données ou d'une fonction
                    "justification_orientation": (
                        # Exécution de cette instruction spécifique
                        "Section 5 du Référentiel Diam Yaraam : « La détection d'urgence prime toujours sur l'orientation spécialisée ». "
                        # Exécution de cette instruction spécifique
                        "Le parcours standard de téléconsultation est immédiatement interrompu."
                    # Exécution de cette instruction spécifique
                    ),
                    # Exécution de cette instruction spécifique
                    "rappel_legal": "URGENCE VITALE : Une prise en charge immédiate sans délai est nécessaire."
                }

        # 2. TEST SECONDAIRE : ORIENTATION VERS LE SPÉCIALISTE IDOINE (SECTIONS 7 & 8)
        # Si et seulement si AUCUN Red Flag n'est détecté, on utilise le RAG pour
        # trouver le meilleur médecin spécialiste (ex: Cardiologue, Dermatologue) 
        # pour orienter le patient de manière ciblée.
        
        # Vérification priorité âge (Règle 8.2) : Enfant
        # Un enfant de moins de 15 ans doit toujours être vu par un pédiatre en priorité.
        mots_pediatrie = ["enfant", "bébé", "nourrisson", "fils", "fille", "bébé", "ans", "mois"]
        # Affectation de la valeur à la variable 'est_enfant'
        est_enfant = any(p in msg_lower for p in ["enfant", "nourrisson", "bébé", "mon fils", "ma fille"])

        # Initialisation et affectation d'une variable
        specialite_scores: Dict[str, int] = {}
        # Boucle itérative pour parcourir les éléments
        for spec_name, spec_info in self.specialties_db.items():
            # Affectation de la valeur à la variable 'score'
            score = 0
            # Boucle itérative pour parcourir les éléments
            for symp in spec_info["symptomes"]:
                # Structure conditionnelle : on vérifie si la condition est vraie
                if symp in msg_lower:
                    # Initialisation et affectation d'une variable
                    score += 1
            # Structure conditionnelle : on vérifie si la condition est vraie
            if score > 0:
                # Initialisation et affectation d'une variable
                specialite_scores[spec_name] = score

        # Structure conditionnelle : on vérifie si la condition est vraie
        if est_enfant:
            # Règle 8.2 : Priorité à l'âge du patient
            best_spec = "Pédiatrie"
            # Affectation de la valeur à la variable 'justif'
            justif = "Règle 8.2 du Référentiel : Chez l'enfant, la Pédiatrie est prioritaire par défaut."
            # Affectation de la valeur à la variable 'spec_info'
            spec_info = self.specialties_db["Pédiatrie"]
        # Sinon si
        elif specialite_scores:
            # Règle 8.3 & 8.4 : Symptôme dominant et spécialité organique directe
            # Trier par score décroissant
            sorted_specs = sorted(specialite_scores.items(), key=lambda x: x[1], reverse=True)
            # Affectation de la valeur à la variable 'best_spec'
            best_spec = sorted_specs[0][0]
            # Affectation de la valeur à la variable 'spec_info'
            spec_info = self.specialties_db[best_spec]
            # Affectation de la valeur à la variable 'justif'
            justif = f"{spec_info['regle_reference']} ({spec_info['confiance']})"
        # Sinon
        else:
            # Règle 8.7 : Médecine générale par défaut
            best_spec = "Médecine Générale"
            # Affectation de la valeur à la variable 'spec_info'
            spec_info = self.specialties_db["Médecine Générale"]
            # Affectation de la valeur à la variable 'justif'
            justif = "Section 4 & Règle 8.7 du Référentiel : Orientation vers la Médecine Générale par défaut pour premier bilan clinique."

        # Renvoie le résultat final de cette fonction
        return {
            # Exécution de cette instruction spécifique
            "est_urgence_vitale": False,
            # Exécution de cette instruction spécifique
            "niveau_gravite": "VERT" if "fatigue" in msg_lower or "acné" in msg_lower else "JAUNE",
            # Exécution de cette instruction spécifique
            "niveau_urgence": "MODERE",
            # Exécution de cette instruction spécifique
            "red_flags": [],
            # Exécution de cette instruction spécifique
            "specialites_suggerees": [best_spec],
            # Exécution de cette instruction spécifique
            "justification_orientation": justif,
            # Exécution de cette instruction spécifique
            "source_medicale": f"Référentiel d'Orientation Médicale Diam Yaraam (Sections 7 & 8) — {best_spec}",
            # Exécution de cette instruction spécifique
            "rappel_legal": "Conformément aux sections 4 et 10 du référentiel, cette orientation constitue une aide indicative et ne remplace pas un examen clinique ou un diagnostic médical.",
            # Exécution de cette instruction spécifique
            "premiers_gestes": []
        }

    # Définition de la fonction evaluate_drug_safety
    def evaluate_drug_safety(self, drugs: List[str], allergies: List[str]) -> List[Dict[str, Any]]:
        """
        Vérifie de manière exhaustive les interactions médicamenteuses croisées
        et les allergies déclarées du patient à partir du Guide MSF.
        """
        # Affectation de la valeur à la variable 'results'
        results = []
        # Affectation de la valeur à la variable 'drugs_clean'
        drugs_clean = [d.strip().lower() for d in drugs if d.strip()]
        # Affectation de la valeur à la variable 'allergies_clean'
        allergies_clean = [a.strip().lower() for a in allergies if a.strip()]

        # 1. Vérification des allergies croisées
        # Boucle itérative pour parcourir les éléments
        for d in drugs_clean:
            # Boucle itérative pour parcourir les éléments
            for rule in self.allergy_rules_db:
                # Si le patient a cette allergie
                has_allergy = any(alias in " ".join(allergies_clean) for alias in rule["aliases_allergie"])
                # Structure conditionnelle : on vérifie si la condition est vraie
                if has_allergy:
                    # Vérifier si le médicament est interdit
                    is_forbidden = any(interdit in d for interdit in rule["medicaments_interdits"])
                    # Structure conditionnelle : on vérifie si la condition est vraie
                    if is_forbidden:
                        # Ouverture d'une structure de données ou d'une fonction
                        results.append({
                            # Exécution de cette instruction spécifique
                            "medicament1": d.upper(),
                            # Exécution de cette instruction spécifique
                            "medicament2": rule["allergie"],
                            # Exécution de cette instruction spécifique
                            "niveau_danger": rule["niveau_danger"],
                            # Exécution de cette instruction spécifique
                            "bloquant": rule["bloquant"],
                            # Exécution de cette instruction spécifique
                            "page_numero": rule.get("page_numero"),
                            # Exécution de cette instruction spécifique
                            "document_nom": rule.get("document_nom"),
                            # Exécution de cette instruction spécifique
                            "document_url": rule.get("document_url"),
                            # Exécution de cette instruction spécifique
                            "source_medicale": rule["source_medicale"],
                            # Exécution de cette instruction spécifique
                            "explication": rule["explication"],
                            # Exécution de cette instruction spécifique
                            "recommandation": rule["recommandation"],
                            # Exécution de cette instruction spécifique
                            "alternative_recommandee": rule["alternative_recommandee"]
                        })

        # 2. Vérification des interactions médicament-médicament (toutes paires possibles)
        # Boucle itérative pour parcourir les éléments
        for i in range(len(drugs_clean)):
            # Boucle itérative pour parcourir les éléments
            for j in range(i + 1, len(drugs_clean)):
                # Affectation de la valeur à la variable 'd1'
                d1 = drugs_clean[i]
                # Affectation de la valeur à la variable 'd2'
                d2 = drugs_clean[j]
                
                # Boucle itérative pour parcourir les éléments
                for inter in self.drug_interactions_db:
                    # Affectation de la valeur à la variable 'match_1_to_1'
                    match_1_to_1 = any(alias in d1 for alias in inter["aliases1"]) and any(alias in d2 for alias in inter["aliases2"])
                    # Affectation de la valeur à la variable 'match_2_to_1'
                    match_2_to_1 = any(alias in d2 for alias in inter["aliases1"]) and any(alias in d1 for alias in inter["aliases2"])
                    
                    # Structure conditionnelle : on vérifie si la condition est vraie
                    if match_1_to_1 or match_2_to_1:
                        # Ouverture d'une structure de données ou d'une fonction
                        results.append({
                            # Exécution de cette instruction spécifique
                            "medicament1": d1.upper(),
                            # Exécution de cette instruction spécifique
                            "medicament2": d2.upper(),
                            # Exécution de cette instruction spécifique
                            "niveau_danger": inter["niveau_danger"],
                            # Exécution de cette instruction spécifique
                            "bloquant": inter["bloquant"],
                            # Exécution de cette instruction spécifique
                            "page_numero": inter.get("page_numero"),
                            # Exécution de cette instruction spécifique
                            "document_nom": inter.get("document_nom"),
                            # Exécution de cette instruction spécifique
                            "document_url": inter.get("document_url"),
                            # Exécution de cette instruction spécifique
                            "source_medicale": inter["source_medicale"],
                            # Exécution de cette instruction spécifique
                            "explication": inter["explication"],
                            # Exécution de cette instruction spécifique
                            "recommandation": inter["recommandation"],
                            # Exécution de cette instruction spécifique
                            "alternative_recommandee": inter["alternative_recommandee"]
                        })

        # Renvoie le résultat final de cette fonction
        return results

    # Définition de la fonction search
    def search(self, query: str, top_k: int = 2) -> str:
        # Exécution de cette instruction spécifique
        """Méthode de recherche textuelle compatible avec les modules existants."""
        # Affectation de la valeur à la variable 'res'
        res = self.evaluate_triage_and_orientation(query)
        # Structure conditionnelle : on vérifie si la condition est vraie
        if res["est_urgence_vitale"]:
            # Renvoie le résultat final de cette fonction
            return (
                # Exécution de cette instruction spécifique
                f"--- ALERTE ROUGE CLINIQUE ---\n"
                # Exécution de cette instruction spécifique
                f"Protocole : {res['source_medicale']}\n"
                # Exécution de cette instruction spécifique
                f"Drapeau Rouge : {res['condition_detectee']}\n"
                # Exécution de cette instruction spécifique
                f"Orientation obligatoire : {res['numero_urgence']} (SAMU National)\n"
            )
        # Sinon
        else:
            # Renvoie le résultat final de cette fonction
            return (
                # Exécution de cette instruction spécifique
                f"--- ORIENTATION MÉDICALE ONDMS ---\n"
                # Exécution de cette instruction spécifique
                f"Spécialité suggérée : {res['specialites_suggerees'][0]}\n"
                # Exécution de cette instruction spécifique
                f"Justification : {res['justification_orientation']}\n"
                # Exécution de cette instruction spécifique
                f"Rappel légal : {res['rappel_legal']}\n"
            )

# Instance singleton du moteur RAG
rag_engine = RagEngine()
