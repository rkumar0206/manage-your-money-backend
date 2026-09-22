# ==============================================================================
# Stage 1: Build the Spring Boot application
# ==============================================================================
FROM gradle:9.7.1-jdk25 AS build

WORKDIR /app

# Step A: Copy Gradle wrapper and configuration files
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle build.gradle settings.gradle ./

# Inject GitHub Package credentials
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
ENV GITHUB_TOKEN=$GITHUB_TOKEN

# Download dependencies (Cached in Docker unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Step B: Copy source code and build the bootable JAR
COPY --chown=gradle:gradle src src
RUN ./gradlew bootJar -x test --no-daemon


# ==============================================================================
# Stage 2: Create lightweight, secure runtime image
# ==============================================================================
FROM eclipse-temurin:25-jdk-alpine

# Security: Non-root user for Kubernetes compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the fixed app.jar directly and assign ownership to appuser
COPY --from=build --chown=appuser:appgroup /app/build/libs/app.jar app.jar

USER appuser

EXPOSE 2600

ENTRYPOINT ["java", "-jar", "app.jar"]