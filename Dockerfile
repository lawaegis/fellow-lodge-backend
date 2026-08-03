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

ARG APP_HOME=/opt/fellowlodge
ARG APP_USER=fellowlodge

RUN groupadd -r ${APP_USER} \
    && useradd -r -g ${APP_USER} -d ${APP_HOME} ${APP_USER} \
    && mkdir -p ${APP_HOME} \
    && chown -R ${APP_USER}:${APP_USER} ${APP_HOME}

WORKDIR ${APP_HOME}

# Copy the application jar from the build stage.
COPY --from=build /build/target/fellow-lodge-backend-1.0.0.jar app.jar

# Entrypoint fixes ownership of the persistent upload volume at container start
# (Render mounts its disk over /data at runtime), then drops to a non-root user.
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Production profile only. Secrets come from Render env vars, never the image.
ENV SPRING_PROFILES_ACTIVE=supabase

# Documentation only - Render injects PORT dynamically; Spring Boot binds to
# ${PORT:8081} (see application.yml). No port is hardcoded for production.
EXPOSE 8081

# Heap sized to the container memory limit; exit instead of hanging on OOM.
ENTRYPOINT ["/docker-entrypoint.sh"]
