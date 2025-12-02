# Build stage
FROM eclipse-temurin:8-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests package

# Runtime stage
FROM eclipse-temurin:8-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
