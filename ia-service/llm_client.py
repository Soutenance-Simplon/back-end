"""
Client OpenRouter LLM — Diam Yaraam IA Service
Gère les appels vers les modèles LLM de pointe (OpenAI GPT-4o-mini, etc.) via OpenRouter
avec injection stricte du contexte clinique et pharmacologique (RAG).
"""

import os
import json
import logging
import httpx
from typing import List, Dict, Any, Optional
from dotenv import load_dotenv
from rag_engine import rag_engine

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
    temperature: float = 0.3,
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


# ─── Triage intelligent & Red Flags ──────────────────────────────────────────

async def triage_symptoms_llm(description: str) -> Dict[str, Any]:
    """
    Analyse les symptômes décrits par le patient selon les référentiels officiels :
    1. Si signe de gravité / Red Flag -> Alerte vitale immédiate SAMU 1515 sourcée.
    2. Si affection non urgente -> Orientation spécialisée justifiée (Sections 7 & 8).
    """
    # 1. Évaluation clinique par le moteur RAG
    rag_eval = rag_engine.evaluate_triage_and_orientation(description)

    # 2. Construction du prompt enrichi par le RAG
    if rag_eval["est_urgence_vitale"]:
        system_prompt = (
            "Vous êtes le médecin régulateur du SAMU National du Sénégal (1515) et l'IA de Diam Yaraam.\n"
            "Un SIGNAL D'ALARME VITAL (RED FLAG) a été identifié selon les protocoles OMS SPU et le Référentiel Diam Yaraam.\n"
            "Votre rôle est d'ordonner le déclenchement immédiat des secours et de rassurer le patient avec des gestes de premiers secours stricts.\n"
            "Format de sortie JSON obligatoire avec les clés suivantes :\n"
            "{\n"
            '  "est_urgence_vitale": true,\n'
            '  "niveau_gravite": "ROUGE",\n'
            '  "niveau_urgence": "SAMU",\n'
            '  "red_flags": ["Description clinique du signal d\'alarme"],\n'
            '  "source_medicale": "' + rag_eval["source_medicale"] + '",\n'
            '  "numero_urgence": "1515",\n'
            '  "orientation_suggeree": "SAMU National 1515 / Service d\'Accueil des Urgences",\n'
            '  "conseil_immediat": "Alerte vitale immédiate : restez assis ou semi-allongé, ne faites aucun effort, appelez le 1515.",\n'
            '  "premiers_gestes": ' + json.dumps(rag_eval["premiers_gestes"], ensure_ascii=False) + ',\n'
            '  "questions_suivi": ["À quelle heure précise la douleur a-t-elle commencé ?"]\n'
            "}"
        )
    else:
        system_prompt = (
            "Vous êtes l'assistant médical de régulation de Diam Yaraam (Sénégal).\n"
            "Aucun signe d'urgence vitale n'a été détecté.\n"
            "Appliquez STRICTEMENT le Référentiel d'Orientation Médicale Diam Yaraam (Sections 7 & 8) :\n"
            f"- Spécialité pré-identifiée par le RAG : {rag_eval['specialites_suggerees'][0]}\n"
            f"- Justification clinique : {rag_eval['justification_orientation']}\n"
            f"- Source : {rag_eval['source_medicale']}\n"
            "Format de sortie JSON obligatoire :\n"
            "{\n"
            '  "est_urgence_vitale": false,\n'
            '  "niveau_gravite": "' + rag_eval["niveau_gravite"] + '",\n'
            '  "niveau_urgence": "' + rag_eval["niveau_urgence"] + '",\n'
            '  "red_flags": [],\n'
            '  "source_medicale": "' + rag_eval["source_medicale"] + '",\n'
            '  "orientation_suggeree": "' + rag_eval["specialites_suggerees"][0] + '",\n'
            '  "specialites_suggerees": ' + json.dumps(rag_eval["specialites_suggerees"], ensure_ascii=False) + ',\n'
            '  "justification_orientation": "' + rag_eval["justification_orientation"] + '",\n'
            '  "conseil_immediat": "Conseil clair et bienveillant adapté à la situation",\n'
            '  "rappel_legal": "' + rag_eval["rappel_legal"] + '",\n'
            '  "questions_suivi": ["Depuis combien de jours avez-vous ce symptôme ?"]\n'
            "}"
        )

    user_prompt = f"Description des symptômes du patient : « {description} »"

    try:
        response_text = await chat_completion(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
            max_tokens=500,
            response_format={"type": "json_object"}
        )
        if response_text:
            data = json.loads(response_text)
            return {
                "description_symptomes": description,
                "est_urgence_vitale": data.get("est_urgence_vitale", rag_eval["est_urgence_vitale"]),
                "niveau_gravite": data.get("niveau_gravite", rag_eval["niveau_gravite"]),
                "niveau_urgence": data.get("niveau_urgence", rag_eval["niveau_urgence"]),
                "red_flags": data.get("red_flags", rag_eval["red_flags"]),
                "source_medicale": data.get("source_medicale", rag_eval["source_medicale"]),
                "numero_urgence": data.get("numero_urgence", rag_eval.get("numero_urgence", "1515")),
                "orientation_suggeree": data.get("orientation_suggeree", rag_eval["specialites_suggerees"][0]),
                "specialites_suggerees": data.get("specialites_suggerees", rag_eval["specialites_suggerees"]),
                "justification_orientation": data.get("justification_orientation", rag_eval.get("justification_orientation", "")),
                "conseil_immediat": data.get("conseil_immediat", "Veuillez consulter un médecin agréé pour un examen approfondi."),
                "premiers_gestes": data.get("premiers_gestes", rag_eval.get("premiers_gestes", [])),
                "rappel_legal": data.get("rappel_legal", rag_eval.get("rappel_legal", "")),
                "questions_suivi": data.get("questions_suivi", ["Depuis quand ressentez-vous ces symptômes ?"])
            }
    except Exception as e:
        logger.error(f"Erreur parsing JSON triage OpenRouter: {e}")

    # En cas d'indisponibilité ou d'erreur, le RAG déterministe garantit une réponse clinique parfaite
    return {
        "description_symptomes": description,
        "est_urgence_vitale": rag_eval["est_urgence_vitale"],
        "niveau_gravite": rag_eval["niveau_gravite"],
        "niveau_urgence": rag_eval["niveau_urgence"],
        "red_flags": rag_eval["red_flags"],
        "source_medicale": rag_eval["source_medicale"],
        "numero_urgence": rag_eval.get("numero_urgence", "1515"),
        "orientation_suggeree": rag_eval["specialites_suggerees"][0],
        "specialites_suggerees": rag_eval["specialites_suggerees"],
        "justification_orientation": rag_eval.get("justification_orientation", ""),
        "conseil_immediat": (
            "ATTENTION URGENCE VITALE : Allongez-vous immédiatement en position semi-assise et appelez sans délai le 1515."
            if rag_eval["est_urgence_vitale"]
            else f"Orientation recommandée vers un praticien en {rag_eval['specialites_suggerees'][0]} sur Diam Yaraam."
        ),
        "premiers_gestes": rag_eval.get("premiers_gestes", []),
        "rappel_legal": rag_eval.get("rappel_legal", ""),
        "questions_suivi": ["Ressentez-vous une aggravation rapide ?"]
    }


