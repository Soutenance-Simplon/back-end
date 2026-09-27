"""
Serveur principal FastAPI — Diam Yaraam IA Service
Alimenté par OpenRouter (OpenAI GPT-4o-mini) & Moteur RAG Clinique (OMS SPU / Guide MSF / Référentiel ONDMS)
pour le triage vital, le chat sécurisé et l'analyse d'ordonnances en temps réel.
"""

import os
import uuid
import json
import base64
import html
import re
import urllib.parse
import logging
from datetime import datetime
from typing import List, Dict, Optional, Any
from fastapi import FastAPI, HTTPException, Header
from fastapi.responses import FileResponse, HTMLResponse
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
    description="Service d'Intelligence Artificielle pour la télémédecine, le triage d'urgence SAMU et la pharmacovigilance MSF",
    version="2.1.0"
)

# Configuration CORS sécurisée avec origines explicites et sans wildcard permissif
cors_origins_env = os.getenv(
    "CORS_ALLOWED_ORIGINS",
    "http://localhost:5173,http://localhost:8088,http://localhost:3000,http://localhost:8090"
)
allowed_origins_list = [o.strip() for o in cors_origins_env.split(",") if o.strip()]

app.add_middleware(
    CORSMiddleware,
    allow_origins=allowed_origins_list,
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"],
)

# ─── Modèles de requêtes et de réponses ─────────────────────────────────────

class TriageRequest(BaseModel):
    description: str

class TriageResponse(BaseModel):
    description_symptomes: str
    niveau_gravite: str  # VERT, JAUNE, ORANGE, ROUGE
    niveau_urgence: Optional[str] = "MODERE"  # FAIBLE, MODERE, URGENT, SAMU
    est_urgence_vitale: bool = False
    orientation_suggeree: str
    specialites_suggerees: List[str] = []
    justification_orientation: Optional[str] = None
    source_medicale: Optional[str] = None
    numero_urgence: Optional[str] = "1515"
    conseil_immediat: str
    premiers_gestes: List[str] = []
    rappel_legal: Optional[str] = None
    red_flags: List[str] = []
    questions_suivi: List[str] = []

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
    recommandations: List[str] = []
    specialites_suggerees: Optional[List[str]] = None
    est_urgence_vitale: bool = False
    niveau_urgence: Optional[str] = "MODERE"  # SAMU, URGENT, MODERE, FAIBLE
    source_medicale: Optional[str] = None
    numero_urgence: Optional[str] = None
    premiers_gestes: List[str] = []
    red_flags: List[str] = []
    justification_orientation: Optional[str] = None
    rappel_legal: Optional[str] = None

class InteractionRequest(BaseModel):
    medicaments: List[str] = []
    allergies: List[str] = []

class InteractionResponse(BaseModel):
    medicament1: str
    medicament2: str
    niveau_danger: str  # CONTRE_INDICATION_ABSOLUE, MAJEURE, MODEREE, MINEURE
    bloquant: bool = False
    source_medicale: Optional[str] = None
    page_numero: Optional[int] = None
    document_url: Optional[str] = None
    document_nom: Optional[str] = None
    explication: str
    recommandation: str
    alternative_recommandee: Optional[str] = None

# ─── Helpers ───────────────────────────────────────────────────────────────

def extract_role_from_token(auth_header: Optional[str]) -> str:
    """
    Extrait de manière sécurisée le rôle du token JWT propagé par le Gateway.
    Retourne 'PATIENT' par défaut.
    """
    if not auth_header or not auth_header.lower().startswith("bearer "):
        return "PATIENT"
    try:
        token = auth_header.split(" ")[1]
        payload_segment = token.split(".")[1]
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
    """Vérifie le statut de santé du service, du RAG et d'OpenRouter."""
    llm_health = await check_openrouter_health()
    return {
        "status": "healthy",
        "service": "diam-yaraam-ia-service",
        "version": "2.1.0",
        "timestamp": datetime.utcnow().isoformat(),
        "rag_database_loaded": rag_engine.is_loaded,
        "rag_red_flags_count": len(rag_engine.red_flags_db),
        "rag_specialties_count": len(rag_engine.specialties_db),
        "rag_msf_interactions_count": len(rag_engine.drug_interactions_db),
        "llm_provider": llm_health
    }

@app.post("/triage", response_model=TriageResponse)
@app.post("/ia/triage", response_model=TriageResponse)
async def triage_symptomes(req: TriageRequest):
    """
    Évalue la gravité des symptômes décrits par le patient.
    Déclenche l'alerte vitale SAMU 1515 ou oriente vers le spécialiste agréé ONDMS.
    """
    logger.info(f"Requête de triage reçue : {req.description[:80]}...")
    result = await triage_symptoms_llm(req.description)
    return TriageResponse(**result)

