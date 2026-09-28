# ============================================================
# Stage 1: Builder (Maven + OpenJDK 21)
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# 1. Copie et installation du POM parent (mode non-récursif)
COPY pom.xml .
RUN mvn --batch-mode clean install -N -DskipTests

# 2. Compilation et installation de shared-lib
COPY shared-lib/pom.xml shared-lib/
COPY shared-lib/src shared-lib/src
RUN mvn --batch-mode clean install -f shared-lib/pom.xml -DskipTests

# 3. Compilation et packaging de patient-service
COPY patient-service/pom.xml patient-service/
COPY patient-service/src patient-service/src
RUN mvn --batch-mode clean package -f patient-service/pom.xml -DskipTests

# ============================================================
# Stage 2: Runtime (JRE 21 minimaliste & sécurisé)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder --chown=appuser:appgroup /build/patient-service/target/patient-service-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
