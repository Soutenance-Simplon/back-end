FROM python:3.11-slim

# Empêche Python d'écrire des fichiers .pyc et active le streaming immédiat des logs
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    IA_SERVICE_PORT=8089 \
    DOCUMENTS_PATH=/app/documents

WORKDIR /app

# Création d'un utilisateur applicatif non-root pour la sécurité conteneur
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser

# Installation optimisée des dépendances Python (cache des couches Docker)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copie du code source applicatif
COPY . .

# Création du point de montage pour les documents cliniques
RUN mkdir -p /app/documents && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8089

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD python -c "import urllib.request, os; urllib.request.urlopen('http://localhost:' + os.getenv('IA_SERVICE_PORT', '8089') + '/health')" || exit 1

CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${IA_SERVICE_PORT:-8089}"]
