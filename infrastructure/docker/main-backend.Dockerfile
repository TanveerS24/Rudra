# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY backend ./backend
WORKDIR /workspace/backend
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/backend/main-backend/target/main-backend-1.0.0.jar app.jar
EXPOSE 8081 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

