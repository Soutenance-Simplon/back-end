import httpx

tests = [
    (['Amoxicilline 500mg'], ['Pénicilline']),
    (['Amiodarone 200mg', 'Ciprofloxacine 500mg'], []),
    (['Ibuprofène 400mg', 'Aspirine 500mg'], []),
    (['Bactrim 800mg'], ['Sulfamides']),
    (['Tramadol 50mg', 'Fluoxétine 20mg'], []),
]

for meds, allergies in tests:
    resp = httpx.post('http://127.0.0.1:8089/ia/interactions', json={'medicaments': meds, 'allergies': allergies})
    data = resp.json()
    print(f"\n--- Test: {meds} | Allergies: {allergies} ---")
    for item in data:
        print(f"  Molécules: {item.get('medicament1')} <-> {item.get('medicament2')}")
        print(f"  Danger: {item.get('niveau_danger')}")
        print(f"  Page: {item.get('page_numero')}")
        print(f"  URL: {item.get('document_url')}")
        print(f"  Source: {item.get('source_medicale')}")