@app.post("/chat", response_model=ChatResponse)
@app.post("/ia/chat", response_model=ChatResponse)
async def chat_assistant(req: ChatRequest, authorization: Optional[str] = Header(None)):
    """
    Assistant conversationnel médical intelligent.
    1. Si rôle Patient : vérifie d'abord les Red Flags d'urgence (SAMU 1515)
       puis oriente vers la bonne spécialité selon regles_orientation_medicale.md.
    2. Si rôle Médecin : copilote de recherche clinique et pharmacologique.
    """
    role = extract_role_from_token(authorization)
    logger.info(f"Requête chat reçue pour le rôle: {role} (message: {req.message[:50]}...)")
    
    is_patient = role not in ("MEDECIN", "ADMIN", "DOCTEUR")

    # 1. ANALYSE CLINIQUE RAG EN PRIORITÉ
    rag_eval = rag_engine.evaluate_triage_and_orientation(req.message)

    # 2. CAS URGENCE VITALE ABSOLUE (Drapeau Rouge Patient)
    if is_patient and rag_eval["est_urgence_vitale"]:
        logger.warning(f"🚨 URGENCE VITALE DÉTECTÉE DANS LE CHAT : {rag_eval['condition_detectee']}")
        reponse_urgence = (
            f"🚨 **ALERTE URGENCE VITALE — SAMU NATIONAL (1515)**\n\n"
            f"Vos symptômes constituent un **signal d'alarme vital immédiat** : {rag_eval['condition_detectee']}.\n\n"
            f"Conformément à la Section 5.1 du Référentiel Médical Diam Yaraam et au protocole d'urgence OMS SPU, "
            f"**le parcours classique de téléconsultation est immédiatement interrompu**.\n\n"
            f"📞 **Composez sans délai le 1515 (SAMU National)** pour une prise en charge urgente."
        )
        return ChatResponse(
            id=f"ia-alert-{uuid.uuid4().hex[:8]}",
            contenu=reponse_urgence,
            estUtilisateur=False,
            timestamp=datetime.now().isoformat(),
            recommandations=[
                "Appeler immédiatement le SAMU (1515)",
                "Adopter une position semi-assise au repos strict",
                "Ne rien boire ni manger en attendant les secours",
                "Déverrouiller la porte d'entrée pour les secours"
            ],
            specialites_suggerees=["SAMU 1515 / Urgences Hospitalières"],
            est_urgence_vitale=True,
            niveau_urgence="SAMU",
            source_medicale=rag_eval["source_medicale"],
            numero_urgence="1515",
            premiers_gestes=rag_eval["premiers_gestes"],
            red_flags=rag_eval["red_flags"],
            justification_orientation=rag_eval["justification_orientation"],
            rappel_legal=rag_eval["rappel_legal"]
        )

    # 3. CONVERSATION NORMALE ORIENTÉE SPÉCIALITÉS ONDMS
    if not is_patient:
        system_content = (
            "Vous êtes l'assistant de recherche clinique et pharmacologique de la plateforme Diam Yaraam destiné aux médecins agréés de l'ONDMS (Sénégal).\n"
            "Votre objectif est d'aider le confrère avec des synthèses cliniques précises, posologies d'usage MSF/Dorosz et interactions thérapeutiques.\n"
            "Soyez précis, scientifique, structuré et professionnel."
        )
    else:
        system_content = (
            "Vous êtes l'assistant médical bienveillant de la plateforme de télémédecine Diam Yaraam (Sénégal), s'adressant à un patient.\n"
            "CONSIGNES STRICTES DE SÉCURITÉ ET D'ORIENTATION :\n"
            "1. Interdiction formelle de poser un diagnostic médical ou de prescrire des médicaments.\n"
            f"2. Spécialité recommandée selon le Référentiel Diam Yaraam : {rag_eval['specialites_suggerees'][0]}.\n"
            f"3. Justification de la règle appliquée : {rag_eval['justification_orientation']}.\n"
            f"4. Rappel légal obligatoire : {rag_eval['rappel_legal']}.\n"
            "5. Répondez de manière chaleureuse, empathique, rassurante et facile à comprendre.\n"
            "IMPORTANT - FORMAT DE RÉPONSE JSON OBLIGATOIRE :\n"
            "{\n"
            '  "reponse": "Votre message chaleureux au patient expliquant calmement la situation et recommandant de consulter un spécialiste",\n'
            f'  "specialites_suggerees": ["{rag_eval["specialites_suggerees"][0]}"],\n'
            f'  "justification_orientation": "{rag_eval["justification_orientation"]}",\n'
            f'  "source_medicale": "{rag_eval["source_medicale"]}",\n'
            f'  "rappel_legal": "{rag_eval["rappel_legal"]}",\n'
            '  "recommandations": ["Prendre rendez-vous sur Diam Yaraam", "Surveiller l\'évolution des symptômes"]\n'
            "}"
        )

    contexte_doc = rag_engine.search(req.message)
    if contexte_doc:
        system_content += f"\n\nContexte réglementaire et documentaire actif :\n{contexte_doc}"

    messages = [{"role": "system", "content": system_content}]

    # Ajouter l'historique
    for msg in req.history[-6:]:
        role_label = "user" if msg.get("estUtilisateur", False) else "assistant"
        content = msg.get("contenu", "")
        if content:
            messages.append({"role": role_label, "content": content})

    messages.append({"role": "user", "content": req.message})

    # Génération LLM
    reponse_texte = await chat_completion(
        messages=messages, 
        temperature=0.4, 
        max_tokens=600,
        response_format={"type": "json_object"} if is_patient else None
    )

    specialites = rag_eval["specialites_suggerees"]
    recommandations = [
        f"Consulter un spécialiste en {specialites[0]}",
        "Prendre rendez-vous en téléconsultation sur Diam Yaraam"
    ]
    justif = rag_eval["justification_orientation"]
    legal = rag_eval["rappel_legal"]

    if is_patient and reponse_texte:
        try:
            data = json.loads(reponse_texte)
            reponse_texte = data.get("reponse", reponse_texte)
            if data.get("specialites_suggerees"):
                specialites = data["specialites_suggerees"]
            if data.get("recommandations"):
                recommandations = data["recommandations"]
            justif = data.get("justification_orientation", justif)
            legal = data.get("rappel_legal", legal)
        except Exception as e:
            logger.error(f"Erreur parsing JSON chat patient: {e}")

    if not reponse_texte:
        reponse_texte = (
            f"Bonjour. D'après les symptômes que vous décrivez, une consultation en **{specialites[0]}** est conseillée. "
            f"{justif} N'hésitez pas à choisir un médecin agréé sur Diam Yaraam."
        )

    return ChatResponse(
        id=f"ia-{uuid.uuid4().hex[:8]}",
        contenu=reponse_texte,
        estUtilisateur=False,
        timestamp=datetime.now().isoformat(),
        recommandations=recommandations,
        specialites_suggerees=specialites,
        est_urgence_vitale=False,
        niveau_urgence="MODERE",
        source_medicale=rag_eval["source_medicale"],
        justification_orientation=justif,
        rappel_legal=legal
    )

