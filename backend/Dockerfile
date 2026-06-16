# Etapa de Compilación
FROM maven:3.8-eclipse-temurin-8 AS build
WORKDIR /app
COPY pom.xml .
# Descargar dependencias para cachear capas de Docker
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package  -B -DskipTests

# Etapa de Ejecución
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/banco-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
