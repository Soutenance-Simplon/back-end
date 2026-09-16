"""
Serveur principal FastAPI — Diam Yaraam IA Service
Alimenté par OpenRouter (OpenAI GPT-4o-mini) & RAG Dorosz pour le triage médical, le chat sécurisé et l'analyse d'ordonnances.
"""

import os
import uuid
import json
import base64
import logging
from datetime import datetime
from typing import List, Dict, Optional, Any
from fastapi import FastAPI, HTTPException, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

# Configurer les logs
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ia-service.main")

# Import des modules internes OpenRouter et RAG
from llm_client import (
    chat_completion,
    triage_symptoms_llm,
    analyze_interactions_llm,
    check_openrouter_health,
    LLM_MODEL
)
from rag_engine import rag_engine

app = FastAPI(
    title="Diam Yaraam IA Service",
    description="Service d'Intelligence Artificielle pour la télémédecine, le triage et la recherche clinique (OpenRouter / GPT-4o-mini)",
    version="2.0.0"
)

# Configuration CORS pour autoriser l'accès depuis le Gateway et le frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─── Modèles de requêtes et de réponses ─────────────────────────────────────

class TriageRequest(BaseModel):
    description: str

class TriageResponse(BaseModel):
    description_symptomes: str
    niveau_gravite: str  # VERT, JAUNE, ORANGE, ROUGE
    orientation_suggeree: str
    conseil_immediat: str
    questions_suivi: List[str]

class ChatMessage(BaseModel):
    id: Optional[str] = None
    contenu: str
    estUtilisateur: bool
    timestamp: Optional[str] = None
    recommandations: Optional[List[str]] = None

class ChatRequest(BaseModel):
    message: str
    history: List[dict] = []

class ChatResponse(BaseModel):
    id: str
    contenu: str
    estUtilisateur: bool
    timestamp: str
    recommandations: List[str]
    specialites_suggerees: Optional[List[str]] = None

class InteractionRequest(BaseModel):
    medicaments: List[str] = []
    allergies: List[str] = []

class InteractionResponse(BaseModel):
    medicament1: str
    medicament2: str
    niveau_danger: str  # CONTRE_INDICATION_ABSOLUE, MAJEURE, MODEREE, MINEURE
    explication: str
    recommandation: str

# ─── Helpers ───────────────────────────────────────────────────────────────

def extract_role_from_token(auth_header: Optional[str]) -> str:
    """
    Extrait de manière sécurisée le rôle du token JWT propagé par le Gateway.
    Retourne 'PATIENT' par défaut (sécurité maximale).
    """
    if not auth_header or not auth_header.lower().startswith("bearer "):
        return "PATIENT"
    try:
        token = auth_header.split(" ")[1]
        payload_segment = token.split(".")[1]
        # Padding base64
        padding = '=' * (4 - len(payload_segment) % 4)
        decoded_bytes = base64.urlsafe_b64decode(payload_segment + padding)
        payload = json.loads(decoded_bytes)
        return payload.get("role", "PATIENT").upper()
    except Exception as e:
        logger.warning(f"Impossible de décoder le rôle du JWT: {e}. Rôle PATIENT par défaut.")
        return "PATIENT"

# ─── Endpoints ──────────────────────────────────────────────────────────────

@app.get("/health")
async def health_check():
    """Vérifie le statut de santé du service et du provider LLM OpenRouter."""
    llm_health = await check_openrouter_health()
    return {
        "status": "healthy",
        "service": "diam-yaraam-ia-service",
        "version": "2.0.0",
        "timestamp": datetime.utcnow().isoformat(),
        "rag_database_loaded": rag_engine.is_loaded,
        "llm_provider": llm_health
    }

@app.post("/triage", response_model=TriageResponse)
async def triage_symptomes(req: TriageRequest):
    """
    Évalue la gravité des symptômes décrits par l'utilisateur avec GPT-4o-mini.
    """
    logger.info(f"Requête de triage reçue : {req.description[:100]}...")
    result = await triage_symptoms_llm(req.description)
    return TriageResponse(**result)

