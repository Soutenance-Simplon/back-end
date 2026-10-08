import psycopg2
import uuid
from datetime import datetime, timedelta

def setup_doctor_slots():
    conn = psycopg2.connect(dbname='diam_yaraam', user='postgres', password='Fatou3112', host='127.0.0.1', port='5432')
    cur = conn.cursor()
    
    # Get Dr Sow ID
    cur.execute("SELECT id FROM medecin_schema.medecin WHERE user_id = (SELECT id FROM auth_schema.users WHERE telephone = '+221773334455')")
    medecin_id = cur.fetchone()[0]
    
    now = datetime.now()
    today_date = now.date()
    
    # Delete old slots
    cur.execute("DELETE FROM medecin_schema.creneau_disponible WHERE medecin_id = %s", (medecin_id,))
    cur.execute("DELETE FROM medecin_schema.disponibilite_medecin WHERE medecin_id = %s", (medecin_id,))
    
    # Create a general disponibilite
    disp_id = str(uuid.uuid4())
    cur.execute("""
        INSERT INTO medecin_schema.disponibilite_medecin (
            id, medecin_id, jour_semaine, heure_debut, heure_fin, duree_creneau_minutes, type_consultation, date_debut_validite, date_fin_validite, actif, created_at, updated_at
        ) VALUES (%s, %s, 'TOUS', '08:00:00', '18:00:00', 30, 'MIXTE', %s, %s, true, %s, %s)
    """, (disp_id, medecin_id, today_date - timedelta(days=1), today_date + timedelta(days=30), now, now))
    
    # Generate slots for today, tomorrow, and day after
    for day_offset in range(3):
        target_date = now + timedelta(days=day_offset)
        # 3 slots per day: 09:00, 10:00, 14:00
        hours = [9, 10, 14, 15, 16]
        
        for hour in hours:
            # Randomly mix CABINET and TELECONSULTATION
            ctype = 'TELECONSULTATION' if hour > 13 else 'CABINET'
            
            slot_start = target_date.replace(hour=hour, minute=0, second=0, microsecond=0)
            if slot_start < now:
                continue # Skip past slots for today
                
            slot_end = slot_start + timedelta(minutes=30)
            
            creneau_id = str(uuid.uuid4())
            cur.execute("""
                INSERT INTO medecin_schema.creneau_disponible (
                    id, disponibilite_id, medecin_id, date_heure_debut, date_heure_fin, type_consultation, statut, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, 'LIBRE', %s)
            """, (creneau_id, disp_id, medecin_id, slot_start, slot_end, ctype, now))

    conn.commit()
    print("Créneaux disponibles (Slots) insérés avec succès pour Dr. Sow !")
    cur.close()
    conn.close()

if __name__ == '__main__':
    setup_doctor_slots()
