# syntax=docker/dockerfile:1
#
# Fellow Lodge Backend - production Docker image for Render
# -----------------------------------------------------------------------------
# Multi-stage build:
#   stage 1  Maven + Temurin JDK 21  -> builds the Spring Boot jar
#   stage 2  Temurin JRE 21          -> minimal runtime image
#
# The supabase Spring profile is activated and ALL secrets (Supabase DB
# credentials, JWT_SECRET, CORS origins) are injected at runtime via Render
# environment variables. Nothing is baked into the image.

# -----------------------------------------------------------------------------
# Stage 1 - build
# -----------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Cache Maven dependencies (invalidated only when pom.xml changes).
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

# Build the application inside the image. Unit/integration tests are executed
# separately (mvn test) rather than during the image build.
COPY src ./src
RUN mvn -B -q package -DskipTests

# -----------------------------------------------------------------------------
# Stage 2 - runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime

ENV APP_HOME=/opt/fellowlodge \
    APP_USER=fellowlodge

RUN groupadd -r ${APP_USER} \
    && useradd -r -g ${APP_USER} -d ${APP_HOME} ${APP_USER} \
    && mkdir -p ${APP_HOME} \
    && chown -R ${APP_USER}:${APP_USER} ${APP_HOME}

WORKDIR ${APP_HOME}

# Copy the application jar from the build stage.
COPY --from=build /build/target/fellow-lodge-backend-1.0.0.jar app.jar

# Production profile only. Secrets come from Render env vars, never the image.
ENV SPRING_PROFILES_ACTIVE=supabase

# Documentation only - Render injects PORT dynamically; Spring Boot binds to
# ${PORT:8081} (see application.yml). No port is hardcoded for production.
EXPOSE 8081

# Run as the unprivileged app user. No entrypoint is required: there is no
# persistent upload volume to prepare (uploads live in Supabase Storage and the
# container runs diskless on Render Free).
USER ${APP_USER}

# Heap sized to the container memory limit; exit instead of hanging on OOM.
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
