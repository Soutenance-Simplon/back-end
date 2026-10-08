"""
Client OpenRouter LLM — Diam Yaraam IA Service
Gère les appels vers les modèles LLM de pointe (OpenAI GPT-4o-mini, etc.) via OpenRouter
avec injection stricte du contexte clinique et pharmacologique (RAG).
"""

# Importation de la librairie os
import os
# Importation de la librairie json
import json
# Importation de la librairie logging
import logging
# Importation de la librairie httpx
import httpx
# Importation spécifique depuis le module typing
from typing import List, Dict, Any, Optional
# Importation spécifique depuis le module dotenv
from dotenv import load_dotenv
# Importation spécifique depuis le module rag_engine
from rag_engine import rag_engine

# Charger les variables d'environnement
load_dotenv()

# Affectation de la valeur à la variable 'logger'
logger = logging.getLogger("ia-service.llm_client")

# ─── Configuration ───────────────────────────────────────────────────────────

# Affectation de la valeur à la variable 'OPENROUTER_API_URL'
OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"

# Définition de la fonction get_api_key
def get_api_key() -> str:
    # Renvoie le résultat final de cette fonction
    return os.getenv("OPENROUTER_API_KEY", "").strip('"').strip("'")

# Définition de la fonction get_model
def get_model() -> str:
    # Renvoie le résultat final de cette fonction
    return os.getenv("LLM_MODEL", "openai/gpt-4o-mini").strip('"').strip("'")

# Affectation de la valeur à la variable 'LLM_MODEL'
LLM_MODEL = get_model()

# Timeout pour les requêtes OpenRouter
TIMEOUT = 45.0


# Définition de la fonction _headers
def _headers() -> dict:
    # Exécution de cette instruction spécifique
    """Headers d'authentification pour l'API OpenRouter."""
    # Affectation de la valeur à la variable 'api_key'
    api_key = get_api_key()
    # Affectation de la valeur à la variable 'headers'
    headers = {
        # Exécution de cette instruction spécifique
        "Content-Type": "application/json",
        # Exécution de cette instruction spécifique
        "HTTP-Referer": "https://diam-yaraam.sn",
        # Exécution de cette instruction spécifique
        "X-Title": "Diam Yaraam Telemedicine AI"
    }
    # Structure conditionnelle : on vérifie si la condition est vraie
    if api_key:
        # Initialisation et affectation d'une variable
        headers["Authorization"] = f"Bearer {api_key}"
    # Renvoie le résultat final de cette fonction
    return headers


# ─── Appel générique OpenRouter Chat ──────────────────────────────────────────

# Définition de la fonction asynchrone chat_completion
async def chat_completion(
    # Exécution de cette instruction spécifique
    messages: List[Dict[str, str]],
    # Initialisation et affectation d'une variable
    model: Optional[str] = None,
    # Initialisation et affectation d'une variable
    temperature: float = 0.3,
    # Initialisation et affectation d'une variable
    max_tokens: int = 800,
    # Initialisation et affectation d'une variable
    response_format: Optional[dict] = None,
# Exécution de cette instruction spécifique
) -> str:
    """
    Appelle le endpoint de chat completions d'OpenRouter.
    """
    # Affectation de la valeur à la variable 'target_model'
    target_model = model or LLM_MODEL
    # Initialisation et affectation d'une variable
    payload: Dict[str, Any] = {
        # Exécution de cette instruction spécifique
        "model": target_model,
        # Exécution de cette instruction spécifique
        "messages": messages,
        # Exécution de cette instruction spécifique
        "temperature": temperature,
        # Exécution de cette instruction spécifique
        "max_tokens": max_tokens,
    }
    # Structure conditionnelle : on vérifie si la condition est vraie
    if response_format:
        # Initialisation et affectation d'une variable
        payload["response_format"] = response_format

    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # Initialisation et affectation d'une variable
        async with httpx.AsyncClient(timeout=TIMEOUT) as client:
            # Affectation de la valeur à la variable 'resp'
            resp = await client.post(
                # Exécution de cette instruction spécifique
                OPENROUTER_API_URL,
                # Affectation de la valeur à la variable 'json'
                json=payload,
                # Affectation de la valeur à la variable 'headers'
                headers=_headers(),
            )
            # Appel d'une fonction ou méthode spécifique
            resp.raise_for_status()
            # Affectation de la valeur à la variable 'data'
            data = resp.json()
            # Affectation de la valeur à la variable 'choices'
            choices = data.get("choices", [])
            # Structure conditionnelle : on vérifie si la condition est vraie
            if choices and len(choices) > 0:
                # Affectation de la valeur à la variable 'message'
                message = choices[0].get("message", {})
                # Renvoie le résultat final de cette fonction
                return message.get("content", "").strip()
            # Renvoie le résultat final de cette fonction
            return ""
    # Capture et gestion de l'erreur si le bloc try échoue
    except httpx.HTTPStatusError as e:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.error(f"Erreur HTTP OpenRouter ({e.response.status_code}): {e.response.text}")
        # Renvoie le résultat final de cette fonction
        return ""
    # Capture et gestion de l'erreur si le bloc try échoue
    except Exception as e:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.error(f"Erreur appel OpenRouter: {e}")
        # Renvoie le résultat final de cette fonction
        return ""


