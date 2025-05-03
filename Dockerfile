# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/NEW_TIMETABLE-SERVICE-0.0.1-SNAPSHOT.jar app.jar

# Expose the port defined in application.yml (8082)
EXPOSE 8083

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]