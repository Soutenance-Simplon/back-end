@echo off
title DIAM YARAAM - SERVICE IA OPENROUTER GPT-4o-mini (Port 8089)
echo ===================================================
echo   DEMARRAGE DU SERVICE IA OPENROUTER (FASTAPI)
echo ===================================================
echo.

cd /d "%~dp0"

rem 1. Configuration du fichier .env
if not exist .env copy .env.example .env

rem 2. Verification de Python
python --version >nul 2>&1
if errorlevel 1 goto nopython

rem 3. Creation de l'environnement virtuel si inexistant
if not exist .venv\Scripts\activate.bat python -m venv .venv
if errorlevel 1 goto errvenv

echo [+] Activation de l'environnement virtuel...
call .venv\Scripts\activate

echo [+] Verification et installation des dependances (requirements.txt)...
pip install -r requirements.txt
if errorlevel 1 goto errpip

echo.
echo ===================================================
echo   Lancement du serveur sur http://localhost:8089
echo ===================================================
python -m uvicorn main:app --host 0.0.0.0 --port 8089 --reload
goto end

:nopython
echo [x] Python n'est pas installe ou n'est pas dans le PATH.
echo [x] Veuillez installer Python 3.9+ pour faire tourner le service IA.
pause
exit /b 1

:errvenv
echo [x] Erreur lors de la creation du venv.
pause
exit /b 1

:errpip
echo [x] Erreur lors de l'installation des dependances pip.
pause
exit /b 1

:end


