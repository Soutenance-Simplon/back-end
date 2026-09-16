"""
Client OpenRouter LLM — Diam Yaraam IA Service
Gère les appels vers les modèles LLM de pointe (OpenAI GPT-4o-mini, etc.) via OpenRouter.
"""

import os
import json
import logging
import httpx
from typing import List, Dict, Any, Optional
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

logger = logging.getLogger("ia-service.llm_client")

# ─── Configuration ───────────────────────────────────────────────────────────

OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"

def get_api_key() -> str:
    return os.getenv("OPENROUTER_API_KEY", "").strip('"').strip("'")

def get_model() -> str:
    return os.getenv("LLM_MODEL", "openai/gpt-4o-mini").strip('"').strip("'")

LLM_MODEL = get_model()

# Timeout pour les requêtes OpenRouter
TIMEOUT = 45.0


def _headers() -> dict:
    """Headers d'authentification pour l'API OpenRouter."""
    api_key = get_api_key()
    headers = {
        "Content-Type": "application/json",
        "HTTP-Referer": "https://diam-yaraam.sn",
        "X-Title": "Diam Yaraam Telemedicine AI"
    }
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"
    return headers



# ─── Appel générique OpenRouter Chat ──────────────────────────────────────────

async def chat_completion(
    messages: List[Dict[str, str]],
    model: Optional[str] = None,
    temperature: float = 0.7,
    max_tokens: int = 800,
    response_format: Optional[dict] = None,
) -> str:
    """
    Appelle le endpoint de chat completions d'OpenRouter.
    """
    target_model = model or LLM_MODEL
    payload: Dict[str, Any] = {
        "model": target_model,
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
    }
    if response_format:
        payload["response_format"] = response_format

    try:
        async with httpx.AsyncClient(timeout=TIMEOUT) as client:
            resp = await client.post(
                OPENROUTER_API_URL,
                json=payload,
                headers=_headers(),
            )
            resp.raise_for_status()
            data = resp.json()
            choices = data.get("choices", [])
            if choices and len(choices) > 0:
                message = choices[0].get("message", {})
                return message.get("content", "").strip()
            return ""
    except httpx.HTTPStatusError as e:
        logger.error(f"Erreur HTTP OpenRouter ({e.response.status_code}): {e.response.text}")
        return ""
    except Exception as e:
        logger.error(f"Erreur appel OpenRouter: {e}")
        return ""


# ─── Triage intelligent des symptômes ────────────────────────────────────────

async def triage_symptoms_llm(description: str) -> Dict[str, Any]:
    """
    Analyse les symptômes décrits par le patient et renvoie une classification structurée.
    Gravités possibles : ROUGE (Urgence vitale), ORANGE (Rapide), JAUNE (Consultation normale), VERT (Bénin).
    """
    system_prompt = (
        "Vous êtes un médecin urgentiste et algorithme expert de régulation médicale d'urgence (type SAMU 1515).\n"
        "Analysez les symptômes décrits par le patient et classez la gravité selon l'échelle suivante :\n"
        "- ROUGE : Urgence vitale absolue (douleur thoracique constrictive, détresse respiratoire aiguë, AVC, perte de connaissance, hémorragie massive).\n"
        "- ORANGE : Urgence relative / Consultation rapide requise sous 2-6 heures (fièvre très élevée mal tolérée, douleur abdominale aiguë, plaie importante).\n"
        "- JAUNE : Consultation normale / Téléconsultation requise sous 24-48h (syndrome grippal, paludisme suspecté, douleurs modérées chroniques).\n"
        "- VERT : Symptômes bénins / Automédication encadrée ou repos (rhume débutant, fatigue passagère).\n\n"
        "Vous devez OBLIGATOIREMENT répondre au format JSON strict avec exactement ces clés :\n"
        "{\n"
        '  "niveau_gravite": "VERT" | "JAUNE" | "ORANGE" | "ROUGE",\n'
        '  "orientation_suggeree": "Texte court décrivant le service ou professionnel vers qui orienter",\n'
        '  "conseil_immediat": "Conseil clair, bienveillant et actionnable pour le patient",\n'
        '  "questions_suivi": ["Question 1 à poser au patient pour affiner", "Question 2"]\n'
        "}"
    )

    user_prompt = f"Description des symptômes du patient : « {description} »"

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]

    try:
        response_text = await chat_completion(
            messages=messages,
            temperature=0.2,
            max_tokens=400,
            response_format={"type": "json_object"}
        )
        if response_text:
            data = json.loads(response_text)
            return {
                "description_symptomes": description,
                "niveau_gravite": data.get("niveau_gravite", "JAUNE"),
                "orientation_suggeree": data.get("orientation_suggeree", "Consultation de Médecine Générale"),
                "conseil_immediat": data.get("conseil_immediat", "Veuillez consulter un médecin pour un diagnostic."),
                "questions_suivi": data.get("questions_suivi", ["Depuis quand ressentez-vous ces symptômes ?"])
            }
    except Exception as e:
        logger.error(f"Erreur parsing JSON triage OpenRouter: {e}")

    # Fallback heuristique en cas de panne réseau
    lower = description.lower()
    if any(k in lower for k in ["coeur", "poitrine", "respir", "infarctus", "avc", "étouff", "saign"]):
        return {
            "description_symptomes": description,
            "niveau_gravite": "ROUGE",
            "orientation_suggeree": "SAMU National (1515) / Urgences Cardiologiques / Hôpital le plus proche",
            "conseil_immediat": "ATTENTION : Vos symptômes peuvent indiquer une urgence vitale. Allongez-vous immédiatement, restez calme, et appelez le 1515.",
            "questions_suivi": ["Ressentez-vous une oppression dans la poitrine ?", "La douleur irradie-t-elle vers le bras gauche ou la mâchoire ?"]
        }
    return {
        "description_symptomes": description,
        "niveau_gravite": "JAUNE",
        "orientation_suggeree": "Consultation Médecine Générale / Téléconsultation",
        "conseil_immediat": "Vos symptômes nécessitent un avis médical. Vous pouvez planifier une téléconsultation sur Diam Yaraam.",
        "questions_suivi": ["Depuis combien de jours avez-vous ces symptômes ?"]
    }