@app.post("/chat", response_model=ChatResponse)
async def chat_assistant(req: ChatRequest, authorization: Optional[str] = Header(None)):
    """
    Assistant conversationnel médical intelligent.
    Intègre le RAG sur le guide Dorosz et adapte le niveau médical selon le rôle (Médecin vs Patient).
    """
    role = extract_role_from_token(authorization)
    logger.info(f"Requête chat reçue pour le rôle: {role} (message: {req.message[:50]}...)")
    
    # 1. Recherche RAG dans le Dorosz
    contexte_documentaire = rag_engine.search(req.message, top_k=2)
    
    # 2. Construction du prompt système adapté au rôle
    if role in ("MEDECIN", "ADMIN"):
        system_content = (
            "Vous êtes l'assistant de recherche clinique et pharmacologique de la plateforme Diam Yaraam destiné aux médecins et professionnels de santé agréés du Sénégal (ONMS).\n"
            "Votre objectif est d'aider le médecin avec des synthèses cliniques précises, posologies d'usage, mécanismes d'action et alternatives thérapeutiques.\n"
            "Vous pouvez utiliser librement les extraits du guide médical Dorosz fournis ci-dessous.\n"
            "Soyez précis, scientifique, structuré et professionnel."
        )
    else:
        system_content = (
            "Vous êtes l'assistant médical bienveillant de la plateforme de télémédecine Diam Yaraam (Sénégal), s'adressant à un patient.\n"
            "CONSIGNES STRICTES DE SÉCURITÉ :\n"
            "1. Interdiction formelle de prescrire ou d'inciter à l'automédication avec des médicaments soumis à prescription.\n"
            "2. Rappelez systématiquement et calmement de consulter un médecin pour toute confirmation diagnostique ou prescription.\n"
            "3. En cas de détresse ou signe de gravité (douleur poitrine, détresse respiratoire, coma), donnez immédiatement les consignes de premiers secours et d'appeler le SAMU (1515).\n"
            "4. Répondez de manière chaleureuse, claire, rassurante et facile à comprendre.\n"
            "IMPORTANT - FORMAT DE REPONSE :\n"
            "Vous devez OBLIGATOIREMENT répondre au format JSON strict avec les clés suivantes :\n"
            "{\n"
            '  "reponse": "Votre message chaleureux au patient",\n'
            '  "specialites_suggerees": ["Cardiologie"] // Si les symptômes justifient une consultation, indiquez la ou les spécialités adéquates (ex: Cardiologie, Dentiste, Médecine Générale, Pédiatrie). Sinon, mettez un tableau vide [].\n'
            "}"
        )

    if contexte_documentaire and "Aucune base" not in contexte_documentaire:
        system_content += f"\n\nExtraits de la base de connaissances médicale (Guide Dorosz) :\n{contexte_documentaire}"

    messages = [{"role": "system", "content": system_content}]

    # Ajouter l'historique
    for msg in req.history[-6:]:
        role_label = "user" if msg.get("estUtilisateur", False) else "assistant"
        content = msg.get("contenu", "")
        if content:
            messages.append({"role": role_label, "content": content})

    messages.append({"role": "user", "content": req.message})

    # 3. Génération de la réponse via OpenRouter (GPT-4o-mini)
    is_patient = role not in ("MEDECIN", "ADMIN")
    reponse_texte = await chat_completion(
        messages=messages, 
        temperature=0.7, 
        max_tokens=600,
        response_format={"type": "json_object"} if is_patient else None
    )
    
    specialites = []
    if is_patient and reponse_texte:
        try:
            data = json.loads(reponse_texte)
            reponse_texte = data.get("reponse", "Désolé, je n'ai pas pu formuler ma réponse.")
            specialites = data.get("specialites_suggerees", [])
        except Exception as e:
            logger.error(f"Erreur de parsing JSON pour le chat patient: {e}")
            reponse_texte = "Bonjour. Je rencontre une difficulté technique. S'il s'agit d'une urgence, appelez le 1515."
    
    if not reponse_texte:
        if role in ("MEDECIN", "ADMIN"):
            reponse_texte = "Désolé, une indisponibilité momentanée du service d'intelligence artificielle est survenue. Veuillez vérifier vos paramètres ou rechercher directement dans vos documents locaux."
        else:
            reponse_texte = "Bonjour. Je rencontre une difficulté momentanée pour traiter votre demande. S'il s'agit d'une urgence, contactez sans tarder le 1515. Sinon, n'hésitez pas à prendre rendez-vous avec un médecin sur Diam Yaraam."

    # Suggestions de boutons d'action adaptées
    if role in ("MEDECIN", "ADMIN"):
        recommandations = ["Consulter le Dorosz complet", "Vérifier les interactions de l'ordonnance"]
    else:
        recommandations = ["Prendre rendez-vous en téléconsultation", "Trouver un médecin proche"]
        lower_msg = req.message.lower()
        if any(k in lower_msg for k in ["coeur", "poitrine", "étouff", "respir", "urgence"]):
            recommandations = ["Appeler le SAMU (1515)", "S'allonger en position semi-assise"]

    return ChatResponse(
        id=f"ia-{uuid.uuid4().hex[:8]}",
        contenu=reponse_texte,
        estUtilisateur=False,
        timestamp=datetime.now().isoformat(),
        recommandations=recommandations,
        specialites_suggerees=specialites if is_patient else None
    )

@app.post("/interactions", response_model=List[InteractionResponse])
async def verifier_interactions(req: InteractionRequest):
    """
    Vérifie les interactions nocives entre une liste de médicaments prescrits
    et les allergies déclarées du patient avec OpenRouter GPT-4o-mini.
    """
    logger.info(f"Requête de vérification d'interactions reçue : Médicaments={req.medicaments}, Allergies={req.allergies}")
    
    raw_results = await analyze_interactions_llm(req.medicaments, req.allergies)
    
    response_list = []
    for item in raw_results:
        response_list.append(InteractionResponse(
            medicament1=item.get("medicament1", "Inconnu"),
            medicament2=item.get("medicament2", "Allergie/Médicament"),
            niveau_danger=item.get("niveau_danger", "MODEREE"),
            explication=item.get("explication", "Risque d'interaction."),
            recommandation=item.get("recommandation", "Demander l'avis d'un professionnel de santé.")
        ))
    return response_list

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("IA_SERVICE_PORT", "8089"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
