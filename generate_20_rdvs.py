import psycopg2
import uuid
import random
from datetime import datetime, timedelta

def populate_20_rdvs_per_doctor():
    conn = psycopg2.connect(dbname='diam_yaraam', user='postgres', password='Fatou3112', host='127.0.0.1', port='5432')
    cur = conn.cursor()
    
    # 1. Fetch all Medecins
    cur.execute("SELECT id FROM medecin_schema.medecin")
    medecins = [row[0] for row in cur.fetchall()]
    
    # 2. Fetch all Patients
    cur.execute("SELECT id FROM patient_schema.patients_patient")
    patients = [row[0] for row in cur.fetchall()]
    
    if not patients:
        print("Erreur: Aucun patient dans la base !")
        return
        
    motifs = [
        "Consultation générale", "Suivi traitement", "Maux de tête", 
        "Renouvellement d'ordonnance", "Douleurs abdominales", 
        "Consultation de routine", "Fièvre et courbatures", 
        "Résultats d'analyse", "Visite de contrôle", "Fatigue chronique"
    ]
    
    statuts = ['CONFIRME', 'TERMINE', 'ANNULE', 'EN_ATTENTE']
    types = ['CABINET', 'TELECONSULTATION']
    
    total_inserted = 0
    now = datetime.now()
    
    for medecin_id in medecins:
        # Generate exactly 20 RDVs per doctor
        for i in range(20):
            patient_id = random.choice(patients)
            motif = random.choice(motifs)
            
            # Distribute dates: some past, some future
            day_offset = random.randint(-15, 15)
            hour = random.randint(8, 17)
            minute = random.choice([0, 30])
            
            rdv_date = now.replace(hour=hour, minute=minute, second=0, microsecond=0) + timedelta(days=day_offset)
            
            statut = random.choice(statuts)
            if rdv_date < now and statut == 'EN_ATTENTE':
                statut = 'TERMINE'
            if rdv_date > now and statut == 'TERMINE':
                statut = 'CONFIRME'
                
            ctype = random.choice(types)
            est_urgent = random.choice([True, False])
            
            rdv_id = str(uuid.uuid4())
            
            cur.execute("""
                INSERT INTO rdv_schema.rendez_vous (
                    id, patient_id, medecin_id, date_heure_souhaitee, date_heure_confirmee, 
                    motif, est_urgent, type_consultation, statut, tarif_applique, 
                    paiement_valide, created_at, updated_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, 15000, true, %s, %s)
            """, (rdv_id, patient_id, medecin_id, rdv_date, rdv_date, motif, est_urgent, ctype, statut, now, now))
            
            # Create room if it's a confirmed teleconsultation
            if ctype == 'TELECONSULTATION' and statut == 'CONFIRME':
                salle_id = str(uuid.uuid4())
                cur.execute("""
                    INSERT INTO rdv_schema.salle_teleconsultation (
                        id, rendez_vous_id, token_patient, token_medecin, url_salle, 
                        statut, est_active, patient_connecte, medecin_connecte
                    ) VALUES (%s, %s, %s, %s, %s, 'ACTIVE', TRUE, FALSE, FALSE)
                """, (salle_id, rdv_id, f"TP-{uuid.uuid4()}", f"TM-{uuid.uuid4()}", f"dy_{uuid.uuid4().hex}"))
                
            total_inserted += 1
            
    conn.commit()
    print(f"Succès ! {total_inserted} rendez-vous ont été insérés au total pour {len(medecins)} médecin(s).")
    cur.close()
    conn.close()

if __name__ == '__main__':
    populate_20_rdvs_per_doctor()
