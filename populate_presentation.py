import psycopg2
import uuid
from datetime import datetime, timedelta
import json

def generate_uuid():
    return str(uuid.uuid4())

def get_db_connection():
    return psycopg2.connect(
        dbname="diam_yaraam",
        user="postgres",
        password="Fatou3112",
        host="127.0.0.1",
        port="5432"
    )

def setup_presentation_data():
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        print("1. Recherche des utilisateurs...")
        # Get Patient (Oumar)
        cursor.execute("SELECT id FROM auth_schema.users WHERE telephone = '+221775556677'")
        fatou_user_id = cursor.fetchone()[0]
        
        # Get Dr Sow
        cursor.execute("SELECT id FROM auth_schema.users WHERE telephone = '+221773334455'")
        dr_sow_user_id = cursor.fetchone()[0]
        
        print(f"Fatou User ID: {fatou_user_id}")
        print(f"Dr Sow User ID: {dr_sow_user_id}")
        
        # Get or create Patient for Fatou
        cursor.execute("SELECT id FROM patient_schema.patients_patient WHERE user_id = %s", (fatou_user_id,))
        patient_row = cursor.fetchone()
        if not patient_row:
            patient_id = generate_uuid()
            cursor.execute("""
                INSERT INTO patient_schema.patients_patient (id, user_id, consent_analyse_ia, consent_partage_famille, created_at, updated_at) 
                VALUES (%s, %s, true, false, NOW(), NOW())
            """, (patient_id, fatou_user_id))
        else:
            patient_id = patient_row[0]
            
        # Get or create Medecin for Dr Sow
        cursor.execute("SELECT id FROM medecin_schema.medecin WHERE user_id = %s", (dr_sow_user_id,))
        medecin_row = cursor.fetchone()
        if not medecin_row:
            medecin_id = generate_uuid()
            cursor.execute("""
                INSERT INTO medecin_schema.medecin (id, user_id, statut_medecin, is_verified, tarif_consultation, created_at) 
                VALUES (%s, %s, 'ACTIF', true, 15000, NOW())
            """, (medecin_id, dr_sow_user_id))
        else:
            medecin_id = medecin_row[0]
            
        # Get or create Dossier for Fatou
        cursor.execute("SELECT id FROM dossier_schema.dossier_medical WHERE patient_id = %s", (patient_id,))
        dossier_row = cursor.fetchone()
        if not dossier_row:
            dossier_id = generate_uuid()
            cursor.execute("""
                INSERT INTO dossier_schema.dossier_medical (id, patient_id, groupe_sanguin, taille, poids, groupe_sanguin_valide, statut_dossier) 
                VALUES (%s, %s, 'O+', 1.65, 68.5, true, 'ACTIF')
            """, (dossier_id, patient_id))
        else:
            dossier_id = dossier_row[0]
            
        print("2. Nettoyage des anciennes données de présentation...")
        cursor.execute("DELETE FROM rdv_schema.salle_teleconsultation WHERE rendez_vous_id IN (SELECT id FROM rdv_schema.rendez_vous WHERE patient_id = %s AND medecin_id = %s)", (patient_id, medecin_id))
        cursor.execute("DELETE FROM dossier_schema.allergie WHERE dossier_medical_id = %s", (dossier_id,))
        cursor.execute("DELETE FROM dossier_schema.maladie_cronique WHERE dossier_medical_id = %s", (dossier_id,))
        cursor.execute("DELETE FROM dossier_schema.antecedent_medical WHERE dossier_medical_id = %s", (dossier_id,))
        cursor.execute("DELETE FROM dossier_schema.dossier_consultation WHERE dossier_medical_id = %s", (dossier_id,))
        cursor.execute("DELETE FROM dossier_schema.prescription WHERE dossier_medical_id = %s", (dossier_id,))
        cursor.execute("DELETE FROM rdv_schema.rendez_vous WHERE patient_id = %s AND medecin_id = %s", (patient_id, medecin_id))
        
        print("3. Insertion de l'historique médical (Allergies, Maladies, Antécédents)...")
        # Allergie 1
        cursor.execute("""
            INSERT INTO dossier_schema.allergie (id, dossier_medical_id, type_allergie, nom, description, source_allergie, severite, est_critique, statut, date_validation, created_at)
            VALUES (%s, %s, 'MEDICAMENTEUSE', 'Pénicilline', 'Éruption cutanée après ingestion', 'MEDICAMENT', 'MODEREE', true, 'CONFIRMEE', CURRENT_DATE, NOW())
        """, (generate_uuid(), dossier_id))
        
        # Allergie 2
        cursor.execute("""
            INSERT INTO dossier_schema.allergie (id, dossier_medical_id, type_allergie, nom, description, source_allergie, severite, est_critique, statut, date_validation, created_at)
            VALUES (%s, %s, 'ALIMENTAIRE', 'Arachide', 'Gêne respiratoire légère', 'ALIMENT', 'SEVERE', true, 'CONFIRMEE', CURRENT_DATE, NOW())
        """, (generate_uuid(), dossier_id))
        
        # Maladie chronique
        cursor.execute("""
            INSERT INTO dossier_schema.maladie_cronique (id, dossier_medical_id, nom_maladie, code_cim10, description, date_diagnostic, statut_diagnostic, archive, created_at)
            VALUES (%s, %s, 'Hypertension artérielle', 'I10', 'HTA diagnostiquée lors d''un bilan de routine', CURRENT_DATE - INTERVAL '1 year', 'CONFIRME', false, NOW())
        """, (generate_uuid(), dossier_id))
        
        # Antécédent
        cursor.execute("""
            INSERT INTO dossier_schema.antecedent_medical (id, dossier_medical_id, type_antecedent, titre, description, date_evenement, statut, created_at)
            VALUES (%s, %s, 'CHIRURGICAL', 'Appendicectomie', 'Intervention sans complications', CURRENT_DATE - INTERVAL '3 years', 'RESOLU', NOW())
        """, (generate_uuid(), dossier_id))
        
        print("4. Insertion des consultations et prescriptions...")
        # Consultation passée
        past_consultation_id = generate_uuid()
        cursor.execute("""
            INSERT INTO dossier_schema.dossier_consultation (id, dossier_medical_id, medecin_id, date_consultation, motif, diagnostic, examen_clinique, traitement_propose, teleconsultation, created_at)
            VALUES (%s, %s, %s, CURRENT_DATE - INTERVAL '2 months', 'Maux de tête fréquents', 'Céphalées de tension', 'Tension artérielle à 13/8, examen neurologique normal', 'Repos et Paracétamol', false, NOW())
        """, (past_consultation_id, dossier_id, medecin_id))
        
        # Prescription
        medicaments = [
            {"nom": "Paracétamol 1000mg", "posologie": "1 comprimé toutes les 8h si douleur", "duree": "5 jours"}
        ]
        import random
        pres_num = f'PRES-2026-{random.randint(1000, 9999)}'
        cursor.execute("""
            INSERT INTO dossier_schema.prescription (id, dossier_medical_id, medecin_id, numero_prescription, statut, details_medicaments, created_at)
            VALUES (%s, %s, %s, %s, 'VALIDE', %s, NOW())
        """, (generate_uuid(), dossier_id, medecin_id, pres_num, json.dumps(medicaments)))
        
        print("5. Insertion des rendez-vous...")
        # Past RDV
        cursor.execute("""
            INSERT INTO rdv_schema.rendez_vous (id, patient_id, medecin_id, date_heure_souhaitee, date_heure_confirmee, motif, est_urgent, type_consultation, statut, tarif_applique, paiement_valide, created_at, updated_at)
            VALUES (%s, %s, %s, NOW() - INTERVAL '2 months', NOW() - INTERVAL '2 months', 'Maux de tête fréquents', false, 'CABINET', 'TERMINE', 15000, true, NOW() - INTERVAL '2 months', NOW() - INTERVAL '2 months')
        """, (generate_uuid(), patient_id, medecin_id))
        
        # Upcoming RDV (for Teleconsultation tomorrow)
        cursor.execute("""
            INSERT INTO rdv_schema.rendez_vous (id, patient_id, medecin_id, date_heure_souhaitee, date_heure_confirmee, motif, est_urgent, type_consultation, statut, tarif_applique, paiement_valide, reference_paiement, created_at, updated_at)
            VALUES (%s, %s, %s, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day', 'Suivi tension et vertiges', false, 'TELECONSULTATION', 'CONFIRME', 15000, true, 'PAY-TEST-999', NOW(), NOW())
        """, (generate_uuid(), patient_id, medecin_id))
        
        conn.commit()
        print("Succès : Les données de présentation ont été générées et insérées.")

    except Exception as e:
        conn.rollback()
        print(f"Erreur: {e}")
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    setup_presentation_data()
