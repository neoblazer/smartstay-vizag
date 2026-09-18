# ── Build stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and project descriptor first for layer caching
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Ensure Maven wrapper is executable (avoids exit code 126 on Linux)
RUN chmod +x ./mvnw

# Download dependencies (cached as long as pom.xml is unchanged)
RUN ./mvnw dependency:go-offline -q

# Copy source and compile
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/smartstay-vizag-backend-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
