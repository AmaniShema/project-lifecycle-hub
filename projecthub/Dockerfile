# ─────────────────────────────────────────────
# Stage 1: Build the application
# Use the full Maven + Java 21 image to compile
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy pom.xml first and download dependencies
# This layer is cached by Docker — if pom.xml
# does not change, dependencies are not re-downloaded
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping tests
# Tests were already verified locally
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Run the application
# Use a minimal Java 21 runtime — no Maven needed
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy only the compiled jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port our Spring Boot app runs on
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