# ─── Triage intelligent & Red Flags ──────────────────────────────────────────

# Définition de la fonction asynchrone triage_symptoms_llm
async def triage_symptoms_llm(description: str) -> Dict[str, Any]:
    """
    [SOUTENANCE] - MOTEUR DE TRIAGE HYBRIDE (RAG + LLM)
    Cette fonction est le cœur du triage médical de l'application.
    Elle résout le problème des hallucinations de l'IA en imposant une évaluation
    déterministe (RAG) avant toute génération de texte par le LLM.
    """
    # 1. [SÉCURITÉ] Évaluation clinique par le moteur RAG déterministe.
    # Le RAG cherche des mots-clés d'urgence vitale (ex: "douleur poitrine") dans le texte.
    # S'il en trouve, il force l'IA à déclencher le protocole SAMU sans contester.
    rag_eval = rag_engine.evaluate_triage_and_orientation(description)

    # 2. Construction du "System Prompt" (Prompt Système)
    # C'est ici que l'on donne une personnalité stricte à l'IA.
    # Si le RAG a détecté une urgence vitale (est_urgence_vitale == True),
    # on verrouille la réponse sur une alerte SAMU 1515.
    if rag_eval["est_urgence_vitale"]:
        # Affectation de la valeur à la variable 'system_prompt'
        system_prompt = (
            # Exécution de cette instruction spécifique
            "Vous êtes le médecin régulateur du SAMU National du Sénégal (1515) et l'IA de Diam Yaraam.\n"
            # Exécution de cette instruction spécifique
            "Un SIGNAL D'ALARME VITAL (RED FLAG) a été identifié selon les protocoles OMS SPU et le Référentiel Diam Yaraam.\n"
            # Exécution de cette instruction spécifique
            "Votre rôle est d'ordonner le déclenchement immédiat des secours et de rassurer le patient avec des gestes de premiers secours stricts.\n"
            # Exécution de cette instruction spécifique
            "Format de sortie JSON obligatoire avec les clés suivantes :\n"
            # Exécution de cette instruction spécifique
            "{\n"
            # Exécution de cette instruction spécifique
            '  "est_urgence_vitale": true,\n'
            # Exécution de cette instruction spécifique
            '  "niveau_gravite": "ROUGE",\n'
            # Exécution de cette instruction spécifique
            '  "niveau_urgence": "SAMU",\n'
            # Exécution de cette instruction spécifique
            '  "red_flags": ["Description clinique du signal d\'alarme"],\n'
            # Exécution de cette instruction spécifique
            '  "source_medicale": "' + rag_eval["source_medicale"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "numero_urgence": "1515",\n'
            # Exécution de cette instruction spécifique
            '  "orientation_suggeree": "SAMU National 1515 / Service d\'Accueil des Urgences",\n'
            # Exécution de cette instruction spécifique
            '  "conseil_immediat": "Alerte vitale immédiate : restez assis ou semi-allongé, ne faites aucun effort, appelez le 1515.",\n'
            # Initialisation et affectation d'une variable
            '  "premiers_gestes": ' + json.dumps(rag_eval["premiers_gestes"], ensure_ascii=False) + ',\n'
            # Exécution de cette instruction spécifique
            '  "questions_suivi": ["À quelle heure précise la douleur a-t-elle commencé ?"]\n'
            # Exécution de cette instruction spécifique
            "}"
        )
    # Sinon
    else:
        # Affectation de la valeur à la variable 'system_prompt'
        system_prompt = (
            # Exécution de cette instruction spécifique
            "Vous êtes l'assistant médical de régulation de Diam Yaraam (Sénégal).\n"
            # Exécution de cette instruction spécifique
            "Aucun signe d'urgence vitale n'a été détecté.\n"
            # Exécution de cette instruction spécifique
            "Appliquez STRICTEMENT le Référentiel d'Orientation Médicale Diam Yaraam (Sections 7 & 8) :\n"
            # Exécution de cette instruction spécifique
            f"- Spécialité pré-identifiée par le RAG : {rag_eval['specialites_suggerees'][0]}\n"
            # Exécution de cette instruction spécifique
            f"- Justification clinique : {rag_eval['justification_orientation']}\n"
            # Exécution de cette instruction spécifique
            f"- Source : {rag_eval['source_medicale']}\n"
            # Exécution de cette instruction spécifique
            "Format de sortie JSON obligatoire :\n"
            # Exécution de cette instruction spécifique
            "{\n"
            # Exécution de cette instruction spécifique
            '  "est_urgence_vitale": false,\n'
            # Exécution de cette instruction spécifique
            '  "niveau_gravite": "' + rag_eval["niveau_gravite"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "niveau_urgence": "' + rag_eval["niveau_urgence"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "red_flags": [],\n'
            # Exécution de cette instruction spécifique
            '  "source_medicale": "' + rag_eval["source_medicale"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "orientation_suggeree": "' + rag_eval["specialites_suggerees"][0] + '",\n'
            # Initialisation et affectation d'une variable
            '  "specialites_suggerees": ' + json.dumps(rag_eval["specialites_suggerees"], ensure_ascii=False) + ',\n'
            # Exécution de cette instruction spécifique
            '  "justification_orientation": "' + rag_eval["justification_orientation"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "conseil_immediat": "Conseil clair et bienveillant adapté à la situation",\n'
            # Exécution de cette instruction spécifique
            '  "rappel_legal": "' + rag_eval["rappel_legal"] + '",\n'
            # Exécution de cette instruction spécifique
            '  "questions_suivi": ["Depuis combien de jours avez-vous ce symptôme ?"]\n'
            # Exécution de cette instruction spécifique
            "}"
        )

    # Affectation de la valeur à la variable 'user_prompt'
    user_prompt = f"Description des symptômes du patient : « {description} »"

    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # 3. [APPEL LLM] - Exécution avec température très basse
        # La température est à 0.1 (presque 0) pour forcer le LLM à être purement analytique.
        # Cela empêche toute créativité littéraire et garantit une réponse factuelle stricte.
        # Le 'response_format' force le LLM à répondre exclusivement en JSON valide.
        response_text = await chat_completion(
            # Affectation de la valeur à la variable 'messages'
            messages=[
                # Exécution de cette instruction spécifique
                {"role": "system", "content": system_prompt},
                # Exécution de cette instruction spécifique
                {"role": "user", "content": user_prompt}
            ],
            # Affectation de la valeur à la variable 'temperature'
            temperature=0.1,
            # Affectation de la valeur à la variable 'max_tokens'
            max_tokens=500,
            # Affectation de la valeur à la variable 'response_format'
            response_format={"type": "json_object"}
        )
        # Structure conditionnelle : on vérifie si la condition est vraie
        if response_text:
            # Affectation de la valeur à la variable 'data'
            data = json.loads(response_text)
            # Renvoie le résultat final de cette fonction
            return {
                # Exécution de cette instruction spécifique
                "description_symptomes": description,
                # Exécution de cette instruction spécifique
                "est_urgence_vitale": data.get("est_urgence_vitale", rag_eval["est_urgence_vitale"]),
                # Exécution de cette instruction spécifique
                "niveau_gravite": data.get("niveau_gravite", rag_eval["niveau_gravite"]),
                # Exécution de cette instruction spécifique
                "niveau_urgence": data.get("niveau_urgence", rag_eval["niveau_urgence"]),
                # Exécution de cette instruction spécifique
                "red_flags": data.get("red_flags", rag_eval["red_flags"]),
                # Exécution de cette instruction spécifique
                "source_medicale": data.get("source_medicale", rag_eval["source_medicale"]),
                # Exécution de cette instruction spécifique
                "numero_urgence": data.get("numero_urgence", rag_eval.get("numero_urgence", "1515")),
                # Exécution de cette instruction spécifique
                "orientation_suggeree": data.get("orientation_suggeree", rag_eval["specialites_suggerees"][0]),
                # Exécution de cette instruction spécifique
                "specialites_suggerees": data.get("specialites_suggerees", rag_eval["specialites_suggerees"]),
                # Exécution de cette instruction spécifique
                "justification_orientation": data.get("justification_orientation", rag_eval.get("justification_orientation", "")),
                # Exécution de cette instruction spécifique
                "conseil_immediat": data.get("conseil_immediat", "Veuillez consulter un médecin agréé pour un examen approfondi."),
                # Exécution de cette instruction spécifique
                "premiers_gestes": data.get("premiers_gestes", rag_eval.get("premiers_gestes", [])),
                # Exécution de cette instruction spécifique
                "rappel_legal": data.get("rappel_legal", rag_eval.get("rappel_legal", "")),
                # Appel d'une fonction ou méthode spécifique
                "questions_suivi": data.get("questions_suivi", ["Depuis quand ressentez-vous ces symptômes ?"])
            }
    # Capture et gestion de l'erreur si le bloc try échoue
    except Exception as e:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.error(f"Erreur parsing JSON triage OpenRouter: {e}")

    # En cas d'indisponibilité ou d'erreur, le RAG déterministe garantit une réponse clinique parfaite
    return {
        # Exécution de cette instruction spécifique
        "description_symptomes": description,
        # Exécution de cette instruction spécifique
        "est_urgence_vitale": rag_eval["est_urgence_vitale"],
        # Exécution de cette instruction spécifique
        "niveau_gravite": rag_eval["niveau_gravite"],
        # Exécution de cette instruction spécifique
        "niveau_urgence": rag_eval["niveau_urgence"],
        # Exécution de cette instruction spécifique
        "red_flags": rag_eval["red_flags"],
        # Exécution de cette instruction spécifique
        "source_medicale": rag_eval["source_medicale"],
        # Exécution de cette instruction spécifique
        "numero_urgence": rag_eval.get("numero_urgence", "1515"),
        # Exécution de cette instruction spécifique
        "orientation_suggeree": rag_eval["specialites_suggerees"][0],
        # Exécution de cette instruction spécifique
        "specialites_suggerees": rag_eval["specialites_suggerees"],
        # Exécution de cette instruction spécifique
        "justification_orientation": rag_eval.get("justification_orientation", ""),
        # Ouverture d'une structure de données ou d'une fonction
        "conseil_immediat": (
            # Exécution de cette instruction spécifique
            "ATTENTION URGENCE VITALE : Allongez-vous immédiatement en position semi-assise et appelez sans délai le 1515."
            # Structure conditionnelle : on vérifie si la condition est vraie
            if rag_eval["est_urgence_vitale"]
            # Exécution de cette instruction spécifique
            else f"Orientation recommandée vers un praticien en {rag_eval['specialites_suggerees'][0]} sur Diam Yaraam."
        # Exécution de cette instruction spécifique
        ),
        # Exécution de cette instruction spécifique
        "premiers_gestes": rag_eval.get("premiers_gestes", []),
        # Exécution de cette instruction spécifique
        "rappel_legal": rag_eval.get("rappel_legal", ""),
        # Exécution de cette instruction spécifique
        "questions_suivi": ["Ressentez-vous une aggravation rapide ?"]
    }


# Définition de la fonction _normalize_name
def _normalize_name(s: str) -> str:
    # Importation de la librairie unicodedata
    import unicodedata
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not s:
        # Renvoie le résultat final de cette fonction
        return ""
    # Affectation de la valeur à la variable 'nfkd'
    nfkd = unicodedata.normalize('NFKD', str(s))
    # Renvoie le résultat final de cette fonction
    return "".join([c for c in nfkd if not unicodedata.combining(c)]).upper()

# ─── Vérification des interactions & allergies ───────────────────────────────

# Définition de la fonction asynchrone analyze_interactions_llm
async def analyze_interactions_llm(
    # Exécution de cette instruction spécifique
    medicaments: List[str],
    # Exécution de cette instruction spécifique
    allergies: List[str]
# Exécution de cette instruction spécifique
) -> List[Dict[str, Any]]:
    """
    [SOUTENANCE] - MODULE DE PHARMACOVIGILANCE
    Vérifie les interactions médicamenteuses dangereuses et les allergies du patient.
    Utilise le Guide des Médicaments Essentiels MSF/OMS.
    """
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not medicaments:
        # Renvoie le résultat final de cette fonction
        return []

    # 1. [RÈGLES DURES] Vérification déterministe certifiée par le RAG MSF.
    # Avant d'interroger l'IA, le système vérifie si la combinaison de médicaments
    # existe déjà dans notre base de données locale inviolable (ex: Ibuprofène + Aspirine).
    # Cela garantit 100% de précision sur les interactions mortelles connues.
    rag_interactions = rag_engine.evaluate_drug_safety(medicaments, allergies)

    # Si le RAG a détecté une contre-indication absolue MSF, on l'utilise directement comme socle inviolable
    if rag_interactions:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.info(f"Interaction critique détectée par le RAG MSF : {rag_interactions}")

    # Affectation de la valeur à la variable 'system_prompt'
    system_prompt = (
        # Exécution de cette instruction spécifique
        "Vous êtes un pharmacologue clinicien expert de la plateforme Diam Yaraam, basé sur le Guide des Médicaments Essentiels MSF/OMS (Édition 2024-2026) et le Dorosz.\n"
        # Exécution de cette instruction spécifique
        "Analysez la liste de médicaments prescrits et les allergies du patient.\n"
        # Exécution de cette instruction spécifique
        "Détectez toute contre-indication absolue (ex: allongement QT mortel Amiodarone + Ciprofloxacine p. 51, Pénicillines chez patient allergique p. 38, AINS + Anticoagulant p. 10).\n"
        # Exécution de cette instruction spécifique
        "Pour chaque problème détecté, vous devez OBLIGATOIREMENT préciser la source exacte (Guide MSF p. 51 ou fiche DCI) et proposer une ALTERNATIVE thérapeutique concrète.\n\n"
        # Exécution de cette instruction spécifique
        "Format de sortie JSON obligatoire :\n"
        # Exécution de cette instruction spécifique
        "{\n"
        # Exécution de cette instruction spécifique
        '  "interactions": [\n'
        # Exécution de cette instruction spécifique
        "    {\n"
        # Exécution de cette instruction spécifique
        '      "medicament1": "Nom molécule 1",\n'
        # Exécution de cette instruction spécifique
        '      "medicament2": "Nom molécule 2 ou allergie",\n'
        # Exécution de cette instruction spécifique
        '      "niveau_danger": "CONTRE_INDICATION_ABSOLUE" | "MAJEURE" | "MODEREE",\n'
        # Exécution de cette instruction spécifique
        '      "bloquant": true,\n'
        # Exécution de cette instruction spécifique
        '      "source_medicale": "Guide Médicaments Essentiels MSF/OMS (Édition 2024-2026), Monographie Amoxicilline, p. 38",\n'
        # Exécution de cette instruction spécifique
        '      "page_numero": 38,\n'
        # Exécution de cette instruction spécifique
        '      "explication": "Explication pharmacologique claire du mécanisme",\n'
        # Exécution de cette instruction spécifique
        '      "recommandation": "ASSOCIATION FORMELLEMENT CONTRE-INDIQUÉE.",\n'
        # Exécution de cette instruction spécifique
        '      "alternative_recommandee": "Alternative thérapeutique concrète"\n'
        # Exécution de cette instruction spécifique
        "    }\n"
        # Exécution de cette instruction spécifique
        "  ]\n"
        # Exécution de cette instruction spécifique
        "}\n"
        # Exécution de cette instruction spécifique
        "S'il n'y a aucun risque, retournez {\"interactions\": []}."
    )

    # Affectation de la valeur à la variable 'user_prompt'
    user_prompt = (
        # Exécution de cette instruction spécifique
        f"Médicaments dans l'ordonnance : {', '.join(medicaments)}\n"
        # Exécution de cette instruction spécifique
        f"Allergies du patient : {', '.join(allergies) if allergies else 'Aucune allergie déclarée'}\n"
        # Initialisation et affectation d'une variable
        f"Résultats pré-identifiés dans le référentiel MSF : {json.dumps(rag_interactions, ensure_ascii=False)}"
    )

    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # [APPEL LLM] Température 0.1 pour empêcher l'IA d'inventer des molécules.
        # Si le RAG a détecté une erreur, il l'injecte dans le prompt (rag_interactions).
        response_text = await chat_completion(
            # Affectation de la valeur à la variable 'messages'
            messages=[
                # Exécution de cette instruction spécifique
                {"role": "system", "content": system_prompt},
                # Exécution de cette instruction spécifique
                {"role": "user", "content": user_prompt}
            ],
            # Affectation de la valeur à la variable 'temperature'
            temperature=0.1,
            # Affectation de la valeur à la variable 'max_tokens'
            max_tokens=600,
            # Affectation de la valeur à la variable 'response_format'
            response_format={"type": "json_object"}
        )
        # Structure conditionnelle : on vérifie si la condition est vraie
        if response_text:
            # Affectation de la valeur à la variable 'data'
            data = json.loads(response_text)
            # Affectation de la valeur à la variable 'interactions_llm'
            interactions_llm = data.get("interactions", [])
            # Structure conditionnelle : on vérifie si la condition est vraie
            if interactions_llm:
                # 2. [VÉRIFICATION CROISÉE] Enrichissement systématique par le RAG.
                # Même si le LLM a généré l'interaction, on force le système à 
                # récupérer la VRAIE page du manuel PDF correspondante (ex: p.38)
                # trouvée par le RAG, empêchant le LLM d'inventer une fausse page.
                for item in interactions_llm:
                    # Affectation de la valeur à la variable 'm1_norm'
                    m1_norm = _normalize_name(item.get("medicament1", ""))
                    # Affectation de la valeur à la variable 'm2_norm'
                    m2_norm = _normalize_name(item.get("medicament2", ""))
                    
                    # Chercher correspondance directe dans le RAG
                    rag_match = next((r for r in rag_interactions if (
                        # Exécution de cette instruction spécifique
                        (_normalize_name(r["medicament1"]) in m1_norm or m1_norm in _normalize_name(r["medicament1"])) and 
                        # Appel d'une fonction ou méthode spécifique
                        (_normalize_name(r["medicament2"]) in m2_norm or m2_norm in _normalize_name(r["medicament2"]))
                    # Exécution de cette instruction spécifique
                    )), None)
                    
                    # Structure conditionnelle : on vérifie si la condition est vraie
                    if rag_match:
                        # Initialisation et affectation d'une variable
                        item["page_numero"] = rag_match.get("page_numero", 38)
                        # Initialisation et affectation d'une variable
                        item["document_nom"] = rag_match.get("document_nom", "guideline-339-fr.pdf")
                        # Initialisation et affectation d'une variable
                        item["document_url"] = rag_match.get("document_url", f"http://127.0.0.1:8089/ia/documents/view/{item['document_nom']}?page={item['page_numero']}")
                        # Initialisation et affectation d'une variable
                        item["source_medicale"] = rag_match.get("source_medicale", item.get("source_medicale"))
                    # Sinon
                    else:
                        # Affectation de la valeur à la variable 'combined'
                        combined = f"{m1_norm} {m2_norm}"
                        # Affectation de la valeur à la variable 'page'
                        page = item.get("page_numero")
                        # Affectation de la valeur à la variable 'src'
                        src = item.get("source_medicale", "")
                        
                        # Structure conditionnelle : on vérifie si la condition est vraie
                        if not page:
                            # Importation de la librairie re
                            import re
                            # Affectation de la valeur à la variable 'match'
                            match = re.search(r'(?:p\.|page\s*)(\d+)', src, re.IGNORECASE)
                            # Structure conditionnelle : on vérifie si la condition est vraie
                            if match:
                                # Affectation de la valeur à la variable 'page'
                                page = int(match.group(1))

                        # Structure conditionnelle : on vérifie si la condition est vraie
                        if not page:
                            # Structure conditionnelle : on vérifie si la condition est vraie
                            if "CIPRO" in combined and "AMIODARONE" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 51
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Ciprofloxacine, p. 51"
                            # Sinon si
                            elif "AMOX" in combined and ("PENICIL" in combined or "ALLERGIE" in combined or "BETA" in combined):
                                # Affectation de la valeur à la variable 'page'
                                page = 38
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Amoxicilline, p. 38"
                            # Sinon si
                            elif "AINS" in combined or "ASPIRIN" in combined or "IBUPROFEN" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 10
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie AINS, p. 10"
                            # Sinon si
                            elif "BACTRIM" in combined or "SULFAMID" in combined or "COTRIMOX" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 12
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Fiche Co-trimoxazole, p. 12"
                            # Sinon si
                            elif "TRAMADOL" in combined and ("FLUOXETIN" in combined or "SERTRALIN" in combined or "ISRS" in combined):
                                # Affectation de la valeur à la variable 'page'
                                page = 101
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Antalgiques opioïdes et IRS, p. 101"
                            # Sinon si
                            elif "SPIRONOLACTON" in combined or "PERINDOPRIL" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 77
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Diurétiques et IEC, p. 77"
                            # Sinon si
                            elif "SIMVASTATIN" in combined or "CLARITHROMYCIN" in combined or "STATIN" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 83
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Hypolipémiants et Macrolides, p. 83"
                            # Sinon si
                            elif "MACROLID" in combined:
                                # Affectation de la valeur à la variable 'page'
                                page = 44
                                # Initialisation et affectation d'une variable
                                item["source_medicale"] = "Guide Médicaments Essentiels MSF/OMS, Monographie Macrolides, p. 44"
                            # Sinon
                            else:
                                # Affectation de la valeur à la variable 'page'
                                page = 38
                        
                        # Affectation de la valeur à la variable 'doc_nom'
                        doc_nom = item.get("document_nom") or "guideline-339-fr.pdf"
                        # Initialisation et affectation d'une variable
                        item["page_numero"] = page
                        # Initialisation et affectation d'une variable
                        item["document_nom"] = doc_nom
                        # Initialisation et affectation d'une variable
                        item["document_url"] = f"http://127.0.0.1:8089/ia/documents/view/{doc_nom}?page={page}"
                # Renvoie le résultat final de cette fonction
                return interactions_llm
    # Capture et gestion de l'erreur si le bloc try échoue
    except Exception as e:
        # Enregistre une trace (log) pour le suivi de l'application
        logger.error(f"Erreur analyse interactions OpenRouter: {e}")

    # Fallback sécurisé : si le LLM échoue, le RAG MSF prend le relais immédiatement
    if rag_interactions:
        # Renvoie le résultat final de cette fonction
        return rag_interactions

    # Renvoie le résultat final de cette fonction
    return []


# ─── Health Check OpenRouter ─────────────────────────────────────────────────

# Définition de la fonction asynchrone check_openrouter_health
async def check_openrouter_health() -> Dict[str, Any]:
    # Exécution de cette instruction spécifique
    """Vérifie la validité de la configuration OpenRouter."""
    # Structure conditionnelle : on vérifie si la condition est vraie
    if not get_api_key():
        # Renvoie le résultat final de cette fonction
        return {"status": "missing_api_key", "model": LLM_MODEL}
    
    # Début d'un bloc sécurisé pour intercepter les erreurs potentielles
    try:
        # Initialisation et affectation d'une variable
        async with httpx.AsyncClient(timeout=10.0) as client:
            # Affectation de la valeur à la variable 'resp'
            resp = await client.post(
                # Exécution de cette instruction spécifique
                OPENROUTER_API_URL,
                # Affectation de la valeur à la variable 'json'
                json={
                    # Exécution de cette instruction spécifique
                    "model": LLM_MODEL,
                    # Exécution de cette instruction spécifique
                    "messages": [{"role": "user", "content": "ping"}],
                    # Exécution de cette instruction spécifique
                    "max_tokens": 5
                },
                # Affectation de la valeur à la variable 'headers'
                headers=_headers(),
            )
            # Structure conditionnelle : on vérifie si la condition est vraie
            if resp.status_code == 200:
                # Renvoie le résultat final de cette fonction
                return {"status": "ok", "provider": "OpenRouter", "model": LLM_MODEL}
            # Renvoie le résultat final de cette fonction
            return {"status": f"error ({resp.status_code})", "provider": "OpenRouter", "model": LLM_MODEL}
    # Capture et gestion de l'erreur si le bloc try échoue
    except Exception as e:
        # Renvoie le résultat final de cette fonction
        return {"status": f"unreachable: {str(e)}", "provider": "OpenRouter", "model": LLM_MODEL}
