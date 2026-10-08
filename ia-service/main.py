"""
====================================================================================================
MICROSERVICE D'INTELLIGENCE ARTIFICIELLE CLINIQUEMENT VALIDÉE (FASTAPI / RAG / LLM)
====================================================================================================

🎓 PROBLÉMATIQUE CENTRALE & ARGUMENTS DU JURY DE SOUTENANCE :
"Comment garantir qu'une IA en télémédecine n'hallucine pas et ne prescrive pas un traitement dangereux ?"

🛡️ ARCHITECTURE DE FIABILISATION MÉDICALE EN TROIS COUCHES :
1. Triage Vital Non-Diagnostique :
   - L'IA n'émet JAMAIS de diagnostic médical définitif (interdit par le Code de Déontologie Médicale).
   - Elle effectue un "Triage d'Orientation" selon l'échelle colorimétrique internationale (VERT, JAUNE, ORANGE, ROUGE)
     et alerte immédiatement en cas d'urgence vitale en affichant le numéro du SAMU national (1515 au Sénégal).

2. Moteur RAG Clinique (Retrieval-Augmented Generation) :
   - Avant de répondre, le système recherche les preuves dans une base documentaire médicale certifiée :
     * Guide Médical MSF (Médecins Sans Frontières)
     * Référentiel OMS (Soins Primaires d'Urgence - SPU)
     * Référentiel des praticiens de l'Ordre National des Médecins du Sénégal (ONDMS).
   - Les réponses sont systématiquement sourcées avec citations et pages des guides officiels.

3. Pharmacovigilance & Analyse d'Interactions Médicamenteuses :
   - Détection automatique des contre-indications croisées (ex: AINS + Anticoagulants, ou allergie déclarée).
   - Bloque la validation d'ordonnances dangereuses avant leur émission.
====================================================================================================
"""

# Importation de la librairie os
import os
# Importation de la librairie uuid
import uuid
# Importation de la librairie json
import json
# Importation de la librairie base64
import base64
# Importation de la librairie html
import html
# Importation de la librairie re
import re
# Importation de la librairie urllib.parse
import urllib.parse
# Importation de la librairie logging
import logging
# Importation spécifique depuis le module datetime
from datetime import datetime
# Importation spécifique depuis le module typing
from typing import List, Dict, Optional, Any
# Importation spécifique depuis le module fastapi
from fastapi import FastAPI, HTTPException, Header
# Importation spécifique depuis le module fastapi.responses
from fastapi.responses import FileResponse, HTMLResponse
# Importation spécifique depuis le module fastapi.middleware.cors
from fastapi.middleware.cors import CORSMiddleware
# Importation spécifique depuis le module pydantic
from pydantic import BaseModel
# Importation spécifique depuis le module dotenv
from dotenv import load_dotenv

# Charger les variables d'environnement (.env)
load_dotenv()

# Configurer le logger d'application
logging.basicConfig(level=logging.INFO)
# Affectation de la valeur à la variable 'logger'
logger = logging.getLogger("ia-service.main")

# Importation des moteurs d'inférence LLM (OpenRouter) et d'indexation vectorielle RAG
from llm_client import (
    # Exécution de cette instruction spécifique
    chat_completion,
    # Exécution de cette instruction spécifique
    triage_symptoms_llm,
    # Exécution de cette instruction spécifique
    analyze_interactions_llm,
    # Exécution de cette instruction spécifique
    check_openrouter_health,
    # Exécution de cette instruction spécifique
    LLM_MODEL
)
# Importation spécifique depuis le module rag_engine
from rag_engine import rag_engine

# Instanciation du serveur FastAPI avec métadonnées Swagger / OpenAPI
app = FastAPI(
    # Affectation de la valeur à la variable 'title'
    title="Diam Yaraam IA Service",
    # Affectation de la valeur à la variable 'description'
    description="Service d'Intelligence Artificielle pour la télémédecine, le triage d'urgence SAMU et la pharmacovigilance MSF",
    # Affectation de la valeur à la variable 'version'
    version="2.1.0"
)

# Configuration de la sécurité CORS (Cross-Origin Resource Sharing)
cors_origins_env = os.getenv(
    # Exécution de cette instruction spécifique
    "CORS_ALLOWED_ORIGINS",
    # Exécution de cette instruction spécifique
    "http://localhost:5173,http://localhost:8088,http://localhost:3000,http://localhost:8090"
)
# Affectation de la valeur à la variable 'allowed_origins_list'
allowed_origins_list = [o.strip() for o in cors_origins_env.split(",") if o.strip()]

# Ouverture d'une structure de données ou d'une fonction
app.add_middleware(
    # Exécution de cette instruction spécifique
    CORSMiddleware,
    # Affectation de la valeur à la variable 'allow_origins'
    allow_origins=allowed_origins_list,
    # Affectation de la valeur à la variable 'allow_credentials'
    allow_credentials=False,
    # Affectation de la valeur à la variable 'allow_methods'
    allow_methods=["GET", "POST", "OPTIONS"],
    # Affectation de la valeur à la variable 'allow_headers'
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"],
)


