# ============================================================
# Stage 1: Builder (Maven + OpenJDK 21)
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# 1. Copie et installation du POM parent (mode non-récursif)
COPY pom.xml .
RUN mvn --batch-mode clean install -N -DskipTests

# 2. Copie et packaging de api-gateway (ne dépend pas de shared-lib)
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src
RUN mvn --batch-mode clean package -f api-gateway/pom.xml -DskipTests

# ============================================================
# Stage 2: Runtime (JRE 21 minimaliste & sécurisé)
# ============================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder --chown=appuser:appgroup /build/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8090

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
