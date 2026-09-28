-- ============================================================
-- DIAM-YARAAM — Initialisation des schémas PostgreSQL
-- Exécuté automatiquement par le conteneur postgres officiel
-- via le point d'entrée /docker-entrypoint-initdb.d/
-- ============================================================

-- Extension UUID requise pour la génération d'identifiants uniques
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Création des schémas dédiés pour chaque microservice métier
-- Garantit l'isolation logique des données (pattern Database-per-Service logique)
CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS patient_schema;
CREATE SCHEMA IF NOT EXISTS medecin_schema;
CREATE SCHEMA IF NOT EXISTS rdv_schema;
CREATE SCHEMA IF NOT EXISTS dossier_schema;
CREATE SCHEMA IF NOT EXISTS notification_schema;
CREATE SCHEMA IF NOT EXISTS wallet_schema;