# ─── Modèles de requêtes et de réponses ─────────────────────────────────────

# Définition de la classe TriageRequest
class TriageRequest(BaseModel):
    # Exécution de cette instruction spécifique
    description: str

# Définition de la classe TriageResponse
class TriageResponse(BaseModel):
    # Exécution de cette instruction spécifique
    description_symptomes: str
    # Exécution de cette instruction spécifique
    niveau_gravite: str  # VERT, JAUNE, ORANGE, ROUGE
    # Initialisation et affectation d'une variable
    niveau_urgence: Optional[str] = "MODERE"  # FAIBLE, MODERE, URGENT, SAMU
    # Initialisation et affectation d'une variable
    est_urgence_vitale: bool = False
    # Exécution de cette instruction spécifique
    orientation_suggeree: str
    # Initialisation et affectation d'une variable
    specialites_suggerees: List[str] = []
    # Initialisation et affectation d'une variable
    justification_orientation: Optional[str] = None
    # Initialisation et affectation d'une variable
    source_medicale: Optional[str] = None
    # Initialisation et affectation d'une variable
    numero_urgence: Optional[str] = "1515"
    # Exécution de cette instruction spécifique
    conseil_immediat: str
    # Initialisation et affectation d'une variable
    premiers_gestes: List[str] = []
    # Initialisation et affectation d'une variable
    rappel_legal: Optional[str] = None
    # Initialisation et affectation d'une variable
    red_flags: List[str] = []
    # Initialisation et affectation d'une variable
    questions_suivi: List[str] = []

# Définition de la classe ChatMessage
class ChatMessage(BaseModel):
    # Initialisation et affectation d'une variable
    id: Optional[str] = None
    # Exécution de cette instruction spécifique
    contenu: str
    # Exécution de cette instruction spécifique
    estUtilisateur: bool
    # Initialisation et affectation d'une variable
    timestamp: Optional[str] = None
    # Initialisation et affectation d'une variable
    recommandations: Optional[List[str]] = None

# Définition de la classe ChatRequest
class ChatRequest(BaseModel):
    # Exécution de cette instruction spécifique
    message: str
    # Initialisation et affectation d'une variable
    history: List[dict] = []

# Définition de la classe ChatResponse
class ChatResponse(BaseModel):
    # Exécution de cette instruction spécifique
    id: str
    # Exécution de cette instruction spécifique
    contenu: str
    # Exécution de cette instruction spécifique
    estUtilisateur: bool
    # Exécution de cette instruction spécifique
    timestamp: str
    # Initialisation et affectation d'une variable
    recommandations: List[str] = []
    # Initialisation et affectation d'une variable
    specialites_suggerees: Optional[List[str]] = None
    # Initialisation et affectation d'une variable
    est_urgence_vitale: bool = False
    # Initialisation et affectation d'une variable
    niveau_urgence: Optional[str] = "MODERE"  # SAMU, URGENT, MODERE, FAIBLE
    # Initialisation et affectation d'une variable
    source_medicale: Optional[str] = None
    # Initialisation et affectation d'une variable
    numero_urgence: Optional[str] = None
    # Initialisation et affectation d'une variable
    premiers_gestes: List[str] = []
    # Initialisation et affectation d'une variable
    red_flags: List[str] = []
    # Initialisation et affectation d'une variable
    justification_orientation: Optional[str] = None
    # Initialisation et affectation d'une variable
    rappel_legal: Optional[str] = None

# Définition de la classe InteractionRequest
class InteractionRequest(BaseModel):
    # Initialisation et affectation d'une variable
    medicaments: List[str] = []
    # Initialisation et affectation d'une variable
    allergies: List[str] = []

# Définition de la classe InteractionResponse
class InteractionResponse(BaseModel):
    # Exécution de cette instruction spécifique
    medicament1: str
    # Exécution de cette instruction spécifique
    medicament2: str
    # Exécution de cette instruction spécifique
    niveau_danger: str  # CONTRE_INDICATION_ABSOLUE, MAJEURE, MODEREE, MINEURE
    # Initialisation et affectation d'une variable
    bloquant: bool = False
    # Initialisation et affectation d'une variable
    source_medicale: Optional[str] = None
    # Initialisation et affectation d'une variable
    page_numero: Optional[int] = None
    # Initialisation et affectation d'une variable
    document_url: Optional[str] = None
    # Initialisation et affectation d'une variable
    document_nom: Optional[str] = None
    # Exécution de cette instruction spécifique
    explication: str
    # Exécution de cette instruction spécifique
    recommandation: str
    # Initialisation et affectation d'une variable
    alternative_recommandee: Optional[str] = None

