"""
Tests automatisés des fonctionnalités IA de la soutenance Diam Yaraam :
1. Démo Patient : Triage vital Red Flag (SAMU 1515) & Orientation Spécialités (ONDMS)
2. Démo Médecin : Vérification d'ordonnance croisée & Blocage d'association MSF
"""

import sys
import asyncio
sys.stdout.reconfigure(encoding='utf-8')
from rag_engine import rag_engine
from llm_client import triage_symptoms_llm, analyze_interactions_llm

async def run_all_tests():
    print("\n" + "="*70)
    print("DÉBUT DES TESTS VALIDATION IA DIAM YARAAM")
    print("="*70)

    # 1. Test Red Flag Infarctus
    print("\n[TEST 1] Patient : Douleur thoracique aiguë (Infarctus)...")
    phrase = "J'ai une forte douleur dans la poitrine qui serre et qui descend dans mon bras gauche avec des sueurs"
    res1 = await triage_symptoms_llm(phrase)
    assert res1["est_urgence_vitale"] is True, f"Attendu True, obtenu {res1['est_urgence_vitale']}"
    assert res1["niveau_urgence"] == "SAMU", f"Attendu SAMU, obtenu {res1['niveau_urgence']}"
    assert res1["numero_urgence"] == "1515"
    assert "Section 5.1" in res1["source_medicale"]
    assert len(res1["premiers_gestes"]) >= 3
    print("  -> SUCCÈS : Urgence absolue SAMU 1515 détectée et sourcée.")

    # 2. Test Red Flag Pédiatrie
    print("\n[TEST 2] Patient : Urgence pédiatrique nourrisson 2 mois...")
    phrase2 = "Mon bébé de 2 mois a une forte fièvre et il est tout mou"
    res2 = await triage_symptoms_llm(phrase2)
    assert res2["est_urgence_vitale"] is True
    assert res2["niveau_urgence"] == "SAMU"
    print("  -> SUCCÈS : Urgence pédiatrique ETAT déclenchée.")

    # 3. Test Orientation Non Urgente Dermatologie
    print("\n[TEST 3] Patient : Éruption cutanée sans urgence vitale...")
    phrase3 = "J'ai des boutons rouges et des démangeaisons sur le bras depuis 3 jours"
    res3 = await triage_symptoms_llm(phrase3)
    assert res3["est_urgence_vitale"] is False
    assert "Dermatologie" in res3["specialites_suggerees"]
    assert "Section 7" in res3["justification_orientation"]
    assert "Règle 8.4" in res3["justification_orientation"]
    print("  -> SUCCÈS : Orientation Dermatologie confirmée (Section 7 & Règle 8.4).")

    # 4. Test Médecin : Blocage Amiodarone + Ciprofloxacine (MSF p. 51)
    print("\n[TEST 4] Médecin : Interaction mortelle Amiodarone + Ciprofloxacine...")
    interactions = await analyze_interactions_llm(["Amiodarone", "Ciprofloxacine"], [])
    assert len(interactions) >= 1
    found_qt = False
    for inter in interactions:
        if "CONTRE_INDICATION_ABSOLUE" in inter.get("niveau_danger", ""):
            assert inter.get("bloquant") is True
            assert "MSF" in inter.get("source_medicale", "")
            assert inter.get("alternative_recommandee") is not None
            found_qt = True
    assert found_qt is True
    print("  -> SUCCÈS : Blocage absolu QT Long MSF p. 51 avec alternative validée.")

    # 5. Test Médecin : Allergie Pénicilline vs Amoxicilline
    print("\n[TEST 5] Médecin : Contre-indication Amoxicilline si allergie Pénicilline...")
    allergie_res = await analyze_interactions_llm(["Amoxicilline"], ["Pénicilline"])
    assert len(allergie_res) >= 1
    assert allergie_res[0].get("niveau_danger") == "CONTRE_INDICATION_ABSOLUE"
    assert allergie_res[0].get("bloquant") is True
    print("  -> SUCCÈS : Contre-indication absolue d'allergie croisée détectée.")

    print("\n" + "="*70)
    print("✅ TOUS LES TESTS CLINIQUES ET PHARMACOLOGIQUES SONT VALIDÉS AVEC SUCCÈS !")
    print("="*70 + "\n")

if __name__ == "__main__":
    asyncio.run(run_all_tests())
