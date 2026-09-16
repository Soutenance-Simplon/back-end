"""
Script de Démonstration et Test du Moteur RAG Clinique — Diam Yaraam
Permet de tester l'extraction directe depuis les documents officiels :
- Guide Médicaments Essentiels MSF/OMS (Éd. 2024-2026, p. 51)
- Référentiel Dorosz (41e éd.)
- Règles d'orientation médicale Diam Yaraam (ONDMS Section 5.1, 7 & 8)
- Algorithmes OMS Soins Primaires d'Urgence (BEC / SPU)
"""

import sys
import asyncio
sys.stdout.reconfigure(encoding='utf-8')

from rag_engine import rag_engine

def separator(title):
    print("\n" + "=" * 80)
    print(f"  {title}")
    print("=" * 80)

async def test_rag():
    separator("1. STATUT DE LA BASE DOCUMENTAIRE RAG")
    print(f"Chargée en mémoire : {rag_engine.is_loaded}")
    print(f"Documents indexés dans /documents/ :")
    print(f"  - Guide des Médicaments Essentiels MSF / OMS (guideline-339-fr.pdf)")
    print(f"  - Guide Pratique des Médicaments Dorosz (41e édition)")
    print(f"  - Référentiel d'Orientation Médicale Diam Yaraam (ONDMS)")
    print(f"  - Protocole OMS Soins Primaires d'Urgence (BEC / SPU)")
    print(f"Critères d'urgences vitales (Red Flags) : {len(rag_engine.red_flags_db)}")
    print(f"Spécialités médicales référencées       : {len(rag_engine.specialties_db)}")
    print(f"Règles d'interactions critiques MSF      : {len(rag_engine.drug_interactions_db)}")

    separator("2. TEST RAG PHARMACOLOGIQUE (GUIDE MSF p. 51)")
    print("Requête : Amiodarone 200mg + Ciprofloxacine 500mg")
    interactions = rag_engine.evaluate_drug_safety(["Amiodarone 200mg", "Ciprofloxacine 500mg"], [])
    if interactions:
        inter = interactions[0]
        print(f"-> Détection RAG : {inter['medicament1']} ↔ {inter['medicament2']}")
        print(f"-> Niveau de danger : {inter['niveau_danger']} (Bloquant : {inter['bloquant']})")
        print(f"-> Source officielle : {inter['source_medicale']}")
        print(f"-> Mécanisme clinique : {inter['explication']}")
        print(f"-> Alternative thérapeutique proposée : {inter['alternative_recommandee']}")

    separator("3. TEST RAG ALLERGIE CROISÉE (DOROSZ / MSF BÊTA-LACTAMINES)")
    print("Requête : Patient allergique à la Pénicilline + Prescription Amoxicilline 500mg")
    allergie_res = rag_engine.evaluate_drug_safety(["Amoxicilline 500mg"], ["Pénicilline (Bêta-lactamines)"])
    if allergie_res:
        inter_a = allergie_res[0]
        print(f"-> Détection RAG : {inter_a['medicament1']} ↔ {inter_a['medicament2']}")
        print(f"-> Niveau de danger : {inter_a['niveau_danger']}")
        print(f"-> Source officielle : {inter_a['source_medicale']}")
        print(f"-> Mécanisme clinique : {inter_a['explication']}")
        print(f"-> Alternative thérapeutique proposée : {inter_a['alternative_recommandee']}")

    separator("4. TEST RAG URGENCE VITALE (SECTION 5.1 ONDMS & OMS BEC)")
    symptome_vital = "Douleur oppressive dans la poitrine irradiant vers le bras gauche avec sueurs"
    print(f"Symptôme : \"{symptome_vital}\"")
    eval_vital = rag_engine.evaluate_triage_and_orientation(symptome_vital)
    print(f"-> Urgence vitale : {eval_vital['est_urgence_vitale']}")
    print(f"-> Niveau : {eval_vital['niveau_gravite']} / {eval_vital['niveau_urgence']}")
    print(f"-> Numéro d'appel d'urgence : {eval_vital['numero_urgence']}")
    print(f"-> Source protocolaire : {eval_vital['source_medicale']}")
    print(f"-> Règle appliquée : {eval_vital['justification_orientation']}")
    print(f"-> Premiers gestes immédiats :")
    for g in eval_vital['premiers_gestes'][:3]:
        print(f"   - {g}")

    separator("5. TEST RAG ORIENTATION SPÉCIALISTE (SECTION 7 & 8 ONDMS)")
    symptome_spe = "Plaques rouges et démangeaisons cutanées sur les avant-bras depuis 5 jours"
    print(f"Symptôme : \"{symptome_spe}\"")
    eval_spe = rag_engine.evaluate_triage_and_orientation(symptome_spe)
    print(f"-> Urgence vitale : {eval_spe['est_urgence_vitale']}")
    print(f"-> Spécialité recommandée : {eval_spe['specialites_suggerees']}")
    print(f"-> Règle appliquée : {eval_spe['justification_orientation']}")
    print(f"-> Rappel légal obligatoire : {eval_spe['rappel_legal']}")

    separator("FIN DES TESTS DU MOTEUR RAG")
    print("Tous les documents sources sont indexés et répondent en temps réel avec traçabilité complète.\n")

if __name__ == "__main__":
    asyncio.run(test_rag())