# ─── Helpers ───────────────────────────────────────────────────────────────

# Définition de la fonction extract_role_from_token
def extract_role_from_token(auth_header: Optional[str]) -> str:
    """
    Extrait de manière sécurisée le rôle du token JWT propagé par le Gateway.
    Retourne 'PATIENT' par défaut.
    """
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not auth_header or not auth_header.lower().startswith("bearer "):
        # Renvoie le résultat final de cette fonction
        return "PATIENT"
    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # Affectation de la valeur à la variable 'token'
        token = auth_header.split(" ")[1]
        # Affectation de la valeur à la variable 'payload_segment'
        payload_segment = token.split(".")[1]
        # Affectation de la valeur à la variable 'padding'
        padding = '=' * (4 - len(payload_segment) % 4)
        # Affectation de la valeur à la variable 'decoded_bytes'
        decoded_bytes = base64.urlsafe_b64decode(payload_segment + padding)
        # Affectation de la valeur à la variable 'payload'
        payload = json.loads(decoded_bytes)
        # Renvoie le résultat final de cette fonction
        return payload.get("role", "PATIENT").upper()
    # Capture et gestion de l'erreur si le bloc try échoue
    except Exception as e:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.warning(f"Impossible de décoder le rôle du JWT: {e}. Rôle PATIENT par défaut.")
        # Renvoie le résultat final de cette fonction
        return "PATIENT"

# ─── Endpoints ──────────────────────────────────────────────────────────────

@app.get("/health")
# Définition de la fonction asynchrone health_check
async def health_check():
    # Exécution de cette instruction spécifique
    """Vérifie le statut de santé du service, du RAG et d'OpenRouter."""
    # Affectation de la valeur à la variable 'llm_health'
    llm_health = await check_openrouter_health()
    # Renvoie le résultat final de cette fonction
    return {
        # Exécution de cette instruction spécifique
        "status": "healthy",
        # Exécution de cette instruction spécifique
        "service": "diam-yaraam-ia-service",
        # Exécution de cette instruction spécifique
        "version": "2.1.0",
        # Exécution de cette instruction spécifique
        "timestamp": datetime.utcnow().isoformat(),
        # Exécution de cette instruction spécifique
        "rag_database_loaded": rag_engine.is_loaded,
        # Exécution de cette instruction spécifique
        "rag_red_flags_count": len(rag_engine.red_flags_db),
        # Exécution de cette instruction spécifique
        "rag_specialties_count": len(rag_engine.specialties_db),
        # Exécution de cette instruction spécifique
        "rag_msf_interactions_count": len(rag_engine.drug_interactions_db),
        # Exécution de cette instruction spécifique
        "llm_provider": llm_health
    }

@app.post("/triage", response_model=TriageResponse)
@app.post("/ia/triage", response_model=TriageResponse)
# Définition de la fonction asynchrone triage_symptomes
async def triage_symptomes(req: TriageRequest):
    """
    Évalue la gravité des symptômes décrits par le patient.
    Déclenche l'alerte vitale SAMU 1515 ou oriente vers le spécialiste agréé ONDMS.
    """
    # Enregistre une trace (log) pour le suivi de l'application
    logger.info(f"Requête de triage reçue : {req.description[:80]}...")
    # Affectation de la valeur à la variable 'result'
    result = await triage_symptoms_llm(req.description)
    # Renvoie le résultat final de cette fonction
    return TriageResponse(**result)

