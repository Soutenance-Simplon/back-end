# ============================================================
# Stage 1: Builder (Maven + OpenJDK 21)
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Configuration du mirror Maven pour éviter le rate-limiting HTTP 429 de Maven Central en CI/CD
COPY .mvn/settings.xml /root/.m2/settings.xml

# 1. Installation non-récursive du POM parent dans ~/.m2 (ignore les modules frères non copiés)
COPY pom.xml .
RUN mvn --batch-mode clean install -N -DskipTests

# 2. Copie et packaging du service discovery-server
COPY discovery-server/pom.xml discovery-server/
COPY discovery-server/src discovery-server/src
RUN mvn --batch-mode clean package -f discovery-server/pom.xml -DskipTests

# ============================================================
# Stage 2: Runtime (JRE 21 minimaliste & sécurisé)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root pour la sécurité conteneur
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder --chown=appuser:appgroup /build/discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8761

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8761/ || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