# ─── Vérification des interactions & allergies ───────────────────────────────

async def analyze_interactions_llm(
    medicaments: List[str],
    allergies: List[str]
) -> List[Dict[str, str]]:
    """
    Analyse les risques d'interactions médicamenteuses et contre-indications par rapport aux allergies.
    """
    if not medicaments:
        return []

    system_prompt = (
        "Vous êtes un système expert en pharmacologie clinique et interactions médicamenteuses (basé sur le Vidal et le Dorosz).\n"
        "Analysez la liste de médicaments prescrits et les allergies du patient.\n"
        "Détectez toute contre-indication absolue, interaction médicamenteuse majeure ou modérée.\n\n"
        "Vous devez OBLIGATOIREMENT répondre au format JSON strict avec une clé 'interactions' contenant une liste d'objets :\n"
        "{\n"
        '  "interactions": [\n'
        "    {\n"
        '      "medicament1": "Nom du médicament",\n'
        '      "medicament2": "Autre médicament ou allergie en conflit",\n'
        '      "niveau_danger": "CONTRE_INDICATION_ABSOLUE" | "MAJEURE" | "MODEREE" | "MINEURE",\n'
        '      "explication": "Explication médicale claire et concise",\n'
        '      "recommandation": "Alternative ou conduite à tenir recommandée"\n'
        "    }\n"
        "  ]\n"
        "}\n"
        "S'il n'y a aucun risque ou interaction notable, retournez {\"interactions\": []}."
    )

    user_prompt = (
        f"Médicaments prescrits : {', '.join(medicaments)}\n"
        f"Allergies connues : {', '.join(allergies) if allergies else 'Aucune allergie déclarée'}"
    )

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]

    try:
        response_text = await chat_completion(
            messages=messages,
            temperature=0.1,
            max_tokens=600,
            response_format={"type": "json_object"}
        )
        if response_text:
            data = json.loads(response_text)
            return data.get("interactions", [])
    except Exception as e:
        logger.error(f"Erreur analyse interactions OpenRouter: {e}")

    # Fallback heuristique (ex: Pénicillines vs Amoxicilline)
    results = []
    for med in medicaments:
        med_lower = med.lower()
        for allg in allergies:
            allg_lower = allg.lower()
            if ("penicilline" in allg_lower or "pénicilline" in allg_lower) and ("amox" in med_lower or "augmentin" in med_lower):
                results.append({
                    "medicament1": med,
                    "medicament2": f"Allergie : {allg}",
                    "niveau_danger": "CONTRE_INDICATION_ABSOLUE",
                    "explication": "Le patient présente une allergie déclarée aux pénicillines.",
                    "recommandation": "Substituer immédiatement par une autre classe antibiotique (ex: Macrolides)."
                })
    return results


# ─── Health Check OpenRouter ─────────────────────────────────────────────────

async def check_openrouter_health() -> Dict[str, Any]:
    """Vérifie la validité de la configuration OpenRouter."""
    if not get_api_key():
        return {"status": "missing_api_key", "model": LLM_MODEL}
    
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.post(
                OPENROUTER_API_URL,
                json={
                    "model": LLM_MODEL,
                    "messages": [{"role": "user", "content": "ping"}],
                    "max_tokens": 5
                },
                headers=_headers(),
            )
            if resp.status_code == 200:
                return {"status": "ok", "provider": "OpenRouter", "model": LLM_MODEL}
            return {"status": f"error ({resp.status_code})", "provider": "OpenRouter", "model": LLM_MODEL}
    except Exception as e:
        return {"status": f"unreachable: {str(e)}", "provider": "OpenRouter", "model": LLM_MODEL}
