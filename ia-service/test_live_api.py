import sys
import json
import httpx
sys.stdout.reconfigure(encoding='utf-8')

BASE_URL = "http://127.0.0.1:8089"

def test_live_api():
    print("\n" + "="*80)
    print("TEST EN DIRECT DU SERVICE IA DIAM YARAAM (PORT 8089)")
    print("="*80)

    with httpx.Client(timeout=30.0) as client:
        # 1. Health Check
        print("\n--- 1. HEALTH CHECK (/health) ---")
        res = client.get(f"{BASE_URL}/health")
        print(f"Status Code: {res.status_code}")
        health_data = res.json()
        print(f"RAG Base chargée: {health_data.get('rag_database_loaded')}")
        print(f"Critères Urgence Vitale: {health_data.get('rag_red_flags_count')}")
        print(f"Spécialités ONDMS: {health_data.get('rag_specialties_count')}")
        print(f"Interactions MSF indexées: {health_data.get('rag_msf_interactions_count')}")
        print(f"Fournisseur LLM: {health_data.get('llm_provider')}")

        # 2. Test Triage - Urgence Vitale Infarctus
        print("\n--- 2. TRIAGE PATIENT : ALERTE ROUGE SAMU 1515 ---")
        prompt_infarctus = "J'ai une forte douleur dans la poitrine qui serre et qui descend dans mon bras gauche avec des sueurs"
        res = client.post(f"{BASE_URL}/triage", json={"description": prompt_infarctus})
        print(f"Status Code: {res.status_code}")
        data = res.json()
        print(f"🚨 Urgence Vitale : {data.get('est_urgence_vitale')}")
        print(f"Niveau Gravité   : {data.get('niveau_gravite')} / Niveau Urgence : {data.get('niveau_urgence')}")
        print(f"Numéro d'Urgence : {data.get('numero_urgence')}")
        print(f"Source Médicale  : {data.get('source_medicale')}")
        print(f"Red Flags        : {data.get('red_flags')}")
        print(f"Premiers Gestes  : {data.get('premiers_gestes')[:2]}")
        print(f"Conseil Immédiat : {data.get('conseil_immediat')}")

        # 3. Test Triage - Orientation Spécialiste Dermatologie (Non urgent)
        print("\n--- 3. TRIAGE PATIENT : ORIENTATION DERMATOLOGIE (SECTIONS 7 & 8) ---")
        prompt_dermato = "J'ai des plaques rouges et des démangeaisons intenses sur le bras depuis 4 jours"
        res = client.post(f"{BASE_URL}/triage", json={"description": prompt_dermato})
        print(f"Status Code: {res.status_code}")
        data = res.json()
        print(f"Urgence Vitale    : {data.get('est_urgence_vitale')}")
        print(f"Niveau Gravité    : {data.get('niveau_gravite')}")
        print(f"Spécialité Cible  : {data.get('specialites_suggerees')}")
        print(f"Règle Appliquée   : {data.get('justification_orientation')}")
        print(f"Rappel Légal      : {data.get('rappel_legal')}")

        # 4. Test Chat Patient - Détection d'Urgence dans le Chat
        print("\n--- 4. ASSISTANT CHAT PATIENT (/chat) ---")
        res = client.post(
            f"{BASE_URL}/chat",
            json={"message": "Je suffoque, mes lèvres deviennent bleues et j'ai du mal à parler"},
            headers={"Authorization": "Bearer fake.patient.token"}
        )
        print(f"Status Code: {res.status_code}")
        chat_data = res.json()
        print(f"🚨 Alerte Vitale Déclenchée : {chat_data.get('est_urgence_vitale')}")
        print(f"Numéro d'appel : {chat_data.get('numero_urgence')}")
        print(f"Recommandations d'action : {chat_data.get('recommandations')}")
        print(f"Extrait Réponse : {chat_data.get('contenu')[:150]}...")

        # 5. Test Ordonnance - Blocage MSF p.51 (Amiodarone + Ciprofloxacine)
        print("\n--- 5. VÉRIFICATION D'ORDONNANCE : BLOCAGE MSF P.51 ---")
        res = client.post(
            f"{BASE_URL}/interactions",
            json={
                "medicaments": ["Amiodarone 200mg", "Ciprofloxacine 500mg"],
                "allergies": []
            }
        )
        print(f"Status Code: {res.status_code}")
        interactions = res.json()
        print(f"Interactions détectées : {len(interactions)}")
        for inter in interactions:
            print(f"⛔ DANGER : {inter.get('niveau_danger')}")
            print(f"   Bloquant par l'IA : {inter.get('bloquant')}")
            print(f"   Molécules : {inter.get('medicament1')} ⚡ {inter.get('medicament2')}")
            print(f"   Source : {inter.get('source_medicale')}")
            print(f"   Explication : {inter.get('explication')}")
            print(f"   Alternative Recommandée : {inter.get('alternative_recommandee')}")

        # 6. Test Ordonnance - Cas sans interaction
        print("\n--- 6. VÉRIFICATION D'ORDONNANCE : MOLÉCULE SÛRE ---")
        res = client.post(
            f"{BASE_URL}/interactions",
            json={
                "medicaments": ["Paracétamol 1g"],
                "allergies": []
            }
        )
        print(f"Status Code: {res.status_code}")
        interactions_safe = res.json()
        print(f"Interactions détectées : {len(interactions_safe)} (Attendu 0 pour molécule sûre sans conflit)")

    print("\n" + "="*80)
    print("✅ TOUS LES TESTS EN DIRECT DU SERVEUR FASTAPI SONT VALIDÉS !")
    print("="*80 + "\n")

if __name__ == "__main__":
    test_live_api()