@app.post("/interactions", response_model=List[InteractionResponse])
@app.post("/ia/interactions", response_model=List[InteractionResponse])
async def verifier_interactions(req: InteractionRequest):
    """
    Vérifie les interactions nocives et contre-indications absolues
    entre la liste de médicaments prescrits et les allergies déclarées du patient,
    avec citation du Guide des Médicaments Essentiels MSF/OMS.
    """
    logger.info(f"Requête de vérification d'interactions reçue : Médicaments={req.medicaments}, Allergies={req.allergies}")
    
    raw_results = await analyze_interactions_llm(req.medicaments, req.allergies)
    
    response_list = []
    for item in raw_results:
        page = item.get("page_numero")
        if not page:
            import re
            src = str(item.get("source_medicale", ""))
            match = re.search(r'(?:p\.|page\s*)(\d+)', src, re.IGNORECASE)
            if match:
                page = int(match.group(1))
            else:
                page = 38
        
        doc_nom = item.get("document_nom") or "guideline-339-fr.pdf"
        doc_url = item.get("document_url") or f"http://127.0.0.1:8089/ia/documents/view/{doc_nom}?page={page}"

        response_list.append(InteractionResponse(
            medicament1=item.get("medicament1", "Médicament"),
            medicament2=item.get("medicament2", "Interaction/Allergie"),
            niveau_danger=item.get("niveau_danger", "MODEREE"),
            bloquant=item.get("bloquant", False),
            source_medicale=item.get("source_medicale", "Guide Médicaments Essentiels MSF/OMS"),
            page_numero=page,
            document_url=doc_url,
            document_nom=doc_nom,
            explication=item.get("explication", "Risque d'interaction médicamenteuse."),
            recommandation=item.get("recommandation", "Vérifier la posologie ou demander l'avis d'un confrère."),
            alternative_recommandee=item.get("alternative_recommandee")
        ))
    return response_list

