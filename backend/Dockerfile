# Utilisation de l'image officielle OpenJDK avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copier le projet et installer les dépendances
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copier le reste des fichiers et builder l'application
COPY src ./src
RUN mvn clean package -DskipTests

# Utiliser une image plus légère pour exécuter l'application
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copier le JAR depuis l'étape précédente
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 8081
EXPOSE 8081

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
