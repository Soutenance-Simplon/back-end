# ============================================================
# Stage 1: Builder (Maven + OpenJDK 21)
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Configuration du mirror Maven pour éviter le rate-limiting HTTP 429 de Maven Central en CI/CD
COPY .mvn/settings.xml /root/.m2/settings.xml

# 1. Installation non-récursive du POM parent (enregistre diam-yaraam:parent dans ~/.m2)
COPY pom.xml .
RUN mvn --batch-mode clean install -N -DskipTests

# 2. Compilation et installation de shared-lib
COPY shared-lib/pom.xml shared-lib/
COPY shared-lib/src shared-lib/src
RUN mvn --batch-mode clean install -f shared-lib/pom.xml -DskipTests

# 3. Compilation et packaging du service
COPY auth-service/pom.xml auth-service/
COPY auth-service/src auth-service/src
RUN mvn --batch-mode clean package -f auth-service/pom.xml -DskipTests

# ============================================================
# Stage 2: Runtime (JRE 21 minimaliste & sécurisé)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder --chown=appuser:appgroup /build/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8081

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