@app.post("/chat", response_model=ChatResponse)
@app.post("/ia/chat", response_model=ChatResponse)
# Définition de la fonction asynchrone chat_assistant
async def chat_assistant(req: ChatRequest, authorization: Optional[str] = Header(None)):
    """
    Assistant conversationnel médical intelligent.
    1. Si rôle Patient : vérifie d'abord les Red Flags d'urgence (SAMU 1515)
       puis oriente vers la bonne spécialité selon regles_orientation_medicale.md.
    2. Si rôle Médecin : copilote de recherche clinique et pharmacologique.
    """
    # Affectation de la valeur à la variable 'role'
    role = extract_role_from_token(authorization)
    # Enregistre une trace (log) pour le suivi de l'application
    logger.info(f"Requête chat reçue pour le rôle: {role} (message: {req.message[:50]}...)")
    
    # Affectation de la valeur à la variable 'is_patient'
    is_patient = role not in ("MEDECIN", "ADMIN", "DOCTEUR")

    # 1. ANALYSE CLINIQUE RAG EN PRIORITÉ
    # Affectation de la valeur à la variable 'rag_eval'
    rag_eval = rag_engine.evaluate_triage_and_orientation(req.message)

    # 2. CAS URGENCE VITALE ABSOLUE (Drapeau Rouge Patient)
    # Structure conditionnelle : on vérifie si la condition est vraie
    if is_patient and rag_eval["est_urgence_vitale"]:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.warning(f"🚨 URGENCE VITALE DÉTECTÉE DANS LE CHAT : {rag_eval['condition_detectee']}")
        # Affectation de la valeur à la variable 'reponse_urgence'
        reponse_urgence = (
            # Exécution de cette instruction spécifique
            f"🚨 **ALERTE URGENCE VITALE — SAMU NATIONAL (1515)**\n\n"
            # Exécution de cette instruction spécifique
            f"Vos symptômes constituent un **signal d'alarme vital immédiat** : {rag_eval['condition_detectee']}.\n\n"
            # Exécution de cette instruction spécifique
            f"Conformément à la Section 5.1 du Référentiel Médical Diam Yaraam et au protocole d'urgence OMS SPU, "
            # Exécution de cette instruction spécifique
            f"**le parcours classique de téléconsultation est immédiatement interrompu**.\n\n"
            # Exécution de cette instruction spécifique
            f"📞 **Composez sans délai le 1515 (SAMU National)** pour une prise en charge urgente."
        )
        # Renvoie le résultat final de cette fonction
        return ChatResponse(
            # Affectation de la valeur à la variable 'id'
            id=f"ia-alert-{uuid.uuid4().hex[:8]}",
            # Affectation de la valeur à la variable 'contenu'
            contenu=reponse_urgence,
            # Affectation de la valeur à la variable 'estUtilisateur'
            estUtilisateur=False,
            # Affectation de la valeur à la variable 'timestamp'
            timestamp=datetime.now().isoformat(),
            # Affectation de la valeur à la variable 'recommandations'
            recommandations=[
                # Exécution de cette instruction spécifique
                "Appeler immédiatement le SAMU (1515)",
                # Exécution de cette instruction spécifique
                "Adopter une position semi-assise au repos strict",
                # Exécution de cette instruction spécifique
                "Ne rien boire ni manger en attendant les secours",
                # Exécution de cette instruction spécifique
                "Déverrouiller la porte d'entrée pour les secours"
            ],
            # Affectation de la valeur à la variable 'specialites_suggerees'
            specialites_suggerees=["SAMU 1515 / Urgences Hospitalières"],
            # Affectation de la valeur à la variable 'est_urgence_vitale'
            est_urgence_vitale=True,
            # Affectation de la valeur à la variable 'niveau_urgence'
            niveau_urgence="SAMU",
            # Affectation de la valeur à la variable 'source_medicale'
            source_medicale=rag_eval["source_medicale"],
            # Affectation de la valeur à la variable 'numero_urgence'
            numero_urgence="1515",
            # Affectation de la valeur à la variable 'premiers_gestes'
            premiers_gestes=rag_eval["premiers_gestes"],
            # Affectation de la valeur à la variable 'red_flags'
            red_flags=rag_eval["red_flags"],
            # Affectation de la valeur à la variable 'justification_orientation'
            justification_orientation=rag_eval["justification_orientation"],
            # Affectation de la valeur à la variable 'rappel_legal'
            rappel_legal=rag_eval["rappel_legal"]
        )

    # 3. CONVERSATION NORMALE ORIENTÉE SPÉCIALITÉS ONDMS
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not is_patient:
        # Affectation de la valeur à la variable 'system_content'
        system_content = (
            # Exécution de cette instruction spécifique
            "Vous êtes l'assistant de recherche clinique et pharmacologique de la plateforme Diam Yaraam destiné aux médecins agréés de l'ONDMS (Sénégal).\n"
            # Exécution de cette instruction spécifique
            "Votre objectif est d'aider le confrère avec des synthèses cliniques précises, posologies d'usage MSF/Dorosz et interactions thérapeutiques.\n"
            # Exécution de cette instruction spécifique
            "Soyez précis, scientifique, structuré et professionnel."
        )
    # Sinon
    else:
        # Affectation de la valeur à la variable 'system_content'
        system_content = (
            # Exécution de cette instruction spécifique
            "Vous êtes l'assistant médical bienveillant de la plateforme de télémédecine Diam Yaraam (Sénégal), s'adressant à un patient.\n"
            # Exécution de cette instruction spécifique
            "CONSIGNES STRICTES DE SÉCURITÉ ET D'ORIENTATION :\n"
            # Exécution de cette instruction spécifique
            "1. Interdiction formelle de poser un diagnostic médical ou de prescrire des médicaments.\n"
            # Exécution de cette instruction spécifique
            f"2. Spécialité recommandée selon le Référentiel Diam Yaraam : {rag_eval['specialites_suggerees'][0]}.\n"
            # Exécution de cette instruction spécifique
            f"3. Justification de la règle appliquée : {rag_eval['justification_orientation']}.\n"
            # Exécution de cette instruction spécifique
            f"4. Rappel légal obligatoire : {rag_eval['rappel_legal']}.\n"
            # Exécution de cette instruction spécifique
            "5. Répondez de manière chaleureuse, empathique, rassurante et facile à comprendre.\n"
            # Exécution de cette instruction spécifique
            "IMPORTANT - FORMAT DE RÉPONSE JSON OBLIGATOIRE :\n"
            # Exécution de cette instruction spécifique
            "{\n"
            # Exécution de cette instruction spécifique
            '  "reponse": "Votre message chaleureux au patient expliquant calmement la situation et recommandant de consulter un spécialiste",\n'
            # Exécution de cette instruction spécifique
            f'  "specialites_suggerees": ["{rag_eval["specialites_suggerees"][0]}"],\n'
            # Exécution de cette instruction spécifique
            f'  "justification_orientation": "{rag_eval["justification_orientation"]}",\n'
            # Exécution de cette instruction spécifique
            f'  "source_medicale": "{rag_eval["source_medicale"]}",\n'
            # Exécution de cette instruction spécifique
            f'  "rappel_legal": "{rag_eval["rappel_legal"]}",\n'
            # Exécution de cette instruction spécifique
            '  "recommandations": ["Prendre rendez-vous sur Diam Yaraam", "Surveiller l\'évolution des symptômes"]\n'
            # Exécution de cette instruction spécifique
            "}"
        )

    # Affectation de la valeur à la variable 'contexte_doc'
    contexte_doc = rag_engine.search(req.message)
    # Structure conditionnelle : on vérifie si la condition est vraie
    if contexte_doc:
        # Initialisation et affectation d'une variable
        system_content += f"\n\nContexte réglementaire et documentaire actif :\n{contexte_doc}"

    # Affectation de la valeur à la variable 'messages'
    messages = [{"role": "system", "content": system_content}]

    # Ajouter l'historique
    for msg in req.history[-6:]:
        # Affectation de la valeur à la variable 'role_label'
        role_label = "user" if msg.get("estUtilisateur", False) else "assistant"
        # Affectation de la valeur à la variable 'content'
        content = msg.get("contenu", "")
        # Structure conditionnelle : on vérifie si la condition est vraie
        if content:
            # Appel d'une fonction ou méthode spécifique
            messages.append({"role": role_label, "content": content})

    # Appel d'une fonction ou méthode spécifique
    messages.append({"role": "user", "content": req.message})

    # Génération LLM
    reponse_texte = await chat_completion(
        # Affectation de la valeur à la variable 'messages'
        messages=messages, 
        # Affectation de la valeur à la variable 'temperature'
        temperature=0.4, 
        # Affectation de la valeur à la variable 'max_tokens'
        max_tokens=600,
        # Affectation de la valeur à la variable 'response_format'
        response_format={"type": "json_object"} if is_patient else None
    )

    # Affectation de la valeur à la variable 'specialites'
    specialites = rag_eval["specialites_suggerees"]
    # Affectation de la valeur à la variable 'recommandations'
    recommandations = [
        # Exécution de cette instruction spécifique
        f"Consulter un spécialiste en {specialites[0]}",
        # Exécution de cette instruction spécifique
        "Prendre rendez-vous en téléconsultation sur Diam Yaraam"
    ]
    # Affectation de la valeur à la variable 'justif'
    justif = rag_eval["justification_orientation"]
    # Affectation de la valeur à la variable 'legal'
    legal = rag_eval["rappel_legal"]

    # Structure conditionnelle : on vérifie si la condition est vraie
    if is_patient and reponse_texte:
        # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
        try:
            # Affectation de la valeur à la variable 'data'
            data = json.loads(reponse_texte)
            # Affectation de la valeur à la variable 'reponse_texte'
            reponse_texte = data.get("reponse", reponse_texte)
            # Structure conditionnelle : on vérifie si la condition est vraie
            if data.get("specialites_suggerees"):
                # Affectation de la valeur à la variable 'specialites'
                specialites = data["specialites_suggerees"]
            # Structure conditionnelle : on vérifie si la condition est vraie
            if data.get("recommandations"):
                # Affectation de la valeur à la variable 'recommandations'
                recommandations = data["recommandations"]
            # Affectation de la valeur à la variable 'justif'
            justif = data.get("justification_orientation", justif)
            # Affectation de la valeur à la variable 'legal'
            legal = data.get("rappel_legal", legal)
        # Capture et gestion de l'erreur si le bloc try échoue
        except Exception as e:
            # Enregistre une trace (log) pour le suivi de l'application
            logger.error(f"Erreur parsing JSON chat patient: {e}")

    # Structure conditionnelle : on vérifie si la condition est vraie
    if not reponse_texte:
        # Affectation de la valeur à la variable 'reponse_texte'
        reponse_texte = (
            # Exécution de cette instruction spécifique
            f"Bonjour. D'après les symptômes que vous décrivez, une consultation en **{specialites[0]}** est conseillée. "
            # Exécution de cette instruction spécifique
            f"{justif} N'hésitez pas à choisir un médecin agréé sur Diam Yaraam."
        )

    # Renvoie le résultat final de cette fonction
    return ChatResponse(
        # Affectation de la valeur à la variable 'id'
        id=f"ia-{uuid.uuid4().hex[:8]}",
        # Affectation de la valeur à la variable 'contenu'
        contenu=reponse_texte,
        # Affectation de la valeur à la variable 'estUtilisateur'
        estUtilisateur=False,
        # Affectation de la valeur à la variable 'timestamp'
        timestamp=datetime.now().isoformat(),
        # Affectation de la valeur à la variable 'recommandations'
        recommandations=recommandations,
        # Affectation de la valeur à la variable 'specialites_suggerees'
        specialites_suggerees=specialites,
        # Affectation de la valeur à la variable 'est_urgence_vitale'
        est_urgence_vitale=False,
        # Affectation de la valeur à la variable 'niveau_urgence'
        niveau_urgence="MODERE",
        # Affectation de la valeur à la variable 'source_medicale'
        source_medicale=rag_eval["source_medicale"],
        # Affectation de la valeur à la variable 'justification_orientation'
        justification_orientation=justif,
        # Affectation de la valeur à la variable 'rappel_legal'
        rappel_legal=legal
    )

