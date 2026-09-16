"""
Client Hugging Face Inference API — Diam Yaraam IA Service
Appelle les modèles hébergés sur Hugging Face (gratuit, pas besoin de GPU local).
"""

import os
import httpx
import logging
from typing import Optional

logger = logging.getLogger("ia-service.hf_client")

# ─── Configuration ───────────────────────────────────────────────────────────

HF_API_URL = "https://api-inference.huggingface.co/models"
HF_API_TOKEN = os.getenv("HF_API_TOKEN", "")

# Modèles utilisés
MODEL_CHAT = "mistralai/Mistral-7B-Instruct-v0.3"
MODEL_ZERO_SHOT = "facebook/bart-large-mnli"
MODEL_EMBEDDINGS = "sentence-transformers/all-MiniLM-L6-v2"

# Timeout pour les requêtes HF (le cold start peut être lent)
HF_TIMEOUT = 120.0


def _headers() -> dict:
    """Headers d'authentification pour l'API Hugging Face."""
    h = {"Content-Type": "application/json"}
    if HF_API_TOKEN:
        h["Authorization"] = f"Bearer {HF_API_TOKEN}"
    return h


# ─── Zero-Shot Classification (Triage) ──────────────────────────────────────

async def classify_zero_shot(
    text: str,
    labels: list[str],
    model: str = MODEL_ZERO_SHOT,
) -> dict:
    """
    Classification zero-shot via facebook/bart-large-mnli.
    Retourne: {"labels": [...], "scores": [...], "sequence": "..."}
    """
    payload = {
        "inputs": text,
        "parameters": {"candidate_labels": labels},
    }
    try:
        async with httpx.AsyncClient(timeout=HF_TIMEOUT) as client:
            resp = await client.post(
                f"{HF_API_URL}/{model}",
                json=payload,
                headers=_headers(),
            )
            resp.raise_for_status()
            return resp.json()
    except Exception as e:
        logger.error(f"Erreur zero-shot classification: {e}")
        return {"labels": labels, "scores": [1.0 / len(labels)] * len(labels), "sequence": text}


# ─── Text Generation (Chat / Interactions) ───────────────────────────────────

async def generate_text(
    prompt: str,
    model: str = MODEL_CHAT,
    max_new_tokens: int = 512,
    temperature: float = 0.7,
) -> str:
    """
    Génération de texte via Mistral-7B-Instruct.
    Retourne la réponse générée en texte brut.
    """
    payload = {
        "inputs": prompt,
        "parameters": {
            "max_new_tokens": max_new_tokens,
            "temperature": temperature,
            "return_full_text": False,
            "do_sample": True,
        },
    }
    try:
        async with httpx.AsyncClient(timeout=HF_TIMEOUT) as client:
            resp = await client.post(
                f"{HF_API_URL}/{model}",
                json=payload,
                headers=_headers(),
            )
            resp.raise_for_status()
            data = resp.json()

            # HF renvoie une liste de dicts avec "generated_text"
            if isinstance(data, list) and len(data) > 0:
                return data[0].get("generated_text", "").strip()
            elif isinstance(data, dict):
                return data.get("generated_text", str(data)).strip()
            return str(data)
    except httpx.HTTPStatusError as e:
        # Modèle en chargement (503) → message d'attente
        if e.response.status_code == 503:
            logger.warning("Modèle HF en cours de chargement (cold start)...")
            return "[Le modèle IA est en cours de chargement. Veuillez réessayer dans quelques secondes.]"
        logger.error(f"Erreur HTTP génération texte: {e}")
        return ""
    except Exception as e:
        logger.error(f"Erreur génération texte: {e}")
        return ""


# ─── Embeddings (pour le RAG) ────────────────────────────────────────────────

async def get_embeddings(texts: list[str], model: str = MODEL_EMBEDDINGS) -> list[list[float]]:
    """
    Calcule les embeddings via sentence-transformers.
    Retourne une liste de vecteurs pour chaque texte.
    """
    payload = {
        "inputs": texts,
        "options": {"wait_for_model": True},
    }
    try:
        async with httpx.AsyncClient(timeout=HF_TIMEOUT) as client:
            resp = await client.post(
                f"{HF_API_URL}/{model}",
                json=payload,
                headers=_headers(),
            )
            resp.raise_for_status()
            return resp.json()
    except Exception as e:
        logger.error(f"Erreur embeddings: {e}")
        return []


# ─── Health Check HF ─────────────────────────────────────────────────────────

async def check_hf_health() -> dict:
    """Vérifie que l'API HF est accessible."""
    status = {"huggingface_api": "unknown", "token_configured": bool(HF_API_TOKEN)}
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.get(
                f"{HF_API_URL}/{MODEL_ZERO_SHOT}",
                headers=_headers(),
            )
            status["huggingface_api"] = "ok" if resp.status_code in (200, 503) else f"error ({resp.status_code})"
    except Exception as e:
        status["huggingface_api"] = f"error: {e}"
    return status
