# Répertoire de Référence Médicale et Protocoles Cliniques

Ce dossier regroupe les guides cliniques, protocoles d'urgence et référentiels médicaux utilisés pour le moteur d'orientation et de RAG (Retrieval-Augmented Generation) du microservice `ia-service`.

## 📚 Documents Inclus

1. **`regles_orientation_medicale.md`** : Matrice décisionnelle pour le triage des symptômes et le niveau d'urgence (Vert, Jaune, Orange, Rouge).
2. **`guideline-339-fr.pdf`** : Lignes directrices de prise en charge clinique et orientation patient.
3. **`orientation_medicale-ang.pdf`** : Référentiel international d'orientation des pathologies courantes.
4. **`signes_alerte_urgences-ang.pdf`** : Signes cliniques d'alerte vitale nécessitant une régulation immédiate.
5. **`soins_urgences.pdf`** : Procédures de premiers secours et gestes d'urgence médicale.
6. **`triage_enfant-ang.pdf`** : Arbre décisionnel de triage pédiatrique pour nourrissons et enfants.

## 📖 Référentiel Dorosz (Pharmacologie)

Le **Guide Pratique des Médicaments Dorosz (41e édition)** (> 400 Mo) est indexé par le moteur RAG `rag_engine.py` pour la vérification des interactions médicamenteuses et contre-indications. En raison de sa volumétrie, ce fichier binaire volumineux doit être placé directement dans ce dossier lors de l'installation locale ou monté via un volume de stockage partagé en production.