@app.post("/interactions", response_model=List[InteractionResponse])
@app.post("/ia/interactions", response_model=List[InteractionResponse])
# Définition de la fonction asynchrone verifier_interactions
async def verifier_interactions(req: InteractionRequest):
    """
    Vérifie les interactions nocives et contre-indications absolues
    entre la liste de médicaments prescrits et les allergies déclarées du patient,
    avec citation du Guide des Médicaments Essentiels MSF/OMS.
    """
    # Enregistre une trace (log) pour le suivi de l'application
    logger.info(f"Requête de vérification d'interactions reçue : Médicaments={req.medicaments}, Allergies={req.allergies}")
    
    # Affectation de la valeur à la variable 'raw_results'
    raw_results = await analyze_interactions_llm(req.medicaments, req.allergies)
    
    # Affectation de la valeur à la variable 'response_list'
    response_list = []
    # Boucle itérative pour parcourir les éléments
    for item in raw_results:
        # Affectation de la valeur à la variable 'page'
        page = item.get("page_numero")
        # Structure conditionnelle : on vérifie si la condition est vraie
        if not page:
            # Importation de la librairie re
            import re
            # Affectation de la valeur à la variable 'src'
            src = str(item.get("source_medicale", ""))
            # Affectation de la valeur à la variable 'match'
            match = re.search(r'(?:p\.|page\s*)(\d+)', src, re.IGNORECASE)
            # Structure conditionnelle : on vérifie si la condition est vraie
            if match:
                # Affectation de la valeur à la variable 'page'
                page = int(match.group(1))
            # Sinon
            else:
                # Affectation de la valeur à la variable 'page'
                page = 38
        
        # Affectation de la valeur à la variable 'doc_nom'
        doc_nom = item.get("document_nom") or "guideline-339-fr.pdf"
        # Affectation de la valeur à la variable 'doc_url'
        doc_url = item.get("document_url") or f"http://127.0.0.1:8089/ia/documents/view/{doc_nom}?page={page}"

        # Ouverture d'une structure de données ou d'une fonction
        response_list.append(InteractionResponse(
            # Affectation de la valeur à la variable 'medicament1'
            medicament1=item.get("medicament1", "Médicament"),
            # Affectation de la valeur à la variable 'medicament2'
            medicament2=item.get("medicament2", "Interaction/Allergie"),
            # Affectation de la valeur à la variable 'niveau_danger'
            niveau_danger=item.get("niveau_danger", "MODEREE"),
            # Affectation de la valeur à la variable 'bloquant'
            bloquant=item.get("bloquant", False),
            # Affectation de la valeur à la variable 'source_medicale'
            source_medicale=item.get("source_medicale", "Guide Médicaments Essentiels MSF/OMS"),
            # Affectation de la valeur à la variable 'page_numero'
            page_numero=page,
            # Affectation de la valeur à la variable 'document_url'
            document_url=doc_url,
            # Affectation de la valeur à la variable 'document_nom'
            document_nom=doc_nom,
            # Affectation de la valeur à la variable 'explication'
            explication=item.get("explication", "Risque d'interaction médicamenteuse."),
            # Affectation de la valeur à la variable 'recommandation'
            recommandation=item.get("recommandation", "Vérifier la posologie ou demander l'avis d'un confrère."),
            # Affectation de la valeur à la variable 'alternative_recommandee'
            alternative_recommandee=item.get("alternative_recommandee")
        # Exécution de cette instruction spécifique
        ))
    # Renvoie le résultat final de cette fonction
    return response_list

