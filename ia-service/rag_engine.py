"""
Moteur RAG (Retrieval-Augmented Generation) — Diam Yaraam IA Service
Gère l'accès rapide et optimisé au guide médical Dorosz et aux recommandations de santé.
"""

import os
import re
import json
import logging
from typing import List, Dict

logger = logging.getLogger("ia-service.rag_engine")

# Configuration
DOCUMENTS_DIR = os.getenv("DOCUMENTS_PATH", "../documents")
PDF_NAME = "Dorosz_Guide_Pratique_des_medicaments_41E_2022.pdf"
INDEX_FILE = os.path.join(os.path.dirname(__file__), "dorosz_summary.json")

class RagEngine:
    def __init__(self):
        self.pdf_path = os.path.join(DOCUMENTS_DIR, PDF_NAME)
        self.knowledge_base: Dict[str, str] = {}
        self.is_loaded = False
        self._init_knowledge_base()

    def _init_knowledge_base(self):
        """Charge la base de connaissances médicale pré-indexée si disponible."""
        if os.path.exists(INDEX_FILE):
            try:
                with open(INDEX_FILE, "r", encoding="utf-8") as f:
                    self.knowledge_base = json.load(f)
                self.is_loaded = True
                logger.info(f"Base de connaissances chargée : {len(self.knowledge_base)} entrées indexées.")
                return
            except Exception as e:
                logger.warning(f"Erreur chargement index JSON: {e}")

        if os.path.exists(self.pdf_path):
            self.is_loaded = True
            logger.info(f"Document Dorosz ({self.pdf_path}) référencé pour consultation ciblée.")

    def search(self, query: str, top_k: int = 2) -> str:
        """
        Recherche ultra-rapide sans latence de blocage.
        """
        if not self.is_loaded:
            return ""

        query_lower = query.lower()
        results = []

        # 1. Recherche dans l'index pré-chargé
        for key, text in self.knowledge_base.items():
            if key.lower() in query_lower:
                results.append(f"[{key}] {text}")
                if len(results) >= top_k:
                    break

        if results:
            return "\n\n--- Extrait Guide Dorosz ---\n" + "\n".join(results)

        return ""

# Instance globale du moteur RAG
rag_engine = RagEngine()