# ─── Consultation des Preuves Documentaires (PDF) ───────────────────────────

DOCUMENTS_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "documents"))

def get_safe_document_path(filename: str) -> tuple[str, str]:
    """
    Valide et résout de manière sécurisée un fichier PDF dans DOCUMENTS_DIR.
    Protège contre les attaques de type Path Traversal (CWE-22) :
    - Refus des chemins absolus et des séparateurs de répertoires
    - Validation stricte du format du nom de fichier via regex
    - Résolution canonique absolue du chemin candidat
    - Vérification d'inclusion stricte dans le répertoire documentaire
    """
    normalized_name = os.path.normpath(filename)

    # Refuser tout chemin ou nom permettant une navigation dans l'arborescence
    if (
        normalized_name in (".", "")
        or os.path.isabs(normalized_name)
        or "/" in normalized_name
        or "\\" in normalized_name
    ):
        logger.warning(
            f"Nom de fichier rejeté (chemin invalide) : {filename}"
        )
        raise HTTPException(
            status_code=400,
            detail="Nom de document invalide."
        )

    clean_name = normalized_name

    # Autoriser uniquement les noms de fichiers PDF attendus
    if (
        len(clean_name) > 255
        or len(clean_name) <= 4
        or not clean_name.lower().endswith(".pdf")
        or not clean_name[0].isalnum()
        or not all(
            char.isalnum() or char in "._-"
            for char in clean_name
        )
    ):
        logger.warning(
            f"Nom de fichier rejeté (format invalide ou tentative de traversal) : {filename}"
        )
        raise HTTPException(
            status_code=400,
            detail="Nom de document invalide."
        )

    # Résolution canonique absolue du répertoire documentaire
    doc_dir_real = os.path.realpath(DOCUMENTS_DIR)

    # Répertoire de confiance : inventaire des PDF réels présents sur le disque (OWASP Whitelist)
    # Les chemins et noms certifiés proviennent exclusivement de la lecture du répertoire serveur.
    trusted_documents: dict[str, tuple[str, str]] = {}
    try:
        with os.scandir(doc_dir_real) as entries:
            for entry in entries:
                if entry.is_file() and entry.name.lower().endswith(".pdf"):
                    canonical_path = os.path.realpath(entry.path)
                    # Confinement strict dans DOCUMENTS_DIR
                    if canonical_path.startswith(doc_dir_real + os.sep):
                        trusted_documents[entry.name] = (canonical_path, entry.name)
    except OSError as err:
        logger.error(f"Erreur d'accès au répertoire documentaire : {err}")
        raise HTTPException(
            status_code=500,
            detail="Erreur interne d'accès aux documents."
        )

    # Validation par comparaison exacte dans la liste blanche
    matched = trusted_documents.get(clean_name)
    if not matched:
        logger.warning(
            f"Document demandé non trouvé dans la liste blanche autorisée : {filename}"
        )
        raise HTTPException(
            status_code=404,
            detail="Document introuvable."
        )

    safe_path, canonical_name = matched
    return safe_path, canonical_name

@app.get("/documents/{filename}")
@app.get("/ia/documents/{filename}")
async def get_document_pdf(filename: str):
    """
    Sert les documents cliniques PDF officiels (Guide MSF/OMS, Dorosz)
    avec support du streaming, des octets et de l'ancrage direct par page (#page=X).
    """
    file_path, clean_name = get_safe_document_path(filename)
    
    return FileResponse(
        path=file_path,
        media_type="application/pdf",
        headers={
            "Content-Disposition": f'inline; filename="{clean_name}"',
            "Accept-Ranges": "bytes"
        }
    )

@app.get("/documents/view/{filename}", response_class=HTMLResponse)
@app.get("/ia/documents/view/{filename}", response_class=HTMLResponse)
async def view_document_page(filename: str, page: int = 1):
    """
    Visualiseur web certifié Diam Yaraam intégrant le PDF directement centré sur la page officielle.
    Protégé contre le Path Traversal (CWE-22) et le Reflected XSS (CWE-79).
    """
    file_path, clean_name = get_safe_document_path(filename)

    safe_page = max(1, int(page))
    escaped_doc_name = html.escape(clean_name)
    encoded_url_doc = urllib.parse.quote(clean_name, safe="")
    pdf_view_url = f"/documents/{encoded_url_doc}#page={safe_page}&zoom=100"
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
    return HTMLResponse(
        content=html_content,
        headers={"Content-Security-Policy": "default-src 'self'; style-src 'self' 'unsafe-inline'; frame-src 'self';"}
    )

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("IA_SERVICE_PORT", "8089"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
