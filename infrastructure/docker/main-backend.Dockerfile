FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY backend/main-backend/target/main-backend-1.0.0.jar app.jar
EXPOSE 8081 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
