# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/new_timetable-service-0.0.1-SNAPSHOT.jar app.jar

# Copy configuration files from src/main/resources to /app/resources/
COPY src/main/resources/ /app/resources/

# Expose the port defined in application.yml (8083)
EXPOSE 8083

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]