# ─── Consultation des Preuves Documentaires (PDF) ───────────────────────────

# Affectation de la valeur à la variable 'DOCUMENTS_DIR'
DOCUMENTS_DIR = os.path.abspath(os.getenv("DOCUMENTS_PATH", os.path.join(os.path.dirname(__file__), "..", "documents")))

# Définition de la fonction get_safe_document_path
def get_safe_document_path(filename: str) -> tuple[str, str]:
    """
    Valide et résout de manière sécurisée un fichier PDF dans DOCUMENTS_DIR.
    Protège contre les attaques de type Path Traversal (CWE-22) :
    - Refus des chemins absolus et des séparateurs de répertoires
    - Validation stricte du format du nom de fichier via regex
    - Résolution canonique absolue du chemin candidat
    - Vérification d'inclusion stricte dans le répertoire documentaire
    """
    # Affectation de la valeur à la variable 'normalized_name'
    normalized_name = os.path.normpath(filename)

    # Refuser tout chemin ou nom permettant une navigation dans l'arborescence
    if (
        # Appel d'une fonction ou méthode spécifique
        normalized_name in (".", "")
        # Appel d'une fonction ou méthode spécifique
        or os.path.isabs(normalized_name)
        # Exécution de cette instruction spécifique
        or "/" in normalized_name
        # Exécution de cette instruction spécifique
        or "\\" in normalized_name
    # Exécution de cette instruction spécifique
    ):
        # Enregistre une trace (log) pour le suivi de l'application
        logger.warning(
            # Exécution de cette instruction spécifique
            f"Nom de fichier rejeté (chemin invalide) : {filename}"
        )
        # Ouverture d'une structure de données ou d'une fonction
        raise HTTPException(
            # Affectation de la valeur à la variable 'status_code'
            status_code=400,
            # Affectation de la valeur à la variable 'detail'
            detail="Nom de document invalide."
        )

    # Affectation de la valeur à la variable 'clean_name'
    clean_name = normalized_name

    # Autoriser uniquement les noms de fichiers PDF attendus
    if (
        # Exécution de cette instruction spécifique
        len(clean_name) > 255
        # Initialisation et affectation d'une variable
        or len(clean_name) <= 4
        # Appel d'une fonction ou méthode spécifique
        or not clean_name.lower().endswith(".pdf")
        # Appel d'une fonction ou méthode spécifique
        or not clean_name[0].isalnum()
        # Ouverture d'une structure de données ou d'une fonction
        or not all(
            # Exécution de cette instruction spécifique
            char.isalnum() or char in "._-"
            # Boucle itérative pour parcourir les éléments
            for char in clean_name
        )
    # Exécution de cette instruction spécifique
    ):
        # Enregistre une trace (log) pour le suivi de l'application
        logger.warning(
            # Exécution de cette instruction spécifique
            f"Nom de fichier rejeté (format invalide ou tentative de traversal) : {filename}"
        )
        # Ouverture d'une structure de données ou d'une fonction
        raise HTTPException(
            # Affectation de la valeur à la variable 'status_code'
            status_code=400,
            # Affectation de la valeur à la variable 'detail'
            detail="Nom de document invalide."
        )

    # Résolution canonique absolue du répertoire documentaire
    doc_dir_real = os.path.realpath(DOCUMENTS_DIR)

    # Répertoire de confiance : inventaire des PDF réels présents sur le disque (OWASP Whitelist)
    # Les chemins et noms certifiés proviennent exclusivement de la lecture du répertoire serveur.
    trusted_documents: dict[str, tuple[str, str]] = {}
    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # Exécution de cette instruction spécifique
        with os.scandir(doc_dir_real) as entries:
            # Boucle itérative pour parcourir les éléments
            for entry in entries:
                # Structure conditionnelle : on vérifie si la condition est vraie
                if entry.is_file() and entry.name.lower().endswith(".pdf"):
                    # Affectation de la valeur à la variable 'canonical_path'
                    canonical_path = os.path.realpath(entry.path)
                    # Confinement strict dans DOCUMENTS_DIR
                    if canonical_path.startswith(doc_dir_real + os.sep):
                        # Initialisation et affectation d'une variable
                        trusted_documents[entry.name] = (canonical_path, entry.name)
    # Capture et gestion de l'erreur si le bloc try échoue
    except OSError as err:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.error(f"Erreur d'accès au répertoire documentaire : {err}")
        # Ouverture d'une structure de données ou d'une fonction
        raise HTTPException(
            # Affectation de la valeur à la variable 'status_code'
            status_code=500,
            # Affectation de la valeur à la variable 'detail'
            detail="Erreur interne d'accès aux documents."
        )

    # Validation par comparaison exacte dans la liste blanche
    matched = trusted_documents.get(clean_name)
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not matched:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.warning(
            # Exécution de cette instruction spécifique
            f"Document demandé non trouvé dans la liste blanche autorisée : {filename}"
        )
        # Ouverture d'une structure de données ou d'une fonction
        raise HTTPException(
            # Affectation de la valeur à la variable 'status_code'
            status_code=404,
            # Affectation de la valeur à la variable 'detail'
            detail="Document introuvable."
        )

    # Affectation de la valeur à la variable 'safe_path'
    safe_path, canonical_name = matched
    # Renvoie le résultat final de cette fonction
    return safe_path, canonical_name

