# Stage 1: Build the application using a Maven image
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml first to leverage Docker's layer caching.
# If pom.xml hasn't changed, Maven dependencies won't be re-downloaded.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application, skipping tests for a faster build
RUN mvn clean package -DskipTests


# Stage 2: Create the final, smaller runtime image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the compiled JAR file from the 'build' stage
# The JAR file is named after the artifactId and version in pom.xml
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]