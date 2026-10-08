import psycopg2
import uuid
from datetime import datetime

def setup_live_rdv():
    conn = psycopg2.connect(dbname='diam_yaraam', user='postgres', password='Fatou3112', host='127.0.0.1', port='5432')
    cur = conn.cursor()
    
    # Obtenir IDs
    cur.execute("SELECT id FROM patient_schema.patients_patient WHERE user_id = (SELECT id FROM auth_schema.users WHERE telephone = '+221775556677')")
    patient_id = cur.fetchone()[0]
    
    cur.execute("SELECT id FROM medecin_schema.medecin WHERE user_id = (SELECT id FROM auth_schema.users WHERE telephone = '+221773334455')")
    medecin_id = cur.fetchone()[0]
    
    now = datetime.now()
    rdv_id = str(uuid.uuid4())
    
    # Inserer un RDV de teleconsultation pour MAINTENANT
    cur.execute("""
        INSERT INTO rdv_schema.rendez_vous (id, patient_id, medecin_id, date_heure_souhaitee, date_heure_confirmee, motif, est_urgent, type_consultation, statut, tarif_applique, paiement_valide, reference_paiement, created_at, updated_at)
        VALUES (%s, %s, %s, %s, %s, 'Téléconsultation Directe (Test)', false, 'TELECONSULTATION', 'CONFIRME', 15000, true, 'PAY-LIVE-001', %s, %s)
    """, (rdv_id, patient_id, medecin_id, now, now, now, now))
    
    # Inserer la salle virtuelle
    salle_id = str(uuid.uuid4())
    cur.execute("""
        INSERT INTO rdv_schema.salle_teleconsultation (
            id, rendez_vous_id, token_patient, token_medecin, url_salle, statut, est_active, patient_connecte, medecin_connecte
        ) VALUES (
            %s, %s, %s, %s, %s, 'ACTIVE', TRUE, FALSE, FALSE
        );
    """, (salle_id, rdv_id, f"TP-{uuid.uuid4()}", f"TM-{uuid.uuid4()}", f"dy_{uuid.uuid4().hex}"))
    
    conn.commit()
    print("RDV Téléconsultation EN DIRECT créé avec succès !")
    cur.close()
    conn.close()

if __name__ == '__main__':
    setup_live_rdv()
