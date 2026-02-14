# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first (layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:resolve -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Render sets PORT env variable (defaults to 10000 on Render)
# Azure App Service uses 8080 by default
ENV PORT=8080

EXPOSE ${PORT}

# JVM tuning for cloud deployment
# Use shell form so ${PORT} is expanded at runtime
ENTRYPOINT exec java \
  -Xmx256m \
  -Xms128m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -Djava.security.egd=file:/dev/./urandom \
  -Dserver.port=${PORT} \
  -Dspring.profiles.active=prod \
  -jar app.jar