@app.get("/documents/{filename}")
@app.get("/ia/documents/{filename}")
# Définition de la fonction asynchrone get_document_pdf
async def get_document_pdf(filename: str):
    """
    Sert les documents cliniques PDF officiels (Guide MSF/OMS, Dorosz)
    avec support du streaming, des octets et de l'ancrage direct par page (#page=X).
    """
    # Affectation de la valeur à la variable 'file_path'
    file_path, clean_name = get_safe_document_path(filename)
    
    # Renvoie le résultat final de cette fonction
    return FileResponse(
        # Affectation de la valeur à la variable 'path'
        path=file_path,
        # Affectation de la valeur à la variable 'media_type'
        media_type="application/pdf",
        # Affectation de la valeur à la variable 'headers'
        headers={
            # Initialisation et affectation d'une variable
            "Content-Disposition": f'inline; filename="{clean_name}"',
            # Exécution de cette instruction spécifique
            "Accept-Ranges": "bytes"
        }
    )

@app.get("/documents/view/{filename}", response_class=HTMLResponse)
@app.get("/ia/documents/view/{filename}", response_class=HTMLResponse)
# Définition de la fonction asynchrone view_document_page
async def view_document_page(filename: str, page: int = 1):
    """
    Visualiseur web certifié Diam Yaraam intégrant le PDF directement centré sur la page officielle.
    Protégé contre le Path Traversal (CWE-22) et le Reflected XSS (CWE-79).
    """
    # Affectation de la valeur à la variable 'file_path'
    file_path, clean_name = get_safe_document_path(filename)

    # Affectation de la valeur à la variable 'safe_page'
    safe_page = max(1, int(page))
    # Affectation de la valeur à la variable 'escaped_doc_name'
    escaped_doc_name = html.escape(clean_name)
    # Affectation de la valeur à la variable 'encoded_url_doc'
    encoded_url_doc = urllib.parse.quote(clean_name, safe="")
    # Affectation de la valeur à la variable 'pdf_view_url'
    pdf_view_url = f"/documents/{encoded_url_doc}#page={safe_page}&zoom=100"
    # Affectation de la valeur à la variable 'escaped_pdf_view_url'
    escaped_pdf_view_url = html.escape(pdf_view_url, quote=True)

    html_content = f"""
    <!DOCTYPE html>
    <html lang="fr">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Diam Yaraam — Source Médicale Officielle (Page {safe_page})</title>
        <style>
            * {{ box-sizing: border-box; }}
            body, html {{ margin: 0; padding: 0; height: 100%; overflow: hidden; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background: #0F172A; }}
            .header {{ height: 52px; background: #1E293B; color: white; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; border-bottom: 1px solid #334155; }}
            .title {{ font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 10px; }}
            .badge-page {{ background: #0D7C66; color: white; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: bold; }}
            .doc-name {{ color: #94A3B8; font-size: 13px; }}
            .btn {{ background: #0D7C66; color: white; text-decoration: none; padding: 7px 14px; border-radius: 8px; font-size: 13px; font-weight: 600; }}
            .btn:hover {{ background: #095949; }}
            iframe {{ width: 100%; height: calc(100% - 52px); border: none; background: #525659; }}
        </style>
    </head>
    <body>
        <div class="header">
            <div class="title">
                <span>📖 Diam Yaraam — Référentiel Médical Officiel</span>
                <span class="badge-page">Page {safe_page}</span>
                <span class="doc-name">{escaped_doc_name}</span>
            </div>
            <div>
                <a class="btn" href="{escaped_pdf_view_url}" target="_blank">Ouvrir dans le lecteur PDF (Page {safe_page}) ↗</a>
            </div>
        </div>
        <iframe src="{escaped_pdf_view_url}"></iframe>
    </body>
    </html>
    """
    # Renvoie le résultat final de cette fonction
    return HTMLResponse(
        # Affectation de la valeur à la variable 'content'
        content=html_content,
        # Affectation de la valeur à la variable 'headers'
        headers={"Content-Security-Policy": "default-src 'self'; style-src 'self' 'unsafe-inline'; frame-src 'self';"}
    )

# Structure conditionnelle : on vérifie si la condition est vraie
if __name__ == "__main__":
    # Importation de la librairie uvicorn
    import uvicorn
    # Affectation de la valeur à la variable 'port'
    port = int(os.getenv("IA_SERVICE_PORT", "8089"))
    # Initialisation et affectation d'une variable
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