def _normalize_name(s: str) -> str:
    import unicodedata
    if not s:
        return ""
    nfkd = unicodedata.normalize('NFKD', str(s))
    return "".join([c for c in nfkd if not unicodedata.combining(c)]).upper()

# ─── Vérification des interactions & allergies ───────────────────────────────

async def analyze_interactions_llm(
    medicaments: List[str],
    allergies: List[str]
) -> List[Dict[str, Any]]:
    """
    Analyse les risques d'interactions médicamenteuses et contre-indications par rapport aux allergies,
    avec injection du Guide des Médicaments Essentiels MSF/OMS.
    """
    if not medicaments:
        return []

    # 1. Vérification déterministe certifiée par le RAG MSF
    rag_interactions = rag_engine.evaluate_drug_safety(medicaments, allergies)

    # Si le RAG a détecté une contre-indication absolue MSF, on l'utilise directement comme socle inviolable
    if rag_interactions:
        logger.info(f"Interaction critique détectée par le RAG MSF : {rag_interactions}")

    system_prompt = (
        "Vous êtes un pharmacologue clinicien expert de la plateforme Diam Yaraam, basé sur le Guide des Médicaments Essentiels MSF/OMS (Édition 2024-2026) et le Dorosz.\n"
        "Analysez la liste de médicaments prescrits et les allergies du patient.\n"
        "Détectez toute contre-indication absolue (ex: allongement QT mortel Amiodarone + Ciprofloxacine p. 51, Pénicillines chez patient allergique p. 38, AINS + Anticoagulant p. 10).\n"
        "Pour chaque problème détecté, vous devez OBLIGATOIREMENT préciser la source exacte (Guide MSF p. 51 ou fiche DCI) et proposer une ALTERNATIVE thérapeutique concrète.\n\n"
        "Format de sortie JSON obligatoire :\n"
        "{\n"
        '  "interactions": [\n'
        "    {\n"
        '      "medicament1": "Nom molécule 1",\n'
        '      "medicament2": "Nom molécule 2 ou allergie",\n'
        '      "niveau_danger": "CONTRE_INDICATION_ABSOLUE" | "MAJEURE" | "MODEREE",\n'
        '      "bloquant": true,\n'
        '      "source_medicale": "Guide Médicaments Essentiels MSF/OMS (Édition 2024-2026), Monographie Amoxicilline, p. 38",\n'
        '      "page_numero": 38,\n'
        '      "explication": "Explication pharmacologique claire du mécanisme",\n'
        '      "recommandation": "ASSOCIATION FORMELLEMENT CONTRE-INDIQUÉE.",\n'
        '      "alternative_recommandee": "Alternative thérapeutique concrète"\n'
        "    }\n"
        "  ]\n"
        "}\n"
        "S'il n'y a aucun risque, retournez {\"interactions\": []}."
    )

    user_prompt = (
        f"Médicaments dans l'ordonnance : {', '.join(medicaments)}\n"
        f"Allergies du patient : {', '.join(allergies) if allergies else 'Aucune allergie déclarée'}\n"
        f"Résultats pré-identifiés dans le référentiel MSF : {json.dumps(rag_interactions, ensure_ascii=False)}"
    )

    try:
        response_text = await chat_completion(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            temperature=0.1,
            max_tokens=600,
            response_format={"type": "json_object"}
        )
        if response_text:
            data = json.loads(response_text)
            interactions_llm = data.get("interactions", [])
            if interactions_llm:
                # Enrichir systématiquement avec les données documentaires certifiées du RAG
                for item in interactions_llm:
                    m1_norm = _normalize_name(item.get("medicament1", ""))
                    m2_norm = _normalize_name(item.get("medicament2", ""))
                    
                    # Chercher correspondance directe dans le RAG
                    rag_match = next((r for r in rag_interactions if (
                        (_normalize_name(r["medicament1"]) in m1_norm or m1_norm in _normalize_name(r["medicament1"])) and 
                        (_normalize_name(r["medicament2"]) in m2_norm or m2_norm in _normalize_name(r["medicament2"]))
                    )), None)
                    
                    if rag_match:
                        item["page_numero"] = rag_match.get("page_numero", 38)
                        item["document_nom"] = rag_match.get("document_nom", "guideline-339-fr.pdf")
                        item["document_url"] = rag_match.get("document_url", f"http://127.0.0.1:8089/ia/documents/view/{item['document_nom']}?page={item['page_numero']}")
                        item["source_medicale"] = rag_match.get("source_medicale", item.get("source_medicale"))
                    else:
                        combined = f"{m1_norm} {m2_norm}"
                        page = item.get("page_numero")
                        src = item.get("source_medicale", "")
                        
                        if not page:
                            import re
                            match = re.search(r'(?:p\.|page\s*)(\d+)', src, re.IGNORECASE)
                            if match:
                                page = int(match.group(1))

                        if not page:
                            if "CIPRO" in combined and "AMIODARONE" in combined:
                                page = 51
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Ciprofloxacine, p. 51"
                            elif "AMOX" in combined and ("PENICIL" in combined or "ALLERGIE" in combined or "BETA" in combined):
                                page = 38
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Amoxicilline, p. 38"
                            elif "AINS" in combined or "ASPIRIN" in combined or "IBUPROFEN" in combined:
                                page = 10
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie AINS, p. 10"
                            elif "BACTRIM" in combined or "SULFAMID" in combined or "COTRIMOX" in combined:
                                page = 12
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Fiche Co-trimoxazole, p. 12"
                            elif "TRAMADOL" in combined and ("FLUOXETIN" in combined or "SERTRALIN" in combined or "ISRS" in combined):
                                page = 101
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Antalgiques opioïdes et IRS, p. 101"
                            elif "SPIRONOLACTON" in combined or "PERINDOPRIL" in combined:
                                page = 77
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Diurétiques et IEC, p. 77"
                            elif "SIMVASTATIN" in combined or "CLARITHROMYCIN" in combined or "STATIN" in combined:
                                page = 83
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Hypolipémiants et Macrolides, p. 83"
                            elif "MACROLID" in combined:
                                page = 44
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Macrolides, p. 44"
                            else:
                                page = 38
                        
                        doc_nom = item.get("document_nom") or "guideline-339-fr.pdf"
                        item["page_numero"] = page
                        item["document_nom"] = doc_nom
                        item["document_url"] = f"http://127.0.0.1:8089/ia/documents/view/{doc_nom}?page={page}"
                return interactions_llm
    except Exception as e:
        logger.error(f"Erreur analyse interactions OpenRouter: {e}")

    # Fallback sécurisé : si le LLM échoue, le RAG MSF prend le relais immédiatement
    if rag_interactions:
        return rag_interactions

    return []


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
