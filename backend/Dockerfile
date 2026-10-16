# ──────────────────────────────────────────────────────────────────────────────
# EvidLock Backend — Dockerfile for Render.com deployment
# ──────────────────────────────────────────────────────────────────────────────

# Stage 1: Build the JAR with Maven
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom first (for Docker layer caching)
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build (skip tests — they require a live DB)
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Stage 2: Minimal JRE runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S evidlock && adduser -S evidlock -G evidlock

# Copy the built WAR (Spring Boot fat-WAR runs as standalone)
COPY --from=builder /app/target/*.war app.war

RUN chown evidlock:evidlock app.war
USER evidlock

# Render assigns PORT env var; default to 8081
EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dserver.port=${PORT:-8081}", \
  "-jar", "app.war"]
