# ==================== BUILD STAGE ====================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first for better Docker layer caching
COPY pom.xml .

# Copy Maven wrapper files if present
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Copy source code
COPY src src

# Build the Spring Boot application
RUN mvn clean package -DskipTests


# ==================== RUNTIME STAGE ====================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated Spring Boot jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]