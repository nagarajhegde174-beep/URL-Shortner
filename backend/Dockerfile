# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Downloader dependencies to cache them in docker layers
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the application package skipping tests (tests run in CI job separately)
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
