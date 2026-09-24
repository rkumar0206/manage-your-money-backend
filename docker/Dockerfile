# syntax=docker/dockerfile:1

# ==============================================================================
# Stage 1: Build the Spring Boot application
# ==============================================================================
FROM gradle:9.7.1-jdk25 AS build
WORKDIR /app

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN
ENV GITHUB_ACTOR=$GITHUB_ACTOR
ENV GITHUB_TOKEN=$GITHUB_TOKEN

COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle build.gradle settings.gradle ./

# Download dependencies (Cached in Docker unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon

# Step B: Copy source code and build the bootable JAR
COPY --chown=gradle:gradle src src
RUN ./gradlew bootJar -x test --no-daemon


# ==============================================================================
# Stage 2: Build a minimal custom JRE with jlink
# ==============================================================================
FROM eclipse-temurin:25-jdk-alpine AS jre-builder
WORKDIR /app

COPY --from=build /app/build/libs/app.jar app.jar

RUN jar xf app.jar && \
    jdeps --ignore-missing-deps -q \
      --recursive \
      --multi-release 25 \
      --print-module-deps \
      --class-path 'BOOT-INF/lib/*' \
      BOOT-INF/classes > modules.txt

RUN jlink \
      --add-modules "$(cat modules.txt),jdk.crypto.ec,jdk.unsupported,java.sql,java.naming,java.management,jdk.management" \
      --strip-debug \
      --no-man-pages \
      --no-header-files \
      --compress=zip-9 \
      --output /custom-jre


# ==============================================================================
# Stage 3: Lightweight, secure runtime image
# ==============================================================================
FROM alpine:3.21

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV JAVA_HOME=/opt/java/custom
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-builder /custom-jre ${JAVA_HOME}

WORKDIR /app
COPY --from=build --chown=appuser:appgroup /app/build/libs/app.jar app.jar

USER appuser
EXPOSE 2600

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+UseSerialGC", \
  "-jar", "app.jar"